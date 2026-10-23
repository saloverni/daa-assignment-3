# Generated measurements

Generated from `results.csv` by `scripts/plot_results.py`.

## Least-squares log-log slopes against n

| Algorithm | Density | Edge scans | Comparisons | Total counted work |
|---|---|---:|---:|---:|
| Kruskal | sparse | 1.0550 | 1.0545 | 1.0537 |
| Kruskal | dense | 1.1194 | 1.0159 | 1.0219 |
| Prim | sparse | 1.0000 | 1.1079 | 1.1007 |
| Prim | dense | 1.0000 | 1.0903 | 1.0850 |
| Kosaraju | sparse | 1.0000 | n/a | 1.0000 |
| Kosaraju | dense | 1.0000 | n/a | 1.0000 |
| Kahn | sparse | 1.0000 | n/a | 1.0000 |
| Kahn | dense | 1.0000 | n/a | 1.0000 |
| DagShortestPath | sparse | 1.0000 | 1.0000 | 1.0000 |
| DagShortestPath | dense | 1.0000 | 1.0000 | 1.0000 |

## Counter / formula ratios

K = Kruskal, P = Prim, S = Kosaraju, T = Kahn, D = DAG relaxation; C = comparisons.

| n | m/n | K: C/(m log2 m) | K: DSU/m | P: C/(m log2 m) | S: scans/(n+m) | T, D: scans/(n+m) |
|---:|---:|---:|---:|---:|---:|---:|
| 1000 | 4 | 0.8914 | 1.7908 | 1.8291 | 1.6000 | 0.8000 |
| 1000 | 20 | 0.9027 | 0.4802 | 1.8248 | 1.9048 | 0.9524 |
| 3162 | 4 | 0.9069 | 2.0191 | 1.8654 | 1.6000 | 0.8000 |
| 3162 | 20 | 0.8464 | 0.5063 | 1.8468 | 1.9048 | 0.9524 |
| 10000 | 4 | 0.8692 | 2.1818 | 1.8952 | 1.6000 | 0.8000 |
| 10000 | 20 | 0.7839 | 0.4647 | 1.8657 | 1.9048 | 0.9524 |
| 31623 | 4 | 0.8091 | 2.2317 | 1.9210 | 1.6000 | 0.8000 |
| 31623 | 20 | 0.7200 | 0.6540 | 1.8779 | 1.9048 | 0.9524 |
| 100000 | 4 | 0.7447 | 2.2483 | 1.9344 | 1.6000 | 0.8000 |
| 100000 | 20 | 0.6662 | 0.7936 | 1.8896 | 1.9048 | 0.9524 |

## Kruskal examined-edge share

| n | Sparse | Dense |
|---:|---:|---:|
| 1000 | 0.7705 | 0.2151 |
| 3162 | 0.8846 | 0.2281 |
| 10000 | 0.9659 | 0.2074 |
| 31623 | 0.9909 | 0.3020 |
| 100000 | 0.9992 | 0.3718 |
