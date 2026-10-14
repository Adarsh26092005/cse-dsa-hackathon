import argparse
import csv
import random
import time
from collections import deque
from pathlib import Path

import matplotlib.pyplot as plt
from matplotlib.colors import ListedColormap
from matplotlib.patches import Patch


KEYS = "abcdef"
DOORS = "ABCDF"  # E is reserved for the exit symbol in this input format.
RANDOM_KEY_TYPES = "abcdf"  # Avoid the unavoidable e/E symbol collision in generated mazes.
WALL = "#"
OPEN = "."
START = "S"
EXIT = "E"


def read_maze_csv(filename):
    """Read a square maze from CSV. Cells may be comma-separated or single-character rows."""
    rows = []
    with open(filename, "r", newline="", encoding="utf-8-sig") as f:
        for row in csv.reader(f):
            if not row:
                continue
            cells = [cell.strip() for cell in row]
            # Also accept a row like S..#A where each character is a cell.
            if len(cells) == 1 and len(cells[0]) > 1:
                cells = list(cells[0])
            rows.append(cells)

    if not rows:
        raise ValueError("CSV file is empty.")

    n = len(rows)
    if any(len(row) != n for row in rows):
        raise ValueError("Maze must be an N x N square grid.")

    valid = set(".#SxXEabcdefABCDEF")
    for r, row in enumerate(rows):
        for c, cell in enumerate(row):
            if cell not in valid:
                raise ValueError(f"Invalid cell {cell!r} at row {r + 1}, column {c + 1}.")

    flat = [cell for row in rows for cell in row]
    if flat.count(START) != 1:
        raise ValueError("Maze must contain exactly one S.")
    if flat.count(EXIT) != 1:
        raise ValueError("Maze must contain exactly one E.")

    keys = {cell for cell in flat if cell in KEYS}
    doors = {cell.lower() for cell in flat if cell in DOORS}
    if not doors.issubset(keys):
        missing = sorted(doors - keys)
        raise ValueError(f"Door(s) without matching key: {', '.join(missing)}")

    return rows


