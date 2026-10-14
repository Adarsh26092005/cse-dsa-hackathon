import time
import random
import math
import matplotlib.pyplot as plt
import numpy as np

def distance(p1, p2):
    """Calculates the Euclidean distance between two 2D points."""
    return math.hypot(p1[0] - p2[0], p1[1] - p2[1])

def brute_force_closest_pair(points):
    """
    Brute-force algorithm to find the closest pair of points.
    Time Complexity: O(N^2)
    """
    n = len(points)
    if n < 2:
        raise ValueError("At least 2 points are required to find a closest pair.")
    
    min_dist = float('inf')
    best_pair = (None, None)
    
    for i in range(n):
        for j in range(i + 1, n):
            d = distance(points[i], points[j])
            if d < min_dist:
                min_dist = d
                best_pair = (points[i], points[j])
                
    return min_dist, best_pair[0], best_pair[1]

def divide_conquer_closest_pair(points):
    """
    Divide-and-Conquer algorithm to find the closest pair of points.
    Time Complexity: O(N log N)
    """
    n = len(points)
    if n < 2:
        raise ValueError("At least 2 points are required to find a closest pair.")
    
    # Check for exact duplicate points (distance 0.0)
    seen = set()
    for p in points:
        if p in seen:
            return 0.0, p, p
        seen.add(p)

    px = sorted(points, key=lambda p: p[0])
    py = sorted(points, key=lambda p: p[1])
    
    def recurse(px, py):
        n = len(px)
        if n <= 3:
            return brute_force_closest_pair(px)
        
        mid = n // 2
        mid_point = px[mid]
        Qx = px[:mid]
        Rx = px[mid:]
        
        mid_x = mid_point[0]
        
        # Partition py into left and right sublists
        left_set = set(Qx)
        Qy = []
        Ry = []
        for p in py:
            if p in left_set:
                Qy.append(p)
                left_set.remove(p)
            else:
                Ry.append(p)
                
        dl, p1_l, p2_l = recurse(Qx, Qy)
        dr, p1_r, p2_r = recurse(Rx, Ry)
        
        if dl < dr:
            d = dl
            best_pair = (p1_l, p2_l)
        else:
            d = dr
            best_pair = (p1_r, p2_r)
            
        # Strip points within distance d of the dividing line
        strip = [p for p in py if abs(p[0] - mid_x) < d]
        
        min_dist = d
        best_p1, best_p2 = best_pair
        
        strip_len = len(strip)
        for i in range(strip_len):
            for j in range(i + 1, min(i + 8, strip_len)):
                p1 = strip[i]
                p2 = strip[j]
                if (p2[1] - p1[1]) >= min_dist:
                    break
                dist = distance(p1, p2)
                if dist < min_dist:
                    min_dist = dist
                    best_p1, best_p2 = (p1, p2)
                    
        return min_dist, best_p1, best_p2

    return recurse(px, py)

def run_simulation(N=50):
    print(f"Generating {N} random points...")
    # Generate random points (integers or floats between 0 and 100)
    points = [(random.uniform(0.0, 100.0), random.uniform(0.0, 100.0)) for _ in range(N)]
    
    # --- Brute-Force Execution ---
    start_time = time.perf_counter()
    bf_dist, bf_p1, bf_p2 = brute_force_closest_pair(points)
    bf_time = (time.perf_counter() - start_time) * 1000 # in ms
    
    # --- Divide-and-Conquer Execution ---
    start_time = time.perf_counter()
    dc_dist, dc_p1, dc_p2 = divide_conquer_closest_pair(points)
    dc_time = (time.perf_counter() - start_time) * 1000 # in ms
    
    # --- Output Format ---
    print(f"\nBrute-Force:         {bf_dist:.6f}, Pair: {bf_p1}, {bf_p2}")
    print(f"Divide-and-Conquer:  {dc_dist:.6f}, Pair: {dc_p1}, {dc_p2}")
    print(f"Execution Time (ms): Brute-Force = {bf_time:.4f} ms | Divide-and-Conquer = {dc_time:.4f} ms")
    
    # Verification
    assert math.isclose(bf_dist, dc_dist, abs_tol=1e-5), "Algorithms disagreed on minimum distance!"
    print("Verification: Both algorithms successfully agreed on the minimum distance.")
    
    # --- Visualization ---
    visualize_results(points, bf_p1, bf_p2, dc_p1, dc_p2, bf_dist)
    benchmark_performance()

