"""Telling regressions from noise.

Some GUIs do not come out the same in every JVM: GTK's text antialiasing, for one, varies from run to run. So every
JVM in which a candidate differs from the base build is run again, a few times with each of the two builds. A rerun
lays out the same scenarios in the same order as the first run, because a difference may depend on what ran before it
in the JVM. A step is noise only when every result of the candidate equals some result of the base build; where the
base build does not even agree with itself on the pixels, only bounds, sizes and errors are compared.
"""
import concurrent.futures as cf
import json

from .compare import compare_step, load, unit_dir
from . import java

AGAIN = "~again"  # the base build's second run, which measures what varies by itself


def todo(comparisons, meta, limit):
    """The (jdk, cfg, laf, candidate) JVMs to run again, and the candidates with too many of them to confirm."""
    base = meta["base"]
    found, skipped = [], []
    for c in meta["builds"]:
        if c == base:
            continue
        mine = sorted((jdk, cfg, laf, c) for (jdk, cfg, laf, cand), scen in comparisons.items() if cand == c
                      and any(st != "same" for steps in scen.values() for st, _ in steps))
        if len(mine) > limit:
            print(f"  {c}: differs in {len(mine)} JVMs (JDK, config and look and feel), more than --confirm-limit "
                  f"{limit}; not confirming its differences")
            skipped.append(c)
        else:
            found += mine
    return found, skipped


def rerun(out, meta, builds, classes, extra_jars, work, runs, jobs, failures):
    """Runs each JVM of work again, runs times per build, with the same scenarios as the first time."""
    base = meta["base"]
    runs_todo = sorted({(jdk, cfg, laf, b, n) for jdk, cfg, laf, c in work for b in (base, c) for n in range(runs)})
    print(f"Confirming: running {len(work)} differing JVMs again, {len(runs_todo)} runs ...")

    def one(job):
        jdk, cfg, laf, b, n = job
        return java.run(java.java_exe(meta["jdks"][jdk]), meta["configs"][cfg], classes,
                        builds[b].jars() + extra_jars, "Runner", confirm_dir(out, jdk, cfg, b, laf) / str(n), laf,
                        meta.get("only"))

    with cf.ThreadPoolExecutor(jobs) as ex:
        failures += [e for e in ex.map(one, runs_todo) if e]
    (out / "confirm.json").write_text(json.dumps(work))


def confirm_dir(out, jdk, cfg, build, laf):
    return out / "confirm" / jdk / cfg / build / laf


def runs_of(out, jdk, cfg, build, laf, also):
    """[(results by scenario id, dir)] of every run of one build in one JVM setup: the reruns and those in also."""
    dirs = sorted(d for d in confirm_dir(out, jdk, cfg, build, laf).glob("*") if d.is_dir())
    dirs += [unit_dir(out, jdk, cfg, b, laf) for b in also]
    runs = []
    for d in dirs:
        r = load(d / "results.json")
        if r is not None:
            runs.append(({s["id"]: s for s in r.get("scenarios", [])}, d))
    return runs


def layout_of(step):
    comps = [{k: c.get(k) for k in ("p", "c", "b", "min", "pref", "hidden", "t")} for c in step.get("comps", [])]
    return step.get("w"), step.get("h"), comps, (step.get("error") or "").split("\n")[0]


def verdicts(out, meta):
    """{"jdk|cfg|laf|scenario|candidate": {"steps": {i: "noise" | "regression"}, "pixelsVary": [i...], runs}}."""
    base = meta["base"]
    result = {}
    for jdk, cfg, laf, c in load(out / "confirm.json") or []:
        refs = runs_of(out, jdk, cfg, base, laf, [base, base + AGAIN])
        cands = runs_of(out, jdk, cfg, c, laf, [c])
        sids = {sid for runs, _ in refs + cands for sid in runs}
        for sid in sids:
            ref_steps = [((r.get(sid) or {}).get("steps", []), d) for r, d in refs]
            cand_steps = [((r.get(sid) or {}).get("steps", []), d) for r, d in cands]
            steps, vary = {}, []
            for i in range(max(len(s) for s, _ in ref_steps + cand_steps)):
                rs = [(s[i], d) for s, d in ref_steps if i < len(s)]
                cs = [(s[i], d) for s, d in cand_steps if i < len(s)]
                if len(rs) < len(ref_steps) or len(cs) < len(cand_steps) or not rs:
                    steps[str(i)] = "regression"  # a run that did not get this far
                    continue
                consistent = all(compare_step(rs[0][0], x, rs[0][1], xd)[0] == "same" for x, xd in rs[1:])
                if not consistent:
                    vary.append(i)

                def matches(b, bd):
                    return any((compare_step(a, b, ad, bd)[0] == "same") if consistent else layout_of(a) == layout_of(b)
                               for a, ad in rs)

                steps[str(i)] = "noise" if all(matches(b, bd) for b, bd in cs) else "regression"
            result[f"{jdk}|{cfg}|{laf}|{sid}|{c}"] = {"steps": steps, "pixelsVary": vary,
                                                      "baseRuns": len(refs), "candRuns": len(cands)}
    return result
