"""Compiling the GUI catalogue and running it in fresh JVMs."""
import os
import shutil
import subprocess
from pathlib import Path

from .errors import TEXT, CheckError

HERE = Path(__file__).resolve().parent.parent
SOURCES = HERE / "java" / "src"
# Font antialiasing as on a desktop, so the screenshots show what users see.
COMMON_FLAGS = ["-Dawt.useSystemAAFontSettings=on"]


def java_exe(home):
    """The java launcher of a JDK home, or the one on the PATH for an empty home."""
    if not home:
        return "java"
    exe = Path(home) / "bin" / ("java.exe" if os.name == "nt" else "java")
    if not exe.exists():
        raise CheckError(f"No {exe}")
    return str(exe)


def compile_catalogue(compile_jars, dest):
    """Compiles java/src against one build's jars into dest; the classes then run against any build."""
    shutil.rmtree(dest, ignore_errors=True)
    dest.mkdir(parents=True)
    srcs = [str(p) for p in sorted(SOURCES.rglob("*.java"))]
    p = subprocess.run(["javac", "--release", "21", "-nowarn", "-encoding", "UTF-8", "-d", str(dest),
                        "-cp", os.pathsep.join(map(str, compile_jars)), *srcs], capture_output=True, **TEXT)
    if p.returncode != 0:
        raise CheckError(f"Compiling the GUI catalogue failed (javac 21 or newer is needed):\n{p.stderr[-4000:]}")
    return dest


def classpath(classes, jars):
    return os.pathsep.join([str(classes), *map(str, jars)])


def supported_lafs(java, classes, jars):
    """The look and feels the JVM can create on this platform."""
    p = subprocess.run([java, "-cp", classpath(classes, jars), "layoutcheck.Lafs"], capture_output=True, **TEXT,
                       timeout=120)
    if p.returncode != 0:
        raise CheckError(f"Cannot list the look and feels:\n{p.stderr[-2000:]}")
    return p.stdout.split()


def run(java, flags, classes, jars, main_class, target, laf, only=None, timeout=1800):
    """Runs layoutcheck.Runner or layoutcheck.Probe; returns None, or what went wrong."""
    cmd = [java, *flags.split(), *COMMON_FLAGS, "-cp", classpath(classes, jars), f"layoutcheck.{main_class}",
           str(target), laf]
    if only:
        cmd.append(only)
    try:
        p = subprocess.run(cmd, capture_output=True, **TEXT, timeout=timeout)
    except subprocess.TimeoutExpired:
        return f"{main_class} {target} {laf}: timed out after {timeout}s"
    if p.returncode != 0:
        return f"{main_class} {target} {laf}: exit code {p.returncode}\n{p.stderr[-3000:]}"
    return None
