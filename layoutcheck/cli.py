"""layout-check: does a MigLayout commit lay out any GUI differently than another one?

Builds MigLayout from two (or more) commits, lays out a catalogue of Swing GUIs with each build in every look and
feel, JVM configuration and JDK, and compares the candidates with the base build: the bounds of every component,
the minimum and preferred sizes of every MigLayout container, errors, and every screenshot pixel for pixel.
Differences are laid out again in fresh JVMs to tell regressions from noise. The result is a browsable report.

Exit status: 0 when nothing differs, 1 when a candidate differs, 2 when the check could not run completely.
"""
import argparse
import concurrent.futures as cf
import json
import os
import re
import shutil
import sys
import time
import webbrowser
from pathlib import Path

from . import compare, confirm, java, report, sources

HERE = Path(__file__).resolve().parent.parent
CACHE = HERE / ".cache"
CONFIGS = {
    "default": "",
    "hidpi-2x": "-Dsun.java2d.uiScale=2",
    "big-fonts": "-Dflatlaf.uiScale=1.5 -Dswing.plaf.metal.controlFont=Dialog-17 -Dswing.plaf.metal.userFont=Dialog-17",
}
EXAMPLES = """examples:
  layout-check v11.5.2 my-branch                      in a MigLayout checkout, or with --repo
  layout-check --repo ~/src/miglayout HEAD~1 HEAD
  layout-check --repo https://github.com/someone/miglayout.git 1a2b3c4 5d6e7f8
  layout-check base=v11.5.2 a=branch-a b=branch-b     name the builds; several candidates at once
  layout-check --laf flatlight --config default= --only '^app-' HEAD~1 HEAD       a quick look
  layout-check --jdk 21=/usr/lib/jvm/java-21 --jdk 25=/usr/lib/jvm/java-25 HEAD~1 HEAD
  layout-check --report-only --out out/v11.5.2-vs-my-branch                       rebuild the report
"""


