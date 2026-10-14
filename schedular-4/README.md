# Exam Timetable Scheduler (Java)

A beginner-friendly Java implementation of exam scheduling as graph colouring with a slot-capacity limit.

## Features
- Manual graph input (`N M R`, followed by `M` conflict edges)
- Random graph generation using edge density
- DSatur greedy heuristic with slot capacity `R`
- Exact backtracking search for a minimum slot count, with a time limit
- Validation of conflicts and slot capacity
- Slot-wise timetable output
- CSV timetable exports
- Graphviz DOT files with slot-coloured nodes
- Browser-viewable HTML timetable grid
- Algorithm comparison CSV

## Requirements
- JDK 11 or newer
- Optional: Graphviz to render `.dot` files as images

## Run
```bash
javac Main.java
java Main
```

## Input example
Choose option `1`, then enter:
```text
6
5
2
1
2
1
3
2
4
3
5
4
5
```
The program prompts for each value/edge separately. This corresponds to `N=6, M=5, R=2`, with edges `(1,2)`, `(1,3)`, `(2,4)`, `(3,5)`, `(4,5)`.

For random input, choose option `2`, then enter `N`, `R`, and density (e.g. `0.4`).

## Output
The program creates an `output/` directory containing:
- `timetable_greedy.csv`
- `timetable_exact.csv`
- `conflict_graph_greedy.dot`
- `conflict_graph_exact.dot`
- `timetable_visualization.html`
- `algorithm_comparison.csv`

Open the HTML file in a browser to see the timetable grid. To render the conflict graph DOT file with Graphviz:
```bash
dot -Tpng output/conflict_graph_greedy.dot -o output/conflict_graph_greedy.png
```

## Algorithm notes
- DSatur repeatedly selects the unassigned course with the most distinct colours among its already-coloured neighbours; ties are broken by degree. It assigns a feasible existing slot if possible, respecting both conflicts and capacity.
- Backtracking searches for a feasible assignment with increasing slot limits below the greedy result. It uses a time limit. `Proven optimal: YES` is printed only when all smaller candidate slot counts have been ruled out or a minimum feasible count was found. If it times out, it reports `NO`.
- Greedy is a heuristic and does not guarantee a minimum.
- The exact algorithm is exponential in the worst case. Large/dense graphs may hit the time limit.
- Input validation expects course IDs in `1..N`; self-conflict edges are rejected.

## GitHub
Create a repository, then run in this folder:
```bash
git init
git add Main.java README.md .gitignore
git commit -m "Add Java exam timetable scheduler"
git branch -M main
git remote add origin https://github.com/YOUR-USERNAME/YOUR-REPOSITORY.git
git push -u origin main
```
