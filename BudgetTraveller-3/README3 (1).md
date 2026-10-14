#  Budget Traveller with Coupons

A beginner-friendly Java project that finds the **minimum cost to travel from City 1 to City N** using one-way flights, with a limited number of stops and discount coupons.

The program uses **Dijkstra’s algorithm with a PriorityQueue** to find the cheapest valid route. A coupon can be used on a single flight to reduce its price by half, rounded down.

##  Problem Statement

Given a directed graph representing cities and one-way flights, find the cheapest route from City `1` to City `N` while satisfying these conditions:

- Each flight has a ticket price.
- At most `K` intermediate stops are allowed.
- A maximum of `C` discount coupons are available.
- Each coupon halves the price of one flight using integer division.
- At most one coupon can be used per flight.
- If the destination cannot be reached within the given constraints, return `-1`.

##  Features

- Finds the minimum travel cost.
- Supports a maximum number of intermediate stops.
- Supports multiple discount coupons.
- Tracks the selected flight route.
- Displays the original and discounted flight prices.
- Exports the flight graph and selected route to a CSV file.
- Marks selected flights with `x` in the CSV output.
- Identifies flights where coupons are used.

### Planned Extensions

- Read graph data from a CSV file.
- Generate random flight networks using user-defined `K` and `C`.
- Visualize cities as nodes and flights as directed, weighted edges.
- Animate the selected route and display the running cost and stops.
- Highlight discounted flights in gold.

##  Technologies Used

- **Programming Language:** Java
- **Algorithm:** Dijkstra’s algorithm
- **Data Structures:** ArrayList, PriorityQueue, arrays and custom classes
- **File Handling:** Java FileWriter and PrintWriter
- **Output Format:** Console and CSV


##  Requirements

- Java Development Kit (JDK) 8 or later
- A Java IDE such as IntelliJ IDEA, Eclipse or VS Code, or a terminal
- A text editor for viewing CSV files

##  Input Format

```text
N M
K C
u1 v1 p1
u2 v2 p2
...
uM vM pM
```

Where:

| Variable | Description |
|---|---|
| `N` | Number of cities |
| `M` | Number of one-way flights |
| `K` | Maximum number of intermediate stops |
| `C` | Number of discount coupons |
| `u` | Starting city of a flight |
| `v` | Destination city of a flight |
| `p` | Original ticket price |

Cities are numbered from `1` to `N`.

## Example Input

```text
4 4
1 1
1 2 100
2 4 100
1 3 300
3 4 100
```

### Explanation

- There are 4 cities and 4 flights.
- The maximum allowed intermediate stops are 1.
- One discount coupon is available.
- The destination is City 4.

The cheapest route is `1 → 2 → 4`. Applying a coupon to either flight reduces the total cost from 200 to 150.

##  Example Output

```text
Minimum Cost: 150
Path: 1 -> 2 -> 4
Stops Used: 1
Flight Details:
1 -> 2: 100 -> 50 (Coupon used)
2 -> 4: 100 (Full price)
CSV saved as output.csv
```

If no valid route exists, the program prints:

```text
-1
```

##  Coupon Calculation

The discounted price is calculated using integer division:

```java
long discountedPrice = price / 2;
```

For example:

| Original Price | Discounted Price |
|---:|---:|
| 100 | 50 |
| 101 | 50 |
| 75 | 37 |
| 999 | 499 |

##  CSV Output

After a valid route is found, the program creates `output.csv` in the current working directory.

The CSV contains these columns:

| Column | Description |
|---|---|
| `From` | Starting city |
| `To` | Destination city |
| `Price` | Original ticket price |
| `OnPath` | `x` if the flight is on the chosen route |
| `CouponUsed` | `x` if a coupon was used |
| `PaidPrice` | Actual price paid for a selected flight |

Example:

```csv
From,To,Price,OnPath,CouponUsed,PaidPrice
1,2,100,x,x,50
2,4,100,x,,100
1,3,300,,,
3,4,100,,,
```

##  Algorithm

The program uses Dijkstra’s algorithm with an expanded state to account for flight limits and coupons.

Each state tracks:

- Current city
- Number of flights taken
- Number of coupons used
- Total cost
- Previous state, to reconstruct the path

For every available flight, the program explores two possibilities:

1. Travel without a coupon and pay the full price.
2. Travel with a coupon, if one remains, and pay half the price rounded down.

A PriorityQueue processes the lowest-cost states first. The algorithm only considers routes containing at most `K + 1` flights.

##  Complexity Analysis

Let:

- `N` = number of cities
- `M` = number of flights
- `K` = maximum intermediate stops
- `C` = number of coupons

The implementation uses a three-dimensional cost array to track cities, flights taken and coupons used.

Approximate complexity:

- **Time:** `O((N(K+1)(C+1) + 1) log(N(K+1)(C+1)) + M(K+1)(C+1))`
- **Space:** `O(N(K+1)(C+1) + M)`

##  Limitations

- The current implementation accepts graph input through the console.
- CSV input and random graph generation are planned extensions.
- Graph visualization and journey animation are not included in the current Java console implementation.
- The program assumes city numbers are valid and ticket prices are non-negative.
- The CSV file is generated only when a valid route is found.

##  Future Enhancements

- Add CSV file import.
- Generate random graphs with configurable constraints.
- Build a graphical interface using Java Swing or a web frontend.
- Animate the selected route and running cost.
- Display discounted flights in gold.
- Add automated test cases for unreachable destinations, zero coupons and multiple possible routes.

##  Learning Outcomes

This project demonstrates:

- Graph representation using adjacency lists.
- Implementation of Dijkstra’s algorithm.
- Use of a PriorityQueue in Java.
- State tracking and path reconstruction.
- Handling constrained shortest-path problems.
- Basic CSV file generation.
- Applying algorithmic problem-solving to a real-world travel scenario.
