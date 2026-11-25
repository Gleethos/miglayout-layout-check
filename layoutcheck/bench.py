"""layout-bench: how long does one MigLayout layout pass take with each build?

Runs MigLayout's own LayoutPassBenchmark (swing/src/test/java/net/miginfocom/swing/trial) once with all builds in
one JVM, in random order over many rounds, and prints the median time of a pass and how often getBaseline is called.
It opens a window while it runs. The first build is the one the others are compared with.
"""
import argparse
import os
import re
import subprocess
import sys
from pathlib import Path

from . import java, sources
from .cli import CACHE, name_builds

BENCHMARK = "swing/src/test/java/net/miginfocom/swing/trial/LayoutPassBenchmark.java"


def main(argv=None):
    ap = argparse.ArgumentParser(prog="layout-bench", description=__doc__,
                                 formatter_class=argparse.RawDescriptionHelpFormatter)
    ap.add_argument("refs", nargs="+", metavar="[NAME=]REF", help="git refs or commit ids, the base build first")
    ap.add_argument("--repo", help="MigLayout repository, as for layout-check")
    ap.add_argument("--benchmark-from", metavar="REF",
                    help="the ref to take LayoutPassBenchmark.java from (default: the last ref that has it)")
    ap.add_argument("--plain", action="store_true",
                    help="time plain Swing components: drop the benchmark's call-counting getBaseline overrides "
                         "(and their opt-in to baseline caching), so getBaseline counts show 0")
    ap.add_argument("--jdk", metavar="JAVA_HOME", help="the JDK to run on (default: java on the PATH)")
    ap.add_argument("--rounds", type=int, help="rounds to time (default: the benchmark's, 40)")
    args = ap.parse_args(argv)
    try:
        repo, fetchable = sources.find_repo(args.repo, CACHE)
        builds = []
        for name, ref in name_builds(args.refs):
            builds.append(sources.build(repo, name, ref, sources.resolve(repo, ref, fetchable), CACHE))
        source = benchmark_source(repo, args.benchmark_from, [b.sha for b in reversed(builds)])
    except sources.SourceError as e:
        print(f"layout-bench: {e}", file=sys.stderr)
        return 2
    if args.plain:
        source = re.sub(r"\n[ \t]*@Override public int getBaseline\(int w, int h\) \{ return counted\(super\.getBaseline\(w, h\)\); \}", "", source)
        source = re.sub(r" ?putClientProperty\(\"migLayout\.baselineCacheable\", Boolean\.TRUE\);", "", source)
    dest = CACHE / "bench" / ("plain" if args.plain else "counting") / BENCHMARK.rsplit("/", 1)[1]
    dest.parent.mkdir(parents=True, exist_ok=True)
    dest.write_text(source, encoding="utf-8")
    cmd = [java.java_exe(args.jdk or "")]
    if args.rounds:
        cmd.append(f"-Dbench.rounds={args.rounds}")
    cmd += [str(dest), *[f"{b.name}={os.pathsep.join(map(str, b.jars()))}" for b in builds]]
    return subprocess.run(cmd).returncode


def benchmark_source(repo, ref, shas):
    for candidate in ([ref] if ref else shas):
        p = sources.git(repo, "show", f"{candidate}:{BENCHMARK}", check=False)
        if p.returncode == 0:
            return p.stdout.replace("\r\n", "\n")
    raise sources.SourceError(f"No {BENCHMARK} in {ref or 'any of the builds'}")


if __name__ == "__main__":
    sys.exit(main())
