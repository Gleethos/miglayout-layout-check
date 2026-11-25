"""Telling regressions from noise.

Some GUIs do not come out the same in every JVM: GTK's text antialiasing, for one, varies from run to run. So every
scenario in which a candidate differs from the base build is laid out again in fresh JVMs, a few times with each of
the two builds. A step counts as a regression only when the candidate's result differs from every result of the base
build. Where the base build does not even agree with itself on the pixels, only bounds, sizes and errors are compared.
"""
import concurrent.futures as cf
import json
import re

from .compare import compare_step, load, unit_dir
from . import java

AGAIN = "~again"  # the base build's second run, which measures what varies by itself


def todo(comparisons, meta, limit):
    """The (jdk, cfg, laf, scenario, candidate) to confirm, and the candidates with too many to confirm."""
    base = meta["base"]
    found, skipped = [], []
    for c in meta["builds"]:
        if c == base:
            continue
        mine = [(jdk, cfg, laf, sid, c) for (jdk, cfg, laf, cand), scen in comparisons.items() if cand == c
                for sid, steps in scen.items() if any(st != "same" for st, _ in steps)]
        if len(mine) > limit:
            print(f"  {c}: {len(mine)} scenarios differ, more than --confirm-limit {limit}; not confirming them")
            skipped.append(c)
        else:
            found += mine
    return sorted(found), skipped


def rerun(out, meta, builds, classes, extra_jars, work, runs, jobs, failures):
    """Lays out each scenario of work again, runs times per build, each in a fresh JVM."""
    base = meta["base"]
    runs_todo = sorted({(jdk, cfg, laf, sid, b, n) for jdk, cfg, laf, sid, c in work for b in (base, c)
                        for n in range(runs)})
    print(f"Confirming {len(work)} differing scenarios with {len(runs_todo)} fresh runs ...")

    def one(job):
        jdk, cfg, laf, sid, b, n = job
        target = confirm_dir(out, jdk, cfg, b, laf, sid) / str(n)
        return java.run(java.java_exe(meta["jdks"][jdk]), meta["configs"][cfg], classes,
                        builds[b].jars() + extra_jars, "Runner", target, laf, "^" + re.escape(sid) + "$", timeout=600)

    with cf.ThreadPoolExecutor(jobs) as ex:
        failures += [e for e in ex.map(one, runs_todo) if e]
    (out / "confirm.json").write_text(json.dumps(work))


def confirm_dir(out, jdk, cfg, build, laf, sid):
    return out / "confirm" / jdk / cfg / build / laf / sid


def runs_of(out, jdk, cfg, build, laf, sid, also):
    """[(steps, dir)] of every run of one scenario by one build: the fresh ones and those in also."""
    dirs = sorted(d for d in confirm_dir(out, jdk, cfg, build, laf, sid).glob("*") if d.is_dir())
    dirs += [unit_dir(out, jdk, cfg, b, laf) for b in also]
    runs = []
    for d in dirs:
        for s in (load(d / "results.json") or {}).get("scenarios", []):
            if s["id"] == sid:
                runs.append((s.get("steps", []), d))
    return runs


def layout_of(step):
    comps = [{k: c.get(k) for k in ("p", "c", "b", "min", "pref", "hidden", "t")} for c in step.get("comps", [])]
    return step.get("w"), step.get("h"), comps, (step.get("error") or "").split("\n")[0]


def verdicts(out, meta):
    """{"jdk|cfg|laf|scenario|candidate": {"steps": {i: "noise" | "regression"}, "pixelsVary": [i...], runs}}."""
    base = meta["base"]
    result = {}
    for jdk, cfg, laf, sid, c in load(out / "confirm.json") or []:
        refs = runs_of(out, jdk, cfg, base, laf, sid, [base, base + AGAIN])
        cands = runs_of(out, jdk, cfg, c, laf, sid, [c])
        steps, vary = {}, []
        for i in range(max([len(s) for s, _ in refs + cands] or [0])):
            ref_steps = [(s[i], d) for s, d in refs if i < len(s)]
            cand_steps = [(s[i], d) for s, d in cands if i < len(s)]
            if not ref_steps or not cand_steps:
                steps[str(i)] = "regression"
                continue
            consistent = all(compare_step(ref_steps[0][0], x, ref_steps[0][1], xd)[0] == "same" for x, xd in ref_steps[1:])
            if consistent:
                same = any(compare_step(a, b, ad, bd)[0] == "same" for a, ad in ref_steps for b, bd in cand_steps)
            else:
                vary.append(i)
                same = any(layout_of(a) == layout_of(b) for a, _ in ref_steps for b, _ in cand_steps)
            steps[str(i)] = "noise" if same else "regression"
        result[f"{jdk}|{cfg}|{laf}|{sid}|{c}"] = {"steps": steps, "pixelsVary": vary,
                                                  "baseRuns": len(refs), "candRuns": len(cands)}
    return result
