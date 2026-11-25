class CheckError(Exception):
    """Something that keeps the check from running, such as a bad option, ref or JDK; the message says what."""


# Output of git, javac, Maven and the JVMs, readable whatever the platform's own encoding is.
TEXT = {"text": True, "encoding": "utf-8", "errors": "replace"}
