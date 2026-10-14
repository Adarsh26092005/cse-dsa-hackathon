# Closest Pair of Points

## Overview

This project compares two algorithms for finding the closest pair of points in a 2D plane:

1. **Brute Force** — checks every pair of points.
2. **Divide and Conquer** — sorts points and recursively searches smaller regions, then checks points near the dividing line.

The program also visualizes the closest pair and compares execution times for increasing input sizes.

## Requirements

- Python 3.8 or later
- NumPy
- Matplotlib

Install the required packages:

```bash
pip install numpy matplotlib
```

## Run the Program

Save the Python code in a file such as `closest_pair.py`, then run:

```bash
python closest_pair.py
```

The entry point runs a simulation with 100 randomly generated points:

```python
if __name__ == "__main__":
    run_simulation(N=100)
```

## How It Works

### 1. Brute-force algorithm

The program calculates the Euclidean distance between every pair of points and returns the smallest distance and its two points.

- **Time complexity:** `O(n^2)`
- **Space complexity:** `O(1)` auxiliary space, excluding input and returned values.

### 2. Divide-and-conquer algorithm

The program sorts points by their x- and y-coordinates, divides the points into two halves, recursively finds the closest pair in each half, and examines a vertical strip around the dividing line for a closer cross-boundary pair.

- **Intended time complexity:** `O(n log n)`
- The exact implementation should be tested carefully for correctness, especially when multiple points share the same x-coordinate. A robust implementation should preserve point identity when partitioning and ensure the strip scan checks the necessary candidate points.

## Output

When executed, the program:

- Generates random points.
- Runs both algorithms and prints their closest distances and pairs.
- Compares the reported distances.
- Displays a visualization of the points and closest pairs.
- Plots execution time against input size.
- Prints a benchmark table for several input sizes.

Because the points are random and execution time depends on the computer, results will vary between runs.

## Functions

| Function | Purpose |
|---|---|
| `distance(p1, p2)` | Calculates Euclidean distance between two points. |
| `brute_force_closest_pair(points)` | Finds the closest pair by checking all pairs. |
| `divide_conquer_closest_pair(points)` | Finds the closest pair using divide and conquer. |
| `run_simulation(N=50)` | Generates points, compares algorithms, and starts visualization and benchmarking. |
| `visualize_results(...)` | Draws the points, closest pairs, dividing line, strip, and timing plot. |
| `benchmark_performance()` | Prints execution-time comparisons for larger inputs. |

## Notes

- At least two points are required.
- The distance comparison verifies that both algorithms report the same minimum distance within a small tolerance.
- The code imports NumPy, although the shown implementation does not currently use it.
- Timing measurements can vary due to system load and other runtime conditions.

## Complexity Summary

| Algorithm | Expected time complexity |
|---|---|
| Brute force | `O(n^2)` |
| Divide and conquer | `O(n log n)` when implemented correctly |

