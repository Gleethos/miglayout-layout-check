import sys

from .cli import main

sys.stdout.reconfigure(line_buffering=True)
sys.exit(main())
