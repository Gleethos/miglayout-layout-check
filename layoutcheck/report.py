"""The report: index.html and data.js in the run directory, and a summary on the console."""
import json
import shutil
from pathlib import Path

from .compare import STATUSES, load
from .confirm import AGAIN

TEMPLATE = Path(__file__).resolve().parent / "report.html"
CODES = {"same": "s", "noise": "n", "pixels": "p", "sizes": "z", "layout": "l", "missing": "m", "error": "e"}
DIFFERENT = ("pixels", "sizes", "layout", "missing", "error")


def classify(comparisons, verdicts, meta):
    """Turns differences into noise where the base build gives them too, and notes why on each step."""
    base = meta["base"]
    for (jdk, cfg, laf, cand), scenarios in comparisons.items():
        again = comparisons.get((jdk, cfg, laf, base + AGAIN), {})
        for sid, steps in scenarios.items():
            v = verdicts.get(f"{jdk}|{cfg}|{laf}|{sid}|{cand}")
            base_again = again.get(sid, [])
            for i, (status, det) in enumerate(steps):
                if status == "same" or cand == base + AGAIN:
                    continue
                verdict = v and v["steps"].get(str(i))
                vary = v and i in v["pixelsVary"]
                base_varies = i < len(base_again) and base_again[i][0] != "same"
                if verdict:
                    runs = f"{v['baseRuns']} runs of the base build and {v['candRuns']} of this one"
                    only = " Only bounds, sizes and errors were compared: the base build's own pixels vary between runs." if vary else ""
                    if verdict == "noise":
                        status = "noise"
                        det["note"] = f"Noise: in {runs}, the base build also gave this result.{only}"
                    else:
                        det["note"] = f"Confirmed: differs from every result of the base build in {runs}.{only}"
                elif base_varies:
                    status = "noise"
                    det["note"] = "Not confirmed: the base build itself gave two different results here."
                elif cand in meta.get("unconfirmed", []):
                    det["note"] = "Not confirmed: too many differences to lay them out again (--confirm-limit)."
                steps[i] = (status, det)


def candidates_of(meta):
    cands = [b for b in meta["builds"] if b != meta["base"]]
    if meta.get("again"):
        cands.append(meta["base"] + AGAIN)
    return cands


def write(out, meta, comparisons, verdicts, infos):
    """Writes out/index.html and out/data.js; returns the totals per candidate."""
    classify(comparisons, verdicts, meta)
    cands = candidates_of(meta)
    combos = [(jdk, cfg, laf) for jdk in meta["jdks"] for cfg in meta["configs"] for laf in meta["lafs"]]
    sids = list(infos)
    totals = {c: dict.fromkeys(STATUSES, 0) for c in cands}
    status, details = {}, {}
    for c in cands:
        per_combo = []
        for ci, (jdk, cfg, laf) in enumerate(combos):
            scen = comparisons.get((jdk, cfg, laf, c), {})
            codes = []
            for si, sid in enumerate(sids):
                steps = scen.get(sid, [])
                codes.append("".join(CODES[st] for st, _ in steps))
                for i, (st, det) in enumerate(steps):
                    totals[c][st] += 1
                    if det:
                        details[f"{c}|{ci}|{si}|{i}"] = det
            per_combo.append(codes)
        status[c] = per_combo

    probe = {}
    for f in sorted((out / "probe").glob("*/*.json")):
        build, laf = f.stem.split("--")
        probe.setdefault(build, {})[f"{f.parent.name} | {laf}"] = load(f)

    data = {"meta": meta, "candidates": cands, "combos": combos,
            "scenarios": [{"id": sid, **infos[sid]} for sid in sids],
            "status": status, "details": details, "totals": totals, "probe": probe}
    shutil.copyfile(TEMPLATE, out / "index.html")
    (out / "data.js").write_text("window.REPORT = " + json.dumps(data, separators=(",", ":")) + ";\n", encoding="utf-8")
    return totals


def summary(out, meta, totals):
    """The console summary, and whether a candidate differs."""
    lines = [f"Base: {meta['base']} ({meta['builds'][meta['base']]['subject']})"]
    differs = False
    for c, t in totals.items():
        n = sum(t.values())
        bad = sum(t[s] for s in DIFFERENT)
        parts = ", ".join(f"{t[s]} {s}" for s in STATUSES[1:] if t[s])
        if c.endswith(AGAIN):
            verdict = "the base build varies by itself in these steps" if bad else "the base build is deterministic"
            lines.append(f"  {'(base again)':22} {n} steps{': ' + parts if parts else ''}  => {verdict}")
            continue
        differs |= bad > 0
        lines.append(f"  {c:22} {n} steps{': ' + parts if parts else ', all identical'}  => "
                     + ("DIFFERENCES FOUND" if bad else "NO DIFFERENCES"))
    if meta.get("failures"):
        lines.append(f"  {len(meta['failures'])} jobs failed; see the report")
    lines.append(f"Report: {out / 'index.html'}")
    return "\n".join(lines), differs
