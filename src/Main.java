import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    // Per solver timeout.
    private static final Map<String,Integer> TIMEOUT_MS_MAP = new HashMap<>();
    static {
        TIMEOUT_MS_MAP.put("BruteForce",                        1000);
        TIMEOUT_MS_MAP.put(SolverType.BruteForceEarlyStopping.name(), 1000);
        TIMEOUT_MS_MAP.put(SolverType.UPAndBF.name(),           1000);
        TIMEOUT_MS_MAP.put(SolverType.PLEAndBF.name(),          1000);
        TIMEOUT_MS_MAP.put(SolverType.UPAndPLEAndBF.name(),     1000);
        TIMEOUT_MS_MAP.put(SolverType.DPLL.name(),              1000);
        TIMEOUT_MS_MAP.put("CDCL",                              1000);
    }
    private static int getTimeout(String solverName) {
        return TIMEOUT_MS_MAP.getOrDefault(solverName, 250);
    }

    private static final boolean ENABLE_TIMEOUT_TRACKING = true;
    private static final int MAX_TIMEOUTS_PER_SOLVER = 10;
    private static final Map<String, Integer> consecutiveTimeoutCounters = new HashMap<>();

    public static void main(String[] args) throws IOException {

        String baseDir        = "C:\\Users\\willi\\OneDrive - University of East Anglia\\Demo";
        String fileName       = baseDir + File.separator + "5000_1_5000_50_50.txt";

        // ─── NEW OUTPUT DIRECTORY ───
        String outputDir      = "C:\\Users\\willi\\OneDrive - University of East Anglia\\Demo";
        String outputFileName = outputDir + File.separator
                + new File(fileName).getName().replace(".txt", ".csv");


        // ─── Initialize timeout counters ───
        for (SolverType type : SolverType.values()) {
            consecutiveTimeoutCounters.put(type.name(), 0);
        }
        consecutiveTimeoutCounters.put("BruteForce", 0);
        consecutiveTimeoutCounters.put("CDCL", 0);

        // ─── 1) Figure out how far we got last time ───
        File outFile = new File(outputFileName);
        int lastProcessedId = 0;
        if (outFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(outFile))) {
                br.readLine();  // skip header
                String line;
                while ((line = br.readLine()) != null) {
                    String[] cols = line.split(",", 2);
                    lastProcessedId = Math.max(lastProcessedId, Integer.parseInt(cols[0]));
                }
            }
        }

        // ─── 2) Open CSV in append mode & write header if new ───
        boolean needsHeader = !outFile.exists();
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, /* append = */ true));
        if (needsHeader) {
            writer.write(
                    "ID,Solver Type,Formula,Answer,Truth Values,Number of Literals,Execution Time (Seconds),Memory Used (MB)\n"
            );
            writer.flush();
        }
        // Make sure we close on SIGINT/kill:
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { writer.close(); } catch (IOException ignored) {}
        }));

        // ─── Warm-up ───
        System.out.println("Warming up JVM...");
        ArrayList<ArrayList<Character>> warmupClauses = Utility.formulaTo2DArrayList("(A)");
        BruteForce.bruteForce(warmupClauses);
        Runtime.getRuntime().gc();
        System.out.println("Warm-up complete. Starting actual formulas...");

        // ─── 3) Read & process formulas, skipping already-done ───
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String formulaLine;
            int id = 1;

            // fast-forward past completed IDs
            while (id <= lastProcessedId && (formulaLine = reader.readLine()) != null) {
                if (formulaLine.isBlank() || formulaLine.trim().startsWith("//")) {
                    continue;
                }
                id++;
            }

            // now resume from the next formula
            while ((formulaLine = reader.readLine()) != null) {
                if (formulaLine.isBlank() || formulaLine.trim().startsWith("//")) {
                    continue;
                }

                // parse formula + optional “!number”
                String[] parts = formulaLine.split("!");
                String formula = parts[0].trim();
                int formulaNumber = 0;
                if (parts.length > 1) {
                    try {
                        formulaNumber = Integer.parseInt(parts[1].trim());
                    } catch (NumberFormatException e) {
                        System.out.println("Error parsing formula number: " + parts[1]);
                        continue;
                    }
                }

                if (ENABLE_TIMEOUT_TRACKING && allSolversTimedOut()) {
                    System.out.println("All solvers have reached the timeout limit. Exiting.");
                    break;
                }

                System.out.println("\nProcessing formula ID: " + id);
                System.out.println("Number of literals: " + formulaNumber);

                var clauses     = Utility.formulaTo2DArrayList(formula);
                var cdclClauses = Utility.formulaToCDCLArrayList(formula);

                // each of these methods does writer.write(...) + writer.flush()
                if (!shouldSkipSolver("BruteForce")) {
                    runBruteForceSolver(Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver(SolverType.BruteForceEarlyStopping.name())) {
                    runSolver(SolverType.BruteForceEarlyStopping, Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver(SolverType.UPAndBF.name())) {
                    runSolver(SolverType.UPAndBF, Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver(SolverType.PLEAndBF.name())) {
                    runSolver(SolverType.PLEAndBF, Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver(SolverType.UPAndPLEAndBF.name())) {
                    runSolver(SolverType.UPAndPLEAndBF, Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver(SolverType.DPLL.name())) {
                    runSolver(SolverType.DPLL, Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver("CDCL")) {
                    runCDCLSolver(cdclClauses, writer, id, formula, formulaNumber);
                }

                id++;
            }
        } finally {
            // 4) Ensure the writer is closed on normal exit
            writer.close();
        }
    }

    private static boolean shouldSkipSolver(String solverName) {
        return ENABLE_TIMEOUT_TRACKING && consecutiveTimeoutCounters.getOrDefault(solverName, 0) >= MAX_TIMEOUTS_PER_SOLVER;
    }

    private static boolean allSolversTimedOut() {
        for (int count : consecutiveTimeoutCounters.values()) {
            if (count < MAX_TIMEOUTS_PER_SOLVER) {
                return false;
            }
        }
        return true;
    }

    private static void runSolver(SolverType solverType, ArrayList<ArrayList<Character>> clauses, BufferedWriter writer, int id, String formula, int formulaNumber) {
        int timeout = getTimeout(solverType.name());

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<HashMap<Character, Boolean>> solverTask = null;

        switch (solverType) {
            case BruteForceEarlyStopping -> solverTask = () -> BruteForce.bruteForceEarlyStopping(clauses);
            case UPAndBF -> solverTask = () -> UPAndBF.uPAndBF(clauses);
            case PLEAndBF -> solverTask = () -> PLEAndBF.pLEAAndBF(clauses);
            case UPAndPLEAndBF -> solverTask = () -> UPAndPLEAndBF.uPAndPLEAndBF(clauses);
            case DPLL -> solverTask = () -> DPLL.dPLL(clauses);
        }

        Future<HashMap<Character, Boolean>> future = executor.submit(solverTask);
        HashMap<Character, Boolean> assignment = null;
        boolean timedout = false;

        try {
            assignment = future.get(timeout, TimeUnit.MILLISECONDS);

        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println(solverType + " timed out after " + timeout + " ms.");

            timedout = true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        double memoryUsed = (memoryAfter - memoryBefore) / (1024.0 * 1024.0);

        String formattedTime = String.format("%.16f", elapsedTime);
        String formattedMemory = String.format("%.16f", memoryUsed);
        String answer = "Satisfiable";
        String truthValues = "None";

        if (timedout) {
            answer = "Not finished";
            if (ENABLE_TIMEOUT_TRACKING)
                consecutiveTimeoutCounters.put(solverType.name(), consecutiveTimeoutCounters.get(solverType.name()) + 1);
        } else if (assignment == null || assignment.isEmpty()) {
            answer = "Unsatisfiable";
            if (ENABLE_TIMEOUT_TRACKING)
                consecutiveTimeoutCounters.put(solverType.name(), 0);
        } else {
            truthValues = assignment.toString();
            if (ENABLE_TIMEOUT_TRACKING)
                consecutiveTimeoutCounters.put(solverType.name(), 0);
        }

        try {
            writer.write(id + "," + solverType + "," + "" + "," + answer + ",\"" + truthValues + "\"," + formulaNumber + "," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }

    private static void runBruteForceSolver(ArrayList<ArrayList<Character>> clauses, BufferedWriter writer, int id, String formula, int formulaNumber) {
        int timeout = getTimeout("BruteForce");

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<HashSet<HashMap<Character, Boolean>>> bruteForceTask = () -> BruteForce.bruteForce(clauses);
        Future<HashSet<HashMap<Character, Boolean>>> future = executor.submit(bruteForceTask);

        HashSet<HashMap<Character, Boolean>> allSatAssignments = null;
        boolean timedout = false;

        try {
            allSatAssignments = future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("BruteForce timed out after " + timeout + " ms.");
            timedout = true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        double memoryUsed = (memoryAfter - memoryBefore) / (1024.0 * 1024.0);

        String formattedTime = String.format("%.16f", elapsedTime);
        String formattedMemory = String.format("%.16f", memoryUsed);
        String answer = "Satisfiable";
        String truthValues = "None";

        if (timedout) {
            answer = "Not finished";
            if (ENABLE_TIMEOUT_TRACKING)
                consecutiveTimeoutCounters.put("BruteForce", consecutiveTimeoutCounters.get("BruteForce") + 1);
        } else if (allSatAssignments == null || allSatAssignments.isEmpty()) {
            answer = "Unsatisfiable";
            if (ENABLE_TIMEOUT_TRACKING)
                consecutiveTimeoutCounters.put("BruteForce", 0);
        } else {
            HashMap<Character, Boolean> firstAssignment = allSatAssignments.iterator().next();
            truthValues = firstAssignment.toString();
            if (allSatAssignments.size() > 1) {
                truthValues += " plus " + (allSatAssignments.size() - 1) + " more";
            }
            if (ENABLE_TIMEOUT_TRACKING)
                consecutiveTimeoutCounters.put("BruteForce", 0);
        }

        try {
            writer.write(id + ",BruteForce," + "" + "," + answer + ",\"" + truthValues + "\"," + formulaNumber + "," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }

    private static void runCDCLSolver(ArrayList<CDCLClause> CDCLClauses, BufferedWriter writer, int id, String formula, int formulaNumber) {
        int timeout = getTimeout("CDCL");

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<HashMap<Character, Boolean>> cdclTask = () -> CDCL.cDCL(CDCLClauses);
        Future<HashMap<Character, Boolean>> future = executor.submit(cdclTask);

        HashMap<Character, Boolean> assignment = null;
        boolean timedout = false;

        try {
            assignment = future.get(timeout, TimeUnit.MILLISECONDS);

        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("CDCL timed out after " + timeout + " ms.");
            timedout = true;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        double memoryUsed = (memoryAfter - memoryBefore) / (1024.0 * 1024.0);

        String formattedTime = String.format("%.16f", elapsedTime);
        String formattedMemory = String.format("%.16f", memoryUsed);
        String answer = "Satisfiable";
        String truthValues = "None";

        if (timedout) {
            answer = "Not finished";
            if (ENABLE_TIMEOUT_TRACKING)
                consecutiveTimeoutCounters.put("CDCL", consecutiveTimeoutCounters.get("CDCL") + 1);
        } else if (assignment == null || assignment.isEmpty()) {
            answer = "Unsatisfiable";
            if (ENABLE_TIMEOUT_TRACKING)
                consecutiveTimeoutCounters.put("CDCL", 0);
        } else {
            truthValues = assignment.toString();
            if (ENABLE_TIMEOUT_TRACKING)
                consecutiveTimeoutCounters.put("CDCL", 0);
        }

        try {
            writer.write(id + ",CDCL," + "" + "," + answer + ",\"" + truthValues + "\"," + formulaNumber + "," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }

    private enum SolverType {
        BruteForceEarlyStopping,
        UPAndBF,
        PLEAndBF,
        UPAndPLEAndBF,
        DPLL
    }
}
