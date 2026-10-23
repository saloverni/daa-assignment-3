#!/usr/bin/env python3
"""Regenerate the assignment's three figures and numerical analysis from the CSV."""
import csv
import math
from pathlib import Path
from statistics import mean

import matplotlib
matplotlib.use('Agg')
import matplotlib.pyplot as plt

ROOT = Path(__file__).resolve().parents[1]
OUT = ROOT / 'results'
PLOTS = OUT / 'plots'
PLOTS.mkdir(parents=True, exist_ok=True)
ALGORITHMS = ['Kruskal', 'Prim', 'Kosaraju', 'Kahn', 'DagShortestPath']
COLORS = ['#2457A7', '#D77A15', '#16836B', '#9B4192', '#CE4444']
MARKERS = ['o', 's', '^', 'D', 'x']
with (OUT / 'results.csv').open() as stream:
    rows = list(csv.DictReader(stream))
if len(rows) != 50:
    raise ValueError(f'Expected 50 rows, got {len(rows)}')
for row in rows:
    for key in ['n', 'm', 'dsu_ops', 'edge_scans', 'comparisons', 'max_depth', 'result']:
        row[key] = int(row[key])
    row['time_ms'] = float(row['time_ms'])
    row['operations'] = row['dsu_ops'] + row['edge_scans'] + row['comparisons']
    row['density'] = row['graph'].rsplit('_', 1)[1]

def series(algorithm, density):
    return sorted((r for r in rows if r['algorithm'] == algorithm and r['density'] == density), key=lambda r: r['n'])

def slope(points, field):
    x = [math.log(r['n']) for r in points]
    y = [math.log(r[field]) for r in points]
    return sum((a - mean(x)) * (b - mean(y)) for a, b in zip(x, y)) / sum((a - mean(x)) ** 2 for a in x)

plt.rcParams.update({'font.family': 'DejaVu Sans', 'font.size': 10, 'axes.spines.top': False,
                     'axes.spines.right': False, 'figure.dpi': 150, 'savefig.facecolor': 'white'})
fig, axes = plt.subplots(1, 2, figsize=(11, 4.4), layout='constrained')
for axis, density in zip(axes, ['sparse', 'dense']):
    for algorithm, color, marker in zip(ALGORITHMS, COLORS, MARKERS):
        points = series(algorithm, density)
        axis.loglog([r['n'] for r in points], [r['operations'] for r in points],
                    label=algorithm, color=color, marker=marker, linewidth=1.6,
                    linestyle='--' if algorithm == 'DagShortestPath' else '-')
    axis.set(title=f'{density.capitalize()}: m = {4 if density == "sparse" else 20}n', xlabel='Vertices n', ylabel='Counted operations')
    axis.grid(True, which='both', alpha=.18)
axes[0].legend(fontsize=8, loc='upper left')
fig.suptitle('Measured work: dsu_ops + edge_scans + comparisons')
fig.savefig(PLOTS / 'operations_vs_n.png')
plt.close(fig)

fig, axes = plt.subplots(2, 3, figsize=(11, 5.4), layout='constrained')
for axis, algorithm in zip(axes.flat, ALGORITHMS):
    for density, color, marker in [('sparse', '#2457A7', 'o'), ('dense', '#D77A15', 'x')]:
        points = series(algorithm, density)
        axis.semilogx([r['n'] for r in points], [r['edge_scans'] / r['m'] for r in points],
                      color=color, marker=marker, linestyle='-' if density == 'sparse' else '--', label=density)
    axis.set(title=algorithm, xlabel='Vertices n', ylabel='Scans / m', ylim=(0, 2.25))
    axis.grid(True, alpha=.18)
axes.flat[-1].axis('off')
axes.flat[-1].text(.04, .75, 'Exact scan ratios\n\nPrim / Kosaraju: 2\nKahn / DAG relaxation: 1\nKruskal: early stopping\n\nOverlapping curves are intentional.', va='top', fontsize=11)
axes.flat[0].legend(fontsize=9)
fig.suptitle('Scans per input edge, separated by algorithm')
fig.savefig(PLOTS / 'scans_per_edge.png')
plt.close(fig)

fig, axis = plt.subplots(figsize=(8.5, 3.8), layout='constrained')
for density, color, marker in [('sparse', '#2457A7', 'o'), ('dense', '#D77A15', 's')]:
    points = series('Kruskal', density)
    axis.semilogx([r['n'] for r in points], [r['edge_scans'] / r['m'] for r in points],
                  color=color, marker=marker, label=f'{density}: m = {4 if density == "sparse" else 20}n', linewidth=2)
axis.set(xlabel='Vertices n', ylabel='Examined logical edges / m', ylim=(0, 1.08),
         title='Kruskal stops after accepting n - 1 edges; sorting still touches all m')
axis.grid(True, alpha=.2)
axis.legend()
fig.savefig(PLOTS / 'kruskal_edge_share.png')
plt.close(fig)

lines = ['# Generated measurements', '', 'Generated from `results.csv` by `scripts/plot_results.py`.', '',
         '## Least-squares log-log slopes against n', '',
         '| Algorithm | Density | Edge scans | Comparisons | Total counted work |',
         '|---|---|---:|---:|---:|']
for algorithm in ALGORITHMS:
    for density in ['sparse', 'dense']:
        points = series(algorithm, density)
        comparison = f'{slope(points, "comparisons"):.4f}' if all(r['comparisons'] for r in points) else 'n/a'
        lines.append(f'| {algorithm} | {density} | {slope(points, "edge_scans"):.4f} | {comparison} | {slope(points, "operations"):.4f} |')
lines += ['', '## Counter / formula ratios', '',
          'K = Kruskal, P = Prim, S = Kosaraju, T = Kahn, D = DAG relaxation; C = comparisons.', '',
          '| n | m/n | K: C/(m log2 m) | K: DSU/m | P: C/(m log2 m) | S: scans/(n+m) | T, D: scans/(n+m) |',
          '|---:|---:|---:|---:|---:|---:|---:|']
for n in sorted({r['n'] for r in rows}):
    for density in ['sparse', 'dense']:
        group = {r['algorithm']: r for r in rows if r['n'] == n and r['density'] == density}
        k, p, s, t = [group[a] for a in ['Kruskal', 'Prim', 'Kosaraju', 'Kahn']]
        m = k['m']
        lines.append(f'| {n} | {m // n} | {k["comparisons"] / (m * math.log2(m)):.4f} | {k["dsu_ops"] / m:.4f} | {p["comparisons"] / (m * math.log2(m)):.4f} | {s["edge_scans"] / (n + m):.4f} | {t["edge_scans"] / (n + m):.4f} |')
lines += ['', '## Kruskal examined-edge share', '', '| n | Sparse | Dense |', '|---:|---:|---:|']
for a, b in zip(series('Kruskal', 'sparse'), series('Kruskal', 'dense')):
    lines.append(f'| {a["n"]} | {a["edge_scans"] / a["m"]:.4f} | {b["edge_scans"] / b["m"]:.4f} |')
(OUT / 'analysis.md').write_text('\n'.join(lines) + '\n')
print('Generated three PNG plots and results/analysis.md')
