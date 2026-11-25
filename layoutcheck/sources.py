"""Where the code under test comes from: a MigLayout git repository, the commits to build, and FlatLaf."""
import hashlib
import re
import shutil
import subprocess
import tempfile
import urllib.request
from dataclasses import dataclass
from pathlib import Path

UPSTREAM = "https://github.com/mikaelgrev/miglayout.git"
FLATLAF_VERSION = "3.6.1"
FLATLAF_SHA256 = "687be33e1e3eface1681fd194732d16e008126a47b6116b4301e7070c918c4c9"
FLATLAF_URL = f"https://repo1.maven.org/maven2/com/formdev/flatlaf/{FLATLAF_VERSION}/flatlaf-{FLATLAF_VERSION}.jar"
MIGLAYOUT_MARKER = "swing/src/main/java/net/miginfocom/swing/MigLayout.java"


from .errors import TEXT, CheckError


class SourceError(CheckError):
    """A repository, a ref or a build that cannot be used; the message says what to do."""


@dataclass
class Build:
    """MigLayout's core and Swing jars built from one commit."""
    name: str
    ref: str
    sha: str
    subject: str
    dir: Path

    def jars(self):
        return [self.dir / "core.jar", self.dir / "swing.jar"]


def git(repo, *args, check=True):
    p = subprocess.run(["git", "-C", str(repo), *args], capture_output=True, **TEXT)
    if check and p.returncode != 0:
        raise SourceError(f"git {' '.join(args)} failed in {repo}:\n{p.stderr.strip()}")
    return p


def find_repo(spec, cache):
    """The repository to build from, and whether it is a cached clone that may be fetched.

    spec is a local path, a git URL, or None: then the MigLayout checkout the command runs in, else a clone of
    upstream MigLayout kept in the cache."""
    if spec is None:
        top = git(Path.cwd(), "rev-parse", "--show-toplevel", check=False)
        if top.returncode == 0 and (Path(top.stdout.strip()) / MIGLAYOUT_MARKER).exists():
            return Path(top.stdout.strip()), False
        spec = UPSTREAM
    if "://" in spec or spec.startswith("git@"):
        slug = re.sub(r"[^A-Za-z0-9]+", "-", spec).strip("-")[-60:]
        clone = cache / "repos" / slug
        if not (clone / "HEAD").exists():
            print(f"Cloning {spec} ...")
            clone.parent.mkdir(parents=True, exist_ok=True)
            p = subprocess.run(["git", "clone", "-q", "--mirror", spec, str(clone)], capture_output=True, **TEXT)
            if p.returncode != 0:
                shutil.rmtree(clone, ignore_errors=True)
                raise SourceError(f"Cannot clone {spec}:\n{p.stderr.strip()}")
        else:
            print(f"Fetching {spec} ...")  # so that a branch name means its newest commit
            git(clone, "fetch", "-q", "--prune", "origin")
        return clone, True
    repo = Path(spec).expanduser().resolve()
    if git(repo, "rev-parse", "--git-dir", check=False).returncode != 0:
        raise SourceError(f"{repo} is not a git repository")
    if not git(repo, "cat-file", "-e", f"HEAD:{MIGLAYOUT_MARKER}", check=False).returncode == 0 \
            and not (repo / MIGLAYOUT_MARKER).exists():
        raise SourceError(f"{repo} does not look like MigLayout (no {MIGLAYOUT_MARKER})")
    return repo, False


def resolve(repo, ref, fetchable):
    """The full commit id of ref (a cached clone is fetched when it is opened)."""
    p = git(repo, "rev-parse", "--verify", "--quiet", f"{ref}^{{commit}}", check=False)
    if p.returncode != 0:
        hint = "" if fetchable else " (fetch it first, or pass --repo with a URL that has it)"
        raise SourceError(f"{ref} is not a commit in {repo}{hint}")
    return p.stdout.strip()


def build(repo, name, ref, sha, cache):
    """Builds core.jar and swing.jar of one commit, or takes them from the cache (cache/builds/<sha>/)."""
    dest = cache / "builds" / sha
    subject = git(repo, "log", "-1", "--format=%h %s", sha).stdout.strip()
    if (dest / "core.jar").exists() and (dest / "swing.jar").exists():
        print(f"[{name}] {subject} (built before)")
        return Build(name, ref, sha, subject, dest)
    print(f"[{name}] building {subject} ...")
    (cache / "tmp").mkdir(parents=True, exist_ok=True)
    work = Path(tempfile.mkdtemp(prefix="build-", dir=cache / "tmp"))  # on the same file system as the builds
    tree = work / "src"
    git(repo, "worktree", "add", "-q", "--detach", str(tree), sha)
    try:
        maven = ["mvn", "-q", "-B", "-pl", "core,swing", "-am", "package",
                 "-Dmaven.test.skip=true", "-Dmaven.javadoc.skip=true", "-Dgpg.skip=true", "-Dmaven.source.skip=true"]
        # Offline first: quick when the dependencies are in the local Maven repository already.
        if subprocess.run(maven + ["-o"], cwd=tree, capture_output=True).returncode != 0:
            p = subprocess.run(maven, cwd=tree, capture_output=True, **TEXT)
            if p.returncode != 0:
                raise SourceError(f"Maven could not build {subject}:\n{(p.stdout + p.stderr)[-4000:]}")
        # Into a directory of its own first, so that another check building the same commit never sees half of it.
        part = work / "jars"
        part.mkdir()
        shutil.copyfile(main_jar(tree / "core" / "target"), part / "core.jar")
        shutil.copyfile(main_jar(tree / "swing" / "target"), part / "swing.jar")
        dest.parent.mkdir(parents=True, exist_ok=True)
        if not dest.exists():
            try:
                part.rename(dest)
            except OSError:
                if not (dest / "swing.jar").exists():
                    raise
    finally:
        git(repo, "worktree", "remove", "--force", str(tree), check=False)
        shutil.rmtree(work, ignore_errors=True)
    return Build(name, ref, sha, subject, dest)


def main_jar(target):
    jars = [j for j in target.glob("miglayout-*.jar") if not re.search(r"-(sources|javadoc|tests)\.jar$", j.name)]
    if len(jars) != 1:
        raise SourceError(f"Expected one jar in {target}, found {[j.name for j in jars]}")
    return jars[0]


def flatlaf(cache):
    """FlatLaf from Maven Central, checked against its known SHA-256."""
    jar = cache / "lib" / f"flatlaf-{FLATLAF_VERSION}.jar"
    if not jar.exists():
        print(f"Downloading FlatLaf {FLATLAF_VERSION} ...")
        data = urllib.request.urlopen(FLATLAF_URL, timeout=60).read()
        if hashlib.sha256(data).hexdigest() != FLATLAF_SHA256:
            raise SourceError(f"{FLATLAF_URL} does not have the expected SHA-256 {FLATLAF_SHA256}")
        jar.parent.mkdir(parents=True, exist_ok=True)
        jar.write_bytes(data)
    return jar
