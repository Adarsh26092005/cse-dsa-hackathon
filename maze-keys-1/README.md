# Maze with Keys and Doors — Shortest Path Solver

A Python-based maze solver that uses **Breadth-First Search (BFS)** and a key-state bitmask to find the shortest valid path through a grid containing walls, keys, and locked doors.

## Features

- Finds the shortest path to the exit using BFS.
- Supports multiple keys and their matching doors.
- Prevents movement through walls and locked doors.
- Visualizes the maze and the calculated path.
- Supports step-by-step animation.
- Accepts custom maze input through a CSV file.
- Can generate random solvable mazes.

## Requirements

- Python 3
- Matplotlib

Install the required dependency:

```bash
python -m pip install matplotlib
```

## Project Structure

```text
maze-keys-1/
├── main.py
├── maze.csv
├── solved_maze.csv
├── requirements.txt
├── README.md
└── .gitignore
```

## How to Provide Input

The solver accepts a maze through `maze.csv`. Each cell is separated by a comma, and the maze must be a square grid with the same number of rows and columns.

### Supported Symbols

| Symbol | Meaning |
|---|---|
| `S` | Starting position |
| `E` | Exit |
| `.` | Open cell |
| `#` | Wall |
| `a`–`f` | Keys |
| `A`–`D`, `F` | Matching locked doors |

Each door requires its corresponding lowercase key. The symbol `E` is reserved for the exit.

### Example Input

Save the following as `maze.csv`:

```csv
S,.,#,.,.
#,.,#,.,#
a,.,.,.,A
#,.,#,.,.
.,.,#,.,E
```

You can replace this example with your own square maze, keeping the symbols and key-door rules valid.

## How to Run

Open a terminal in the project folder.

### 1. Solve a custom maze

```bash
python main.py --csv maze.csv
```

### 2. Solve a custom maze with animation

```bash
python main.py --csv maze.csv --animate
```

### 3. Generate a random solvable maze

```bash
python main.py --random --size 12 --keys 2
```

### 4. Generate and animate a random maze

```bash
python main.py --random --size 12 --keys 2 --animate
```

## Output

The program reports the shortest path length and solver execution time. If a valid path exists, it saves the solved maze to `solved_maze.csv` and displays the visualization.

The solved CSV marks ordinary cells on the chosen path with `x`, while keeping the start, exit, keys, and doors identifiable.

## Algorithm

The solver uses Breadth-First Search over states consisting of:

- Current row and column.
- A bitmask representing the keys collected.

Tracking the key inventory as part of the state allows the algorithm to distinguish between reaching the same cell with different sets of keys.

Because each move has equal cost, BFS finds the shortest valid path.

## Complexity

For an N × N maze with T distinct key types, the worst-case time and space complexity are both:

**O(N² × 2ᵀ)**

## Author

Developed as a Data Structures and Algorithms project demonstrating graph traversal, state-space search, bitmasking, and visualization.

## Maze Visualization

The following screenshot shows the shortest path found by the BFS solver, including key collection, locked doors, obstacles, and the exit.

![Maze with Keys and Doors — Shortest Path](screenshot/maze-solution.png)
