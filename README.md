# Maze with Keys and Doors

## Project Overview
A Python-based maze solver that finds the shortest path from the start to the exit while handling keys, locked doors, and walls.

## Features
- Shortest-path search using Breadth-First Search (BFS).
- Automatic key collection and matching door access.
- Random solvable maze generation.
- CSV input and solved-maze CSV output.
- Color-coded maze visualization.
- Optional step-by-step path animation.

## Technologies Used
- Python
- Matplotlib
- CSV
- Breadth-First Search (BFS)

## Installation
Install the required dependencies:

```bash
pip install -r requirements.txt
```

## Run the Project

Generate a random maze:

```bash
python main.py --random --size 12 --keys 3 --seed 42
```

Run with animation:

```bash
python main.py --random --size 15 --keys 5 --animate
```

Solve a maze from a CSV file:

```bash
python main.py --csv maze.csv
```

## Output
The program displays the shortest path length and execution time, visualizes the maze, and saves the solved maze as `solved_maze.csv`.

## Algorithm
Breadth-First Search (BFS) explores possible positions while tracking collected keys to find a shortest valid route.