def visualize_results(points, bf_p1, bf_p2, dc_p1, dc_p2, d):
    fig, (ax1, ax2) = plt.subplots(1, 2, figsize=(14, 6))
    
    xs = [p[0] for p in points]
    ys = [p[1] for p in points]
    
    # Plot 1: Points & Closest Pair
    ax1.scatter(xs, ys, color='dodgerblue', alpha=0.7, label='Points', s=40)
    
    # Highlight Brute-Force pair
    ax1.plot([bf_p1[0], bf_p2[0]], [bf_p1[1], bf_p2[1]], color='red', linewidth=2.5, linestyle='--', label='BF Closest Pair')
    ax1.scatter([bf_p1[0], bf_p2[0]], [bf_p1[1], bf_p2[1]], color='red', s=80, zorder=5)
    
    # Highlight D&C pair (slightly offset or distinct marker if overlapping)
    ax1.plot([dc_p1[0], dc_p2[0]], [dc_p1[1], dc_p2[1]], color='green', linewidth=1.2, label='D&C Closest Pair')
    
    # Show dividing line and strip for D&C (approximate midpoint for visual representation)
    sorted_x = sorted(points, key=lambda p: p[0])
    mid_x = sorted_x[len(sorted_x) // 2][0]
    ax1.axvline(x=mid_x, color='orange', linestyle=':', label='Dividing Line')
    ax1.axvspan(mid_x - d, mid_x + d, color='orange', alpha=0.15, label=f'Strip Region (width 2d)')
    
    ax1.set_title("Closest Pair Visualization")
    ax1.set_xlabel("X Coordinate")
    ax1.set_ylabel("Y Coordinate")
    ax1.legend(loc='upper right', fontsize=8)
    ax1.grid(True, linestyle='--', alpha=0.5)
    
    # Plot 2: Performance Benchmark Curve
    sizes = [10, 50, 100, 250, 500, 1000, 2000]
    bf_times = []
    dc_times = []
    
    for size in sizes:
        test_pts = [(random.uniform(0, 1000), random.uniform(0, 1000)) for _ in range(size)]
        
        t0 = time.perf_counter()
        brute_force_closest_pair(test_pts)
        bf_times.append((time.perf_counter() - t0) * 1000)
        
        t0 = time.perf_counter()
        divide_conquer_closest_pair(test_pts)
        dc_times.append((time.perf_counter() - t0) * 1000)
        
    ax2.plot(sizes, bf_times, marker='o', color='red', label='Brute-Force O(N^2)')
    ax2.plot(sizes, dc_times, marker='s', color='green', label='Divide-and-Conquer O(N log N)')
    ax2.set_title("Running Time vs Input Size (N)")
    ax2.set_xlabel("Number of Points (N)")
    ax2.set_ylabel("Execution Time (ms)")
    ax2.legend()
    ax2.grid(True, linestyle='--', alpha=0.5)
    
    plt.tight_layout()
    plt.show()

def benchmark_performance():
    print("\nRunning benchmark across increasing input sizes...")
    # Console performance summary table
    print(f"{'N':<8} | {'Brute-Force (ms)':<18} | {'Divide-and-Conquer (ms)':<22}")
    print("-" * 54)
    for n in [100, 500, 1000, 2000, 5000]:
        pts = [(random.uniform(0, 1000), random.uniform(0, 1000)) for _ in range(n)]
        
        t0 = time.perf_counter()
        brute_force_closest_pair(pts)
        t_bf = (time.perf_counter() - t0) * 1000
        
        t0 = time.perf_counter()
        divide_conquer_closest_pair(pts)
        t_dc = (time.perf_counter() - t0) * 1000
        
        print(f"{n:<8} | {t_bf:<18.4f} | {t_dc:<22.4f}")

if __name__ == "__main__":
    # Example execution with N = 100 points
    run_simulation(N=100)