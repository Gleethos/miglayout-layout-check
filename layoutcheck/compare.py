"""Comparing what two builds did with the same GUI: bounds, container sizes, errors and pixels.

A run directory holds results/<jdk>/<config>/<build>/<laf>/results.json, written by layoutcheck.Runner, with a
screenshot per step next to it. Every step of every scenario is compared with the same step of the base build.
"""
import concurrent.futures as cf
import json
from pathlib import Path

from PIL import Image, ImageChops

# From least to most severe. "noise" is a difference the base build also gives when it runs again.
STATUSES = ["same", "noise", "pixels", "sizes", "layout", "missing", "error"]
# Painted where one screenshot is smaller than the other, so a size change shows in the delta.
PAD = (255, 0, 255)


def load(path):
    try:
        return json.loads(Path(path).read_text(encoding="utf-8"))
    except FileNotFoundError:
        return None


def unit_dir(out, jdk, cfg, build, laf):
    return Path(out) / "results" / jdk / cfg / build / laf


def first_line(text):
    return (text or "").split("\n")[0]


def pixel_delta(a_png, b_png, delta_png=None):
    """Compares two screenshots and writes their delta |a - b| per channel, if asked to.

    Returns None when they are identical, else {"count": pixels that differ, "box": [x0, y0, x1, y1],
    "max": the largest difference in one channel, 1..255} plus both sizes when the sizes differ."""
    a, b = Image.open(a_png).convert("RGB"), Image.open(b_png).convert("RGB")
    sized = a.size != b.size
    if sized:
        w, h = max(a.width, b.width), max(a.height, b.height)
        pa, pb = Image.new("RGB", (w, h), PAD), Image.new("RGB", (w, h), PAD)
        pa.paste(a)
        pb.paste(b)
        a, b = pa, pb
    delta = ImageChops.difference(a, b)
    if delta_png:
        Path(delta_png).parent.mkdir(parents=True, exist_ok=True)
        delta.save(delta_png)
    box = delta.getbbox()
    if box is None:
        return None
    r, g, bl = delta.split()
    largest = ImageChops.lighter(ImageChops.lighter(r, g), bl)
    hist = largest.histogram()
    result = {"count": a.width * a.height - hist[0], "box": list(box), "max": max(i for i, n in enumerate(hist) if n)}
    if sized:
        result["sizes"] = [list(Image.open(a_png).size), list(Image.open(b_png).size)]
    return result


def path_key(p):
    return [int(x) if x.isdigit() else -1 for x in p.split("/")]


def compare_step(ref, cand, ref_dir, cand_dir, delta_png=None):
    """Returns (status, details) for one step; ref or cand is None when that build did not get this far."""
    if ref is None or cand is None:
        return "missing", {"note": "Only the base build got this far." if cand is None
                           else "Only this build got this far."}
    details = {}
    if bool(ref.get("error")) != bool(cand.get("error")) or first_line(ref.get("error")) != first_line(cand.get("error")):
        details["error"] = {"ref": ref.get("error"), "cand": cand.get("error")}
    diffs = []
    if (ref.get("w"), ref.get("h")) != (cand.get("w"), cand.get("h")):
        diffs.append({"p": "root", "c": "(window)", "what": "size", "ref": [ref.get("w"), ref.get("h")],
                      "cand": [cand.get("w"), cand.get("h")]})
    rc = {c["p"]: c for c in ref.get("comps", [])}
    cc = {c["p"]: c for c in cand.get("comps", [])}
    for p in sorted(set(rc) | set(cc), key=path_key):
        a, b = rc.get(p), cc.get(p)
        if a is None or b is None:
            diffs.append({"p": p, "c": (a or b)["c"], "what": "only in " + ("this build" if a is None else "the base")})
            continue
        for key in ("c", "b", "min", "pref", "hidden", "t"):
            if a.get(key) != b.get(key):
                diffs.append({"p": p, "c": a["c"], "t": a.get("t"), "n": a.get("n"), "what": key,
                              "ref": a.get(key), "cand": b.get(key)})
    if diffs:
        details["comps"] = diffs
    if ref.get("png") and cand.get("png"):
        pix = pixel_delta(ref_dir / ref["png"], cand_dir / cand["png"], delta_png)
        if pix:
            details["pixels"] = pix
    elif ref.get("paintError") or cand.get("paintError"):
        if first_line(ref.get("paintError")) != first_line(cand.get("paintError")):
            details["error"] = {"ref": ref.get("paintError"), "cand": cand.get("paintError")}
    if "error" in details:
        return "error", details
    if any(d["what"] not in ("min", "pref") for d in diffs):
        return "layout", details
    if diffs:
        return "sizes", details
    if "pixels" in details:
        return "pixels", details
    return "same", details


def compare_scenario(s, cs, ref_dir, cand_dir, delta_dir=None):
    """[(status, details)] for every step of scenario s (base) and cs (candidate, or None)."""
    rsteps, csteps = s.get("steps", []), (cs or {}).get("steps", [])
    out = []
    for i in range(max(len(rsteps), len(csteps))):
        a = rsteps[i] if i < len(rsteps) else None
        b = csteps[i] if i < len(csteps) else None
        delta = delta_dir / f"{i:02d}.png" if delta_dir else None
        status, det = compare_step(a, b, ref_dir, cand_dir, delta)
        if status == "missing" and (s.get("error") or (cs or {}).get("error")):
            det["error"] = {"ref": s.get("error"), "cand": (cs or {}).get("error") if cs else "(no result)"}
        out.append((status, det))
    return out


def compare_unit(out, jdk, cfg, laf, base, cand, deltas):
    """Compares every scenario one candidate laid out in one JDK, config and look and feel with the base build."""
    rdir, cdir = unit_dir(out, jdk, cfg, base, laf), unit_dir(out, jdk, cfg, cand, laf)
    rres, cres = load(rdir / "results.json"), load(cdir / "results.json")
    if rres is None:
        return {}
    cidx = {s["id"]: s for s in (cres or {}).get("scenarios", [])}
    result = {}
    for s in rres["scenarios"]:
        ddir = Path(out) / "deltas" / jdk / cfg / cand / laf / s["id"] if deltas else None
        result[s["id"]] = compare_scenario(s, cidx.get(s["id"]), rdir, cdir, ddir)
    return result


def compare_all(out, meta, candidates, jobs, deltas=True):
    """{(jdk, cfg, laf, candidate): {scenario id: [(status, details) per step]}}, compared in parallel."""
    units = [(jdk, cfg, laf, c) for jdk in meta["jdks"] for cfg in meta["configs"] for laf in meta["lafs"]
             for c in candidates]
    with cf.ProcessPoolExecutor(jobs) as ex:
        futures = {u: ex.submit(compare_unit, str(out), u[0], u[1], u[2], meta["base"], u[3], deltas) for u in units}
        return {u: f.result() for u, f in futures.items()}


def scenario_infos(out, meta):
    """{id: {group, title, description, steps: [labels]}} of every scenario the base build laid out."""
    infos = {}
    for jdk in meta["jdks"]:
        for cfg in meta["configs"]:
            for laf in meta["lafs"]:
                r = load(unit_dir(out, jdk, cfg, meta["base"], laf) / "results.json")
                for s in (r or {}).get("scenarios", []):
                    info = infos.setdefault(s["id"], {"group": s.get("group"), "title": s.get("title"),
                                                      "description": s.get("description"), "steps": []})
                    labels = [st["label"] for st in s.get("steps", [])]
                    if len(labels) > len(info["steps"]):
                        info["steps"] = labels
    return infos