def parse_args(argv):
    ap = argparse.ArgumentParser(prog="layout-check", description=__doc__, epilog=EXAMPLES,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("refs", nargs="*", metavar="[NAME=]REF",
                    help="git refs or commit ids: the base build first, then the candidates")
    ap.add_argument("--repo", help="MigLayout repository: a path or a git URL. Default: the MigLayout checkout "
                                   "this runs in, else a clone of upstream MigLayout")
    ap.add_argument("--out", help="the run directory (default: out/BASE-vs-CANDIDATE)")
    ap.add_argument("--laf", action="append", metavar="NAME",
                    help="look and feel to run, repeatable (default: every one this platform supports)")
    ap.add_argument("--config", action="append", metavar="NAME=JVM_FLAGS",
                    help="JVM configuration, repeatable (default: " + "; ".join(f"{k}={v}" for k, v in CONFIGS.items()) + ")")
    ap.add_argument("--jdk", action="append", metavar="[NAME=]JAVA_HOME",
                    help="JDK to run on, repeatable (default: java on the PATH)")
    ap.add_argument("--only", metavar="REGEX", help="only the scenarios whose id matches")
    ap.add_argument("--jobs", type=int, default=max(1, (os.cpu_count() or 2) // 2), help="JVMs to run at once")
    ap.add_argument("--confirm-runs", type=int, default=3, metavar="N",
                    help="fresh runs of each build for every scenario that differs (default 3, 0 = off)")
    ap.add_argument("--confirm-limit", type=int, default=150, metavar="N",
                    help="do not confirm a candidate with more differing scenarios than this (default 150)")
    ap.add_argument("--probe-baselines", action="store_true",
                    help="also list the components whose getBaseline(w, h) depends on earlier calls")
    ap.add_argument("--report-only", action="store_true", help="only rebuild the report of the run in --out")
    ap.add_argument("--open", action="store_true", help="open the report in a browser when done")
    args = ap.parse_args(argv)
    if args.report_only:
        if not args.out:
            ap.error("--report-only needs --out")
    elif len(args.refs) < 2:
        ap.error("give a base ref and at least one candidate ref")
    return args


def name_builds(specs):
    """[(name, ref)] from NAME=REF or REF; a name is a file name, unique among the builds."""
    named = []
    for spec in specs:
        name, ref = spec.split("=", 1) if re.match(r"^[\w.-]+=", spec) else (None, spec)
        name = re.sub(r"[^\w.-]+", "-", name or ref).strip("-.") or "build"
        while name in [n for n, _ in named]:
            name += "-2"
        named.append((name, ref))
    return named


def pairs(specs, what):
    result = {}
    for spec in specs:
        if "=" not in spec:
            if what == "jdk":
                spec = f"{Path(spec).name}={spec}"
            else:
                sys.exit(f"--{what} {spec}: expected NAME=VALUE")
        name, value = spec.split("=", 1)
        result[name] = value
    return result


def main(argv=None):
    args = parse_args(argv)
    try:
        return check(args) if not args.report_only else rebuild_report(Path(args.out).resolve(), args)
    except sources.SourceError as e:
        print(f"layout-check: {e}", file=sys.stderr)
        return 2


def check(args):
    started = time.strftime("%Y-%m-%d %H:%M:%S")
    named = name_builds(args.refs)
    repo, fetchable = sources.find_repo(args.repo, CACHE)
    print(f"Repository: {repo}")
    builds = {}
    for name, ref in named:
        sha = sources.resolve(repo, ref, fetchable)
        builds[name] = sources.build(repo, name, ref, sha, CACHE)
    base = named[0][0]
    flatlaf = sources.flatlaf(CACHE)

    out = Path(args.out or HERE / "out" / f"{base}-vs-{named[1][0]}").resolve()
    if out.exists():
        if not (out / "meta.json").exists() and any(out.iterdir()):
            sys.exit(f"{out} exists and is not a run directory; pick another --out")
        shutil.rmtree(out)
    out.mkdir(parents=True)
    # The catalogue is compiled per run, so that runs can go on side by side.
    classes = java.compile_catalogue(builds[base].jars() + [flatlaf], out / "classes")

    jdks = pairs(args.jdk, "jdk") if args.jdk else {"jdk": ""}
    configs = pairs(args.config, "config") if args.config else dict(CONFIGS)
    first_java = java.java_exe(next(iter(jdks.values())))
    supported = java.supported_lafs(first_java, classes, builds[base].jars() + [flatlaf])
    lafs = args.laf or supported
    for laf in lafs:
        if laf not in supported:
            sys.exit(f"Look and feel {laf} is not available here; available: {' '.join(supported)}")

    meta = {"base": base, "builds": {n: {"ref": b.ref, "sha": b.sha, "subject": b.subject} for n, b in builds.items()},
            "again": True, "jdks": jdks, "configs": configs, "lafs": lafs, "only": args.only, "repo": str(repo),
            "started": started}
    failures = []
    run_builds = list(builds) + [base + confirm.AGAIN]
    jobs = [(jdk, cfg, laf, b) for jdk in jdks for cfg in configs for laf in lafs for b in run_builds]
    print(f"Laying out the GUIs in {len(jobs)} JVMs (JDKs {', '.join(jdks)}; configs {', '.join(configs)}; "
          f"look and feels {', '.join(lafs)}; {len(run_builds)} runs), {args.jobs} at a time ...")
    t0 = time.time()

    def lay_out(job):
        jdk, cfg, laf, b = job
        jars = builds[b.removesuffix(confirm.AGAIN)].jars() + [flatlaf]
        return java.run(java.java_exe(jdks[jdk]), configs[cfg], classes, jars, "Runner",
                        compare.unit_dir(out, jdk, cfg, b, laf), laf, args.only)

    with cf.ThreadPoolExecutor(args.jobs) as ex:
        for i, err in enumerate(ex.map(lay_out, jobs), 1):
            if err:
                failures.append(err)
            if i % 10 == 0 or i == len(jobs):
                print(f"  {i}/{len(jobs)} ({time.time() - t0:.0f}s)")

    if args.probe_baselines:
        pjobs = [(jdk, laf, b) for jdk in jdks for laf in lafs for b in builds]
        print(f"Probing baselines: {len(pjobs)} JVMs ...")
        first_cfg = next(iter(configs.values()))
        with cf.ThreadPoolExecutor(args.jobs) as ex:
            failures += [e for e in ex.map(lambda j: java.run(
                java.java_exe(jdks[j[0]]), first_cfg, classes, builds[j[2]].jars() + [flatlaf], "Probe",
                out / "probe" / j[0] / f"{j[2]}--{j[1]}.json", j[1], args.only), pjobs) if e]

    print("Comparing ...")
    comparisons = compare.compare_all(out, meta, report.candidates_of(meta), args.jobs)
    if args.confirm_runs > 0:
        work, meta["unconfirmed"] = confirm.todo(comparisons, meta, args.confirm_limit)
        if work:
            confirm.rerun(out, meta, builds, classes, [flatlaf], work, args.confirm_runs, args.jobs, failures)
    meta["failures"] = failures
    meta["finished"] = time.strftime("%Y-%m-%d %H:%M:%S")
    (out / "meta.json").write_text(json.dumps(meta, indent=1))
    for f in failures:
        print("FAILED:", f[:2000], file=sys.stderr)
    return finish(out, meta, comparisons, args)


def rebuild_report(out, args):
    meta = compare.load(out / "meta.json")
    if meta is None:
        sys.exit(f"No run in {out} (no meta.json)")
    comparisons = compare.compare_all(out, meta, report.candidates_of(meta), args.jobs)
    return finish(out, meta, comparisons, args)


def finish(out, meta, comparisons, args):
    verdicts = confirm.verdicts(out, meta)
    totals = report.write(out, meta, comparisons, verdicts, compare.scenario_infos(out, meta))
    text, differs = report.summary(out, meta, totals)
    print(text)
    if args.open:
        webbrowser.open((out / "index.html").as_uri())
    return 2 if meta.get("failures") else 1 if differs else 0
