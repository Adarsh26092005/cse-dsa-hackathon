import java.util.*;
import java.io.*;
public class BudgetTraveller {
    static class Flight {
        int from, to;
        long price;
        Flight(int from, int to, long price) {
            this.from = from;
            this.to = to;
            this.price = price;
        }
    }
    static class State {
        int city;
        int flightsUsed;
        int couponsUsed;
        long cost;
        State parent;
        Flight flightTaken;
        boolean couponUsedOnFlight;
        State(int city, int flightsUsed, int couponsUsed,
              long cost, State parent, Flight flightTaken,
              boolean couponUsedOnFlight) {
            this.city = city;
            this.flightsUsed = flightsUsed;
            this.couponsUsed = couponsUsed;
            this.cost = cost;
            this.parent = parent;
            this.flightTaken = flightTaken;
            this.couponUsedOnFlight = couponUsedOnFlight;
        }
    }
    static class Result {
        long cost;
        List<State> path;
        Result(long cost, List<State> path) {
            this.cost = cost;
            this.path = path;
        }
    }
    static Result findCheapestPath(
            int n, int k, int c,
            ArrayList<Flight>[] graph) {
        int maxFlights = k + 1;
        long[][][] best =
                new long[n + 1][maxFlights + 1][c + 1];
        for (int i = 0; i <= n; i++) {
            for (int j = 0; j <= maxFlights; j++) {
                Arrays.fill(best[i][j], Long.MAX_VALUE);
            }
        }
        PriorityQueue<State> pq = new PriorityQueue<>(
                (a, b) -> Long.compare(a.cost, b.cost)
        );
        State start = new State(
                1, 0, 0, 0, null, null, false
        );
        best[1][0][0] = 0;
        pq.offer(start);
        while (!pq.isEmpty()) {
            State current = pq.poll();
            if (current.cost != best[current.city]
                    [current.flightsUsed][current.couponsUsed]) {
                continue;
            }
            if (current.city == n) {
                ArrayList<State> path = new ArrayList<>();
                State temp = current;
                while (temp != null) {
                    path.add(temp);
                    temp = temp.parent;
                }
                Collections.reverse(path);
                return new Result(current.cost, path);
            }
            if (current.flightsUsed >= maxFlights) {
                continue;
            }
            for (Flight f : graph[current.city]) {
                int nextCity = f.to;
                int nextFlights = current.flightsUsed + 1;
                int used = current.couponsUsed;
                long newCost = current.cost + f.price;
                if (newCost < best[nextCity][nextFlights][used]) {
                    best[nextCity][nextFlights][used] = newCost;
                    pq.offer(new State(
                            nextCity, nextFlights, used, newCost,
                            current, f, false
                    ));
                }
                if (current.couponsUsed < c) {
                    used = current.couponsUsed + 1;
                    newCost = current.cost + f.price / 2;
                    if (newCost <
                            best[nextCity][nextFlights][used]) {
                        best[nextCity][nextFlights][used] = newCost;
                        pq.offer(new State(
                                nextCity, nextFlights, used, newCost,
                                current, f, true
                        ));
                    }
                }
            }
        }
        return null;
    }
    static void writeCSV(
            ArrayList<Flight> flights,
            List<State> path) throws IOException {
        Set<Flight> selected = new HashSet<>();
        Set<Flight> discounted = new HashSet<>();
        for (int i = 1; i < path.size(); i++) {
            State s = path.get(i);
            selected.add(s.flightTaken);
            if (s.couponUsedOnFlight) {
                discounted.add(s.flightTaken);
            }
        }
        try (PrintWriter out =
                     new PrintWriter(new FileWriter("output.csv"))) {
            out.println(
                    "From,To,Price,OnPath,CouponUsed,PaidPrice"
            );
            for (Flight f : flights) {
                boolean chosen = selected.contains(f);
                boolean coupon = discounted.contains(f);
                long paidPrice = f.price;
                if (chosen && coupon) {
                    paidPrice = f.price / 2;
                }
                out.println(
                        f.from + "," +
                        f.to + "," +
                        f.price + "," +
                        (chosen ? "x" : "") + "," +
                        (chosen && coupon ? "x" : "") + "," +
                        (chosen ? paidPrice : "")
                );
            }
        }
        System.out.println("CSV saved as output.csv");
    }
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        try {
            int n = sc.nextInt();
            int m = sc.nextInt();
            int k = sc.nextInt();
            int c = sc.nextInt();
            if (n < 1 || m < 0 || k < 0 || c < 0) {
                System.out.println("Invalid input");
                return;
            }
            @SuppressWarnings("unchecked")
            ArrayList<Flight>[] graph =
                    (ArrayList<Flight>[]) new ArrayList<?>[n + 1];
            for (int i = 0; i <= n; i++) {
                graph[i] = new ArrayList<>();
            }
            ArrayList<Flight> flights = new ArrayList<>();
            for (int i = 0; i < m; i++) {
                int u = sc.nextInt();
                int v = sc.nextInt();
                long p = sc.nextLong();
                if (u < 1 || u > n ||
                    v < 1 || v > n || p < 0) {
                    System.out.println("Invalid flight details");
                    return;
                }
                Flight f = new Flight(u, v, p);
                graph[u].add(f);
                flights.add(f);
            }
            Result result = findCheapestPath(n, k, c, graph);
            if (result == null) {
                System.out.println(-1);
                return;
            }
            System.out.println("Minimum Cost: " + result.cost);
            System.out.print("Path: ");
            for (int i = 0; i < result.path.size(); i++) {
                System.out.print(result.path.get(i).city);
                if (i < result.path.size() - 1) {
                    System.out.print(" -> ");
                }
            }
            System.out.println();
            int stops = Math.max(0, result.path.size() - 2);
            System.out.println("Stops Used: " + stops);
            System.out.println("Flight Details:");
            for (int i = 1; i < result.path.size(); i++) {
                State s = result.path.get(i);
                Flight f = s.flightTaken;
                if (s.couponUsedOnFlight) {
                    System.out.println(
                            f.from + " -> " + f.to +
                            ": " + f.price + " -> " +
                            (f.price / 2) + " (Coupon used)"
                    );
                } else {
                    System.out.println(
                            f.from + " -> " + f.to +
                            ": " + f.price + " (Full price)"
                    );
                }
            }
            writeCSV(flights, result.path);
        } catch (InputMismatchException e) {
            System.out.println("Invalid input format");
        } catch (IOException e) {
            System.out.println("Error writing CSV: " + e.getMessage());
        } finally {
            sc.close();
        }
    }
}