def write_maze_csv(grid, filename):
    with open(filename, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerows(grid)


def solve_maze(grid):
    """
    BFS over states (row, column, key_mask).
    Returns (distance, path_states, visited_states).
    path_states contains (row, col, key_mask) states from S to E.
    """
    n = len(grid)
    start = next((r, c) for r in range(n) for c in range(n) if grid[r][c] == START)
    all_keys_mask = 0
    for r in range(n):
        for c in range(n):
            cell = grid[r][c]
            if cell in KEYS:
                all_keys_mask |= 1 << (ord(cell) - ord("a"))

    def collect_key(mask, cell):
        if cell in KEYS:
            mask |= 1 << (ord(cell) - ord("a"))
        return mask

    start_mask = collect_key(0, grid[start[0]][start[1]])
    start_state = (start[0], start[1], start_mask)
    queue = deque([start_state])
    parent = {start_state: None}
    distance = {start_state: 0}
    visited_order = [start_state]
    end_state = None
    directions = [(-1, 0), (1, 0), (0, -1), (0, 1)]

    while queue:
        r, c, mask = queue.popleft()
        state = (r, c, mask)
        if grid[r][c] == EXIT:
            end_state = state
            break

        for dr, dc in directions:
            nr, nc = r + dr, c + dc
            if not (0 <= nr < n and 0 <= nc < n):
                continue

            cell = grid[nr][nc]
            if cell == WALL:
                continue
            if cell in DOORS:
                needed_bit = 1 << (ord(cell.lower()) - ord("a"))
                if not (mask & needed_bit):
                    continue

            new_mask = collect_key(mask, cell)
            next_state = (nr, nc, new_mask)
            if next_state in parent:
                continue

            parent[next_state] = state
            distance[next_state] = distance[state] + 1
            visited_order.append(next_state)
            queue.append(next_state)

    if end_state is None:
        return -1, [], visited_order

    path = []
    state = end_state
    while state is not None:
        path.append(state)
        state = parent[state]
    path.reverse()
    return distance[end_state], path, visited_order


def make_solvable_random_maze(n, t, seed=None):
    """
    Create a solvable maze by placing S, keys, doors, E along a guaranteed
    corridor, then adding walls only outside that corridor.
    """
    if n < max(5, 2 * t + 3):
        raise ValueError(f"For T={t}, use N >= {max(5, 2 * t + 3)}.")
    rng = random.Random(seed)
    grid = [[OPEN for _ in range(n)] for _ in range(n)]

    # Build a guaranteed L-shaped route along the top row and right column.
    # Other cells can contain random walls without blocking this route.
    route = [(0, c) for c in range(n)]
    route.extend((r, n - 1) for r in range(1, n))

    # Pick distinct positions along the route: S, each key/door pair, E.
    needed = 2 * t + 2
    chosen_indices = [round(i * (len(route) - 1) / (needed - 1)) for i in range(needed)]
    chosen_indices = sorted(set(chosen_indices))
    if len(chosen_indices) < needed:
        raise ValueError("Grid is too small to place all keys and doors.")

    for r in range(n):
        for c in range(n):
            grid[r][c] = WALL if rng.random() < 0.28 else OPEN

    # Ensure the entire snake route remains open, guaranteeing connectivity.
    for r, c in route:
        grid[r][c] = OPEN

    placed = [(route[i], i) for i in chosen_indices]
    grid[route[chosen_indices[0]][0]][route[chosen_indices[0]][1]] = START
    for i in range(t):
        key_type = RANDOM_KEY_TYPES[i]
        kr, kc = route[chosen_indices[1 + 2 * i]]
        dr, dc = route[chosen_indices[2 + 2 * i]]
        grid[kr][kc] = key_type
        grid[dr][dc] = key_type.upper()
    er, ec = route[chosen_indices[-1]]
    grid[er][ec] = EXIT
    return grid


def path_to_csv_grid(grid, path):
    """Keep S/E/keys/doors identifiable; mark ordinary path cells with x."""
    result = [row[:] for row in grid]
    path_cells = {(r, c) for r, c, _ in path}
    for r, c in path_cells:
        if result[r][c] == OPEN:
            result[r][c] = "x"
    return result


def visualize_maze(grid, path, animate=False, delay=0.15):
    n = len(grid)
    fig, ax = plt.subplots(figsize=(8, 8))
    plt.subplots_adjust(bottom=0.18)

    # Fixed symbol-to-color map.
    colors = {
        "#": "#222222",
        ".": "#ffffff",
        "x": "#75d6a1",
        "S": "#238444",
        "E": "#d94343",
        "a": "#f4d35e", "A": "#f4d35e",
        "b": "#56b4e9", "B": "#56b4e9",
        "c": "#c77dff", "C": "#c77dff",
        "d": "#f4a261", "D": "#f4a261",
        "e": "#80b918", "E": "#d94343",
        "f": "#ff70a6", "F": "#ff70a6",
    }
    # E is the exit, so its color takes precedence over key/door palettes.
    symbol_order = ["#", ".", "x", "S", "E", "a", "A", "b", "B", "c", "C",
                    "d", "D", "e", "f", "F"]
    color_list = [colors[s] for s in symbol_order]
    cmap = ListedColormap(color_list)
    symbol_to_num = {s: i for i, s in enumerate(symbol_order)}

    path_cells = {(r, c) for r, c, _ in path}
    display = [row[:] for row in grid]
    for r, c in path_cells:
        if display[r][c] == ".":
            display[r][c] = "x"

    matrix = [[symbol_to_num.get(cell, symbol_to_num["."]) for cell in row] for row in display]
    ax.imshow(matrix, cmap=cmap, vmin=0, vmax=len(color_list) - 1)

    for r in range(n):
        for c in range(n):
            cell = grid[r][c]
            label = cell
            if (r, c) in path_cells and cell in "ABCDEF":
                label = cell + "✓"
            ax.text(c, r, label, ha="center", va="center",
                    color="white" if cell == "#" or cell in "SE" else "black",
                    fontsize=max(7, min(13, 180 // n)), fontweight="bold")

    ax.set_xticks(range(n))
    ax.set_yticks(range(n))
    ax.set_xticks([x - 0.5 for x in range(1, n)], minor=True)
    ax.set_yticks([y - 0.5 for y in range(1, n)], minor=True)
    ax.grid(which="minor", color="#888888", linewidth=0.6)
    ax.tick_params(length=0)
    ax.set_title("Maze with Keys and Doors — Shortest Path")
    legend_items = [
        Patch(facecolor="#ffffff", edgecolor="gray", label="Open"),
        Patch(facecolor="#222222", label="Wall"),
        Patch(facecolor="#238444", label="Start"),
        Patch(facecolor="#d94343", label="Exit"),
        Patch(facecolor="#75d6a1", label="Path"),
        Patch(facecolor="#f4d35e", label="Key/door a/A"),
        Patch(facecolor="#56b4e9", label="Key/door b/B"),
        Patch(facecolor="#c77dff", label="Key/door c/C"),
    ]
    ax.legend(handles=legend_items, loc="upper center", bbox_to_anchor=(0.5, -0.08),
              ncol=4, fontsize=8)
    plt.show()

    if animate and path:
        # A simple step-by-step animation in a second window.
        plt.ion()
        fig2, ax2 = plt.subplots(figsize=(7, 7))
        inventory = set()
        for step, (r, c, mask) in enumerate(path):
            ax2.clear()
            ax2.imshow(matrix, cmap=cmap, vmin=0, vmax=len(color_list) - 1)
            for rr in range(n):
                for cc in range(n):
                    cell = grid[rr][cc]
                    if rr == r and cc == c:
                        label = "🙂"
                    elif (rr, cc) in path_cells and cell in "ABCDEF" and cell.lower() in inventory:
                        label = cell + "✓"
                    else:
                        label = cell
                    ax2.text(cc, rr, label, ha="center", va="center",
                                 color="white" if cell == "#" else "black",
                                 fontsize=max(7, min(13, 180 // n)))
            cell = grid[r][c]
            if cell in KEYS:
                inventory.add(cell)
            ax2.set_title(f"Step {step} | Keys: {', '.join(sorted(inventory)) or 'none'}")
            ax2.set_xticks(range(n))
            ax2.set_yticks(range(n))
            ax2.set_xticks([x - 0.5 for x in range(1, n)], minor=True)
            ax2.set_yticks([y - 0.5 for y in range(1, n)], minor=True)
            ax2.grid(which="minor", color="#888888", linewidth=0.6)
            ax2.tick_params(length=0)
            plt.pause(delay)
        plt.ioff()
        plt.show()


def main():
    parser = argparse.ArgumentParser(description="Maze shortest path with keys and doors.")
    source = parser.add_mutually_exclusive_group()
    source.add_argument("--csv", type=str, help="Input square maze CSV file.")
    source.add_argument("--random", action="store_true", help="Generate a random solvable maze.")
    parser.add_argument("--size", type=int, default=12, help="Grid size for random maze (default: 12).")
    parser.add_argument("--keys", type=int, default=2, help="Number of key/door types for random maze (1-6).")
    parser.add_argument("--seed", type=int, default=None, help="Optional random seed.")
    parser.add_argument("--animate", action="store_true", help="Animate traversal of the found path.")
    parser.add_argument("--delay", type=float, default=0.15, help="Animation delay in seconds.")
    parser.add_argument("--output", type=str, default="solved_maze.csv", help="Output CSV path.")
    args = parser.parse_args()

    try:
        if args.csv:
            grid = read_maze_csv(args.csv)
        else:
            # Default behavior is to generate a random maze.
            t = args.keys
            if not 1 <= t <= len(RANDOM_KEY_TYPES):
                raise ValueError("Random generation supports 1-5 unambiguous key/door types (a-d and f); E is reserved for the exit.")
            grid = make_solvable_random_maze(args.size, t, args.seed)

        n = len(grid)
        started = time.perf_counter()
        distance, path, _ = solve_maze(grid)
        elapsed_ms = (time.perf_counter() - started) * 1000

        print(f"Shortest path length: {distance}")
        print(f"Solver execution time: {elapsed_ms:.3f} ms")

        if distance >= 0:
            solved = path_to_csv_grid(grid, path)
            write_maze_csv(solved, args.output)
            print(f"Solved maze CSV saved to: {args.output}")
        else:
            print("No valid path exists; output CSV was not written.")

        visualize_maze(grid, path, animate=args.animate, delay=args.delay)

    except (ValueError, OSError) as exc:
        raise SystemExit(f"Error: {exc}")


if __name__ == "__main__":
    main()
