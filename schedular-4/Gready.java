import java.io.*;
import java.nio.file.*;
import java.util.*;
public class Gready {
    static class Result {
        int[] slots; // zero-based slot assignment
        int slotCount;
        long runtimeMs;
        boolean optimal;
        String algorithm;
        Result(String algorithm, int[] slots, int slotCount, long runtimeMs, boolean optimal) {
            this.algorithm = algorithm; this.slots = slots; this.slotCount = slotCount;
            this.runtimeMs = runtimeMs; this.optimal = optimal;
        }
    }

    static List<Set<Integer>> graph;
    static int N, R;
    static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) throws Exception {
        System.out.println("=== EXAM TIMETABLE SCHEDULER (JAVA) ===");
        System.out.println("1. Enter conflict graph");
        System.out.println("2. Generate random conflict graph");
        int mode = readInt("Choose option (1/2): ", 1, 2);

        if (mode == 1) {
            N = readInt("Enter N (courses): ", 1, 10000);
            int M = readInt("Enter M (conflict edges): ", 0, N * (N - 1) / 2);
            R = readInt("Enter R (maximum exams per slot): ", 1, N);
            graph = emptyGraph(N);
            System.out.println("Enter " + M + " edges as: u v (course IDs from 1 to " + N + ")");
            for (int i = 0; i < M; i++) {
                int u = readInt("Edge " + (i + 1) + " - u: ", 1, N) - 1;
                int v = readInt("Edge " + (i + 1) + " - v: ", 1, N) - 1;
                if (u == v) {
                    System.out.println("A course cannot conflict with itself. Re-enter this edge.");
                    i--; continue;
                }
                graph.get(u).add(v);
                graph.get(v).add(u);
            }
        } else {
            N = readInt("Enter N (courses): ", 1, 10000);
            R = readInt("Enter R (maximum exams per slot): ", 1, N);
            double density = readDouble("Enter edge density (0.0 to 1.0): ", 0.0, 1.0);
            long seed = System.nanoTime();
            Random random = new Random(seed);
            graph = emptyGraph(N);
            for (int u = 0; u < N; u++) {
                for (int v = u + 1; v < N; v++) {
                    if (random.nextDouble() < density) {
                        graph.get(u).add(v);
                        graph.get(v).add(u);
                    }
                }
            }
            System.out.println("Random graph generated (seed " + seed + ").");
        }

        System.out.println("\nCourses: " + N + ", conflicts: " + countEdges() + ", capacity R: " + R);
        Result greedy = runGreedy();
        System.out.println("\n--- DSatur Greedy ---");
        printResult(greedy);

        int exactLimit = readInt("\nExact backtracking time limit in seconds (1-120, recommended 5-15): ", 1, 120);
        Result exact = runExact(exactLimit * 1000L);
        System.out.println("\n--- Exact Backtracking ---");
        printResult(exact);

        System.out.println("\n--- Timetable (DSatur) ---");
        printTimetable(greedy);
        System.out.println("\n--- Timetable (Exact / best found) ---");
        printTimetable(exact);

        Path out = Paths.get("output");
        Files.createDirectories(out);
        exportCsv(greedy, out.resolve("timetable_greedy.csv"));
        exportCsv(exact, out.resolve("timetable_exact.csv"));
        exportDot(greedy, out.resolve("conflict_graph_greedy.dot"));
        exportDot(exact, out.resolve("conflict_graph_exact.dot"));
        exportHtml(greedy, exact, out.resolve("timetable_visualization.html"));
        exportComparisonCsv(greedy, exact, out.resolve("algorithm_comparison.csv"));

        System.out.println("\nFiles saved in: " + out.toAbsolutePath());
        System.out.println("- timetable_greedy.csv");
        System.out.println("- timetable_exact.csv");
        System.out.println("- conflict_graph_greedy.dot and conflict_graph_exact.dot");
        System.out.println("- timetable_visualization.html (open in a browser)");
        System.out.println("- algorithm_comparison.csv");
        System.out.println("\nNote: exact is marked optimal only if the search completed a proof.");
    }

    static List<Set<Integer>> emptyGraph(int n) {
        List<Set<Integer>> g = new ArrayList<>();
        for (int i = 0; i < n; i++) g.add(new HashSet<>());
        return g;
    }

    static int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            try {
                int x = Integer.parseInt(sc.nextLine().trim());
                if (x >= min && x <= max) return x;
            } catch (Exception ignored) {}
            System.out.println("Please enter an integer from " + min + " to " + max + ".");
        }
    }

    static double readDouble(String prompt, double min, double max) {
        while (true) {
            System.out.print(prompt);
            try {
                double x = Double.parseDouble(sc.nextLine().trim());
                if (x >= min && x <= max) return x;
            } catch (Exception ignored) {}
            System.out.println("Please enter a number from " + min + " to " + max + ".");
        }
    }

    static int countEdges() {
        int total = 0;
        for (Set<Integer> neighbors : graph) total += neighbors.size();
        return total / 2;
    }

    static Result runGreedy() {
        long start = System.nanoTime();
        int[] color = new int[N];
        Arrays.fill(color, -1);
        int[] sizes = new int[N];
        int used = 0;

        for (int step = 0; step < N; step++) {
            int chosen = chooseDsaturVertex(color);
            boolean[] forbidden = new boolean[N];
            for (int neighbor : graph.get(chosen)) {
                if (color[neighbor] >= 0) forbidden[color[neighbor]] = true;
            }

            int slot = 0;
            while (slot < used && (forbidden[slot] || sizes[slot] >= R)) slot++;
            if (slot == used) used++;
            color[chosen] = slot;
            sizes[slot]++;
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        Result result = new Result("DSatur Greedy", color, used, ms, false);
        if (!isValid(result)) throw new IllegalStateException("Greedy produced an invalid timetable.");
        return result;
    }

    static int chooseDsaturVertex(int[] color) {
        int best = -1, bestSaturation = -1, bestDegree = -1;
        for (int v = 0; v < N; v++) {
            if (color[v] != -1) continue;
            Set<Integer> neighborColors = new HashSet<>();
            for (int u : graph.get(v)) if (color[u] != -1) neighborColors.add(color[u]);
            int saturation = neighborColors.size();
            int degree = graph.get(v).size();
            if (saturation > bestSaturation ||
                (saturation == bestSaturation && degree > bestDegree)) {
                best = v; bestSaturation = saturation; bestDegree = degree;
            }
        }
        return best;
    }

    static Result runExact(long timeLimitMs) {
        Result greedy = runGreedy();
        long start = System.nanoTime();
        ExactSearch search = new ExactSearch(timeLimitMs, start, greedy.slotCount);
        int lower = lowerBound();
        int[] bestAssignment = greedy.slots.clone();
        int bestCount = greedy.slotCount;
        boolean proved = false;

        for (int k = lower; k < bestCount; k++) {
            search.target = k;
            search.assignment = new int[N];
            Arrays.fill(search.assignment, -1);
            search.sizes = new int[k];
            search.nodes = 0;
            search.timedOut = false;
            if (search.solve(0)) {
                bestAssignment = search.assignment.clone();
                bestCount = k;
                proved = true; // first feasible k, all smaller k were infeasible
                break;
            }
            if (search.timedOut) break;
        }

        // If no smaller solution was found and every smaller k was exhaustively rejected,
        // the greedy count is proven optimal.
        if (!proved && !search.timedOut) proved = true;
        long ms = (System.nanoTime() - start) / 1_000_000;
        Result result = new Result("Exact Backtracking", bestAssignment, bestCount, ms, proved);
        if (!isValid(result)) throw new IllegalStateException("Exact method returned invalid timetable.");
        return result;
    }

    static int lowerBound() {
        // Safe lower bounds: capacity requires ceil(N/R) slots.
        // If at least one conflict edge exists, at least two slots are required.
        // Do NOT use maximum degree + 1 here: it is not a valid lower bound for
        // general graph colouring (for example, a star graph has high degree but
        // needs only two colours).
        int bound = Math.max(1, (N + R - 1) / R);
        if (countEdges() > 0) bound = Math.max(bound, 2);
        return bound;
    }

    static class ExactSearch {
        long deadline;
        int target;
        int[] assignment;
        int[] sizes;
        long nodes;
        boolean timedOut;

        ExactSearch(long limitMs, long start, int greedyCount) {
            deadline = start + limitMs * 1_000_000L;
        }

        boolean solve(int assignedCount) {
            if (System.nanoTime() > deadline) { timedOut = true; return false; }
            if (assignedCount == N) return true;

            int v = chooseDsaturVertex(assignment);
            boolean[] forbidden = new boolean[target];
            for (int u : graph.get(v)) {
                if (assignment[u] >= 0) forbidden[assignment[u]] = true;
            }

            // Symmetry breaking: use existing slots and at most one new slot at a time.
            int maxUsed = -1;
            for (int c : assignment) maxUsed = Math.max(maxUsed, c);
            int limit = Math.min(target - 1, maxUsed + 1);
            for (int slot = 0; slot <= limit; slot++) {
                if (sizes[slot] >= R || forbidden[slot]) continue;
                assignment[v] = slot;
                sizes[slot]++;
                if (solve(assignedCount + 1)) return true;
                sizes[slot]--;
                assignment[v] = -1;
                if (timedOut) return false;
            }
            return false;
        }
    }

    static boolean isValid(Result r) {
        int[] sizes = new int[r.slotCount];
        for (int v = 0; v < N; v++) {
            int c = r.slots[v];
            if (c < 0 || c >= r.slotCount) return false;
            sizes[c]++;
            if (sizes[c] > R) return false;
            for (int u : graph.get(v)) if (u > v && r.slots[u] == c) return false;
        }
        return true;
    }

    static void printResult(Result r) {
        System.out.println("Slots used: " + r.slotCount);
        System.out.println("Runtime: " + r.runtimeMs + " ms");
        System.out.println("Proven optimal: " + (r.optimal ? "YES" : "NO"));
        System.out.println("Valid timetable: " + isValid(r));
    }

    static void printTimetable(Result r) {
        for (int slot = 0; slot < r.slotCount; slot++) {
            System.out.print("Slot " + (slot + 1) + ": ");
            boolean first = true;
            for (int v = 0; v < N; v++) {
                if (r.slots[v] == slot) {
                    if (!first) System.out.print(", ");
                    System.out.print(v + 1);
                    first = false;
                }
            }
            System.out.println();
        }
    }

    static void exportCsv(Result r, Path file) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(file))) {
            pw.print("Slot");
            for (int h = 1; h <= R; h++) pw.print(",Hall " + h);
            pw.println();
            for (int slot = 0; slot < r.slotCount; slot++) {
                pw.print(slot + 1);
                int count = 0;
                for (int v = 0; v < N; v++) {
                    if (r.slots[v] == slot) {
                        pw.print(",");
                        pw.print(v + 1);
                        count++;
                    }
                }
                while (count++ < R) pw.print(",");
                pw.println();
            }
        }
    }

    static void exportComparisonCsv(Result a, Result b, Path file) throws IOException {
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(file))) {
            pw.println("Algorithm,Slots,Runtime_ms,Proven_Optimal");
            pw.println(a.algorithm + "," + a.slotCount + "," + a.runtimeMs + "," + a.optimal);
            pw.println(b.algorithm + "," + b.slotCount + "," + b.runtimeMs + "," + b.optimal);
        }
    }

    static void exportDot(Result r, Path file) throws IOException {
        String[] colors = {"#8ecae6", "#90be6d", "#ffb703", "#f4a261", "#cdb4db",
                           "#ffafcc", "#a8dadc", "#ffd6a5", "#bdb2ff", "#caffbf"};
        try (PrintWriter pw = new PrintWriter(Files.newBufferedWriter(file))) {
            pw.println("graph ConflictGraph {");
            pw.println("  node [style=filled, fontname=Arial];");
            for (int v = 0; v < N; v++) {
                pw.printf(Locale.US, "  %d [label=\"Course %d\", fillcolor=\"%s\"];%n",
                    v + 1, v + 1, colors[r.slots[v] % colors.length]);
            }
            for (int v = 0; v < N; v++)
                for (int u : graph.get(v)) if (u > v)
                    pw.printf("  %d -- %d;%n", v + 1, u + 1);
            pw.println("}");
        }
    }

    static void exportHtml(Result greedy, Result exact, Path file) throws IOException {
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html><html><head><meta charset='utf-8'><title>Exam Timetable</title>");
        html.append("<style>body{font-family:Arial;margin:24px;background:#f7f8fa;color:#222}table{border-collapse:collapse;margin:16px 0;background:white}td,th{border:1px solid #ccc;padding:10px;text-align:center}.slot{font-weight:bold}h1{color:#243b53}</style></head><body>");
        html.append("<h1>Exam Timetable Scheduler</h1>");
        appendHtmlTable(html, "DSatur Greedy", greedy);
        appendHtmlTable(html, "Exact Backtracking / Best Found", exact);
        html.append("<p>Graph files are exported as Graphviz DOT. The exact result is optimal only when explicitly marked proven.</p></body></html>");
        Files.writeString(file, html.toString());
    }

    static void appendHtmlTable(StringBuilder html, String title, Result r) {
        String[] colors = {"#8ecae6", "#90be6d", "#ffb703", "#f4a261", "#cdb4db",
                           "#ffafcc", "#a8dadc", "#ffd6a5", "#bdb2ff", "#caffbf"};
        html.append("<h2>").append(title).append("</h2><p>Slots: ").append(r.slotCount)
            .append(" | Runtime: ").append(r.runtimeMs).append(" ms | Proven optimal: ")
            .append(r.optimal).append("</p><table><tr><th>Slot</th>");
        for (int h = 1; h <= R; h++) html.append("<th>Hall ").append(h).append("</th>");
        html.append("</tr>");
        for (int s = 0; s < r.slotCount; s++) {
            html.append("<tr><td class='slot'>").append(s + 1).append("</td>");
            int count = 0;
            for (int v = 0; v < N; v++) if (r.slots[v] == s) {
                html.append("<td style='background:").append(colors[s % colors.length]).append("'>Course ").append(v + 1).append("</td>");
                count++;
            }
            while (count++ < R) html.append("<td></td>");
            html.append("</tr>");
        }
        html.append("</table>");
    }
}


