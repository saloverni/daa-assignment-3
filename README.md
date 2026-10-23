# DAA Assignment 3 — City Network & Job Scheduler

Java 21 / Gradle implementation of DSU, Kruskal, Prim, iterative Kosaraju, Kahn and
multi-source DAG shortest paths. No graph-algorithm library is used.

**Repository:** https://github.com/saloverni/daa-assignment-3
**Submission branch:** `main` · **Release tag:** `v1.0`

## Build and test

Install JDK 21. The committed Gradle Wrapper downloads Gradle 9.1.0 on first use;
no globally installed Gradle is required. Network access is needed for the first
Gradle/JUnit dependency download. Windows users can replace `./gradlew` with `gradlew.bat`.

```bash
./gradlew clean build
./gradlew test
```

JUnit 5 tests cover random DSU unions, 100 random MST comparisons, exhaustive
small spanning trees, SCC vs BFS reachability in both directions, cycle detection,
100 random DAGs vs Bellman–Ford with negative weights, path reconstruction,
input errors, parallel edges, loops, isolated vertices, long totals, and a
100,000-vertex DFS path. HTML results: `build/reports/tests/test/index.html`.

## Demonstrate the input graphs

```bash
./gradlew run --args='data/connected.txt'
./gradlew run --args='data/disconnected.txt'
./gradlew run --args='data/cyclic.txt'
./gradlew run --args='data/dag.txt'
```

Input format: `n m directed|undirected`, then exactly `m` lines `u v w`.
Vertices are `0..n-1`; weights are signed Java `int`. Totals and distances use `long`.
Undirected graphs produce an MST or spanning forest with its component count.
Directed graphs print SCC IDs and, if acyclic, a topological order and actual paths
from the virtual super-source (all zero-indegree vertices start at distance zero).
A cycle is reported explicitly. The graph CLI is intended for small demonstration files.

## Reproduce the evidence

Only plots need Python 3 and Matplotlib. Java build/tests/benchmark do not need Python.
Set up the plotting dependency once, preferably in a virtual environment:

```bash
python3 -m venv .venv
. .venv/bin/activate
python3 -m pip install -r scripts/requirements.txt
./gradlew reproduce
```

`reproduce` runs tests, the full benchmark and plotting in that order. Individual commands:

```bash
./gradlew benchmark
./gradlew plots
```

The benchmark uses a new `Random(42)` for every graph and the exact assignment
shuffle and edge/weight draw order. It measures five runs for each case, writes the
median in milliseconds and verifies that counters are identical across the five runs.
All ten MST/SCC/DAG reference triples are printed and checked. It also checks both
MST weights, tree sizes, all topological orders and the exact `2m`/`m` scan counts.
A discrepancy throws an exception and fails Gradle before replacing the result CSV.

There are **50 rows**: five algorithms × five sizes × two densities; DSU is measured
within Kruskal. This follows sections 6–7 of the assignment; the deliverables' phrase
“four generated graphs” conflicts with the specified grid. The benchmark needs up
to a 2 GiB Java heap; graph construction and validation are outside the timed region.
There is no dedicated warm-up, so especially the smallest timings include JVM noise.
`results/environment.txt` records the benchmark runtime and timing boundaries.

### Counter definitions

- `dsu_ops`: calls to `find` plus successful unions. Each `union` invokes two finds;
  failed unions add no union count. This is an API-operation count, not parent hops.
- `edge_scans`: Kruskal's sorted edges examined until connected; Prim's adjacency
  entries (`2m`); Kosaraju's two DFS passes (`2m`); Kahn's outgoing-edge processing (`m`);
  DAG relaxation (`m`). Graph reversal, validation and sorting are not edge scans.
- `comparisons`: calls to the sorting/priority-queue weight comparator, plus DAG
  candidate-distance comparisons. Integer indegree/visited checks are not included.
- `max_depth`: maximum number of active frames in Kosaraju's explicit DFS stack,
  across both passes; zero in algorithms without a DFS stack.

Kahn reads indegrees maintained by `Graph.addEdge`. DAG computation includes Kahn
in elapsed time but uses a separate counter object for topological preparation,
so its CSV counters describe only relaxation as required. Kosaraju's reversal is
also included in time but excluded from DFS scans. All counters are incremented
where the operation occurs; none is filled in from a complexity formula.

## Results and report

- [`REPORT.md`](REPORT.md): complexity, correctness arguments, choices and measured interpretation.
- [`results/results.csv`](results/results.csv): 50 measured rows.
- [`results/analysis.md`](results/analysis.md): generated slopes and normalized counter tables.
- [`results/plots/`](results/plots/): three PNG charts, regenerated from the CSV.

The reported counters are reproducible with this code/toolchain; times vary by
hardware, JVM, garbage collection and load. Plotted “operations” means the sum of
three operation counters, excluding stack depth. It is a work proxy, not a CPU
instruction count, and excludes vertex-initialization overhead on edgeless graphs.

## Git history and ZIP

The implementation stages live in `feature/dsu`, `feature/mst`, `feature/scc`,
`feature/paths`, and `feature/metrics`. Each contains focused commits; tested stages
are merged into `main` with merge commits. No history flattening is used.

```bash
git log --graph --oneline --all --decorate
git branch -a
git show v1.0 --no-patch
python3 scripts/package_release.py Name Surname Group
```

The last command creates `dist/DAA_Assignment3_Name_Surname_Group.zip`. Without
arguments it uses `DAA_Assignment3_saloverni.zip`; replace this with your real name,
surname and group before Moodle submission. The ZIP contains the tagged source,
results, report, wrapper, and `git-history.bundle`, which preserves all six branches
and the release tag even when the ZIP is viewed separately from GitHub. See the
included `GIT_HISTORY.txt` for restoration commands. Moodle upload is manual.

## AI-use acknowledgement

OpenAI Codex assisted with implementation, test design, benchmark execution,
plotting and documentation. Tests and benchmark were actually executed; CSV and
plots are generated from the code. The student should review the algorithms and
be prepared to explain the proofs, counter definitions and choices at the defense.

Build references: [Gradle 9.1 release notes](https://docs.gradle.org/9.1.0/release-notes.html)
and [JUnit 5.13.4 guide](https://docs.junit.org/current/user-guide/junit-user-guide-5.13.4.pdf).
