#!/usr/bin/env python3
"""Package v1.0 sources plus a portable Git bundle retaining the required history."""
import argparse
import io
from pathlib import Path
import re
import subprocess
import tempfile
import zipfile

ROOT = Path(__file__).resolve().parents[1]
parser = argparse.ArgumentParser()
parser.add_argument('identity', nargs='*', help='Name Surname Group; defaults to GitHub username saloverni')
args = parser.parse_args()
identity = '_'.join(args.identity or ['saloverni'])
if not re.fullmatch(r'[\w-]+', identity):
    parser.error('Use letters, digits, underscores or hyphens for name, surname and group')
branches = ['main', 'feature/dsu', 'feature/mst', 'feature/scc', 'feature/paths', 'feature/metrics']

def git(*args):
    return subprocess.check_output(['git', '-C', str(ROOT), *args])

release = git('rev-parse', 'v1.0^{commit}').strip()
if git('rev-parse', 'main').strip() != release:
    raise SystemExit('Tag v1.0 must point to main before packaging')
for branch in branches:
    subprocess.run(['git', '-C', str(ROOT), 'merge-base', '--is-ancestor', branch, 'main'], check=True)
output = ROOT / 'dist' / f'DAA_Assignment3_{identity}.zip'
output.parent.mkdir(exist_ok=True)
source = git('archive', '--format=zip', '--prefix=daa-assignment-3/', 'v1.0')
with tempfile.TemporaryDirectory() as temporary:
    bundle = Path(temporary) / 'git-history.bundle'
    git('bundle', 'create', str(bundle), *branches, 'v1.0')
    git('bundle', 'verify', str(bundle))
    with zipfile.ZipFile(io.BytesIO(source)) as archive, zipfile.ZipFile(output, 'w', zipfile.ZIP_DEFLATED) as target:
        for entry in archive.infolist():
            target.writestr(entry, archive.read(entry))
        target.write(bundle, 'daa-assignment-3/git-history.bundle')
        target.writestr('daa-assignment-3/GIT_HISTORY.txt',
                        'Repository: https://github.com/saloverni/daa-assignment-3\n'
                        'Release: main, tag v1.0\n\n'
                        'Restore all local feature branches and the release tag:\n'
                        'git clone -b main git-history.bundle history\n'
                        'cd history\n'
                        'for branch in dsu mst scc paths metrics; do\n'
                        '  git branch --track feature/$branch origin/feature/$branch\n'
                        'done\n'
                        'git log --graph --oneline --all --decorate\n')
print(output)
