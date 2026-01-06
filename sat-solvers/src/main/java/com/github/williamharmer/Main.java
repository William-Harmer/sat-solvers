package com.github.williamharmer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.cnfparser.CNFParserCDCL;
import com.github.williamharmer.solvers.BruteForce;
import com.github.williamharmer.solvers.DPLL;
import com.github.williamharmer.solvers.PLEAndBF;
import com.github.williamharmer.solvers.UPAndBF;
import com.github.williamharmer.solvers.UPAndPLEAndBF;
import com.github.williamharmer.solvers.cdcl.CDCL;
import com.github.williamharmer.solvers.cdcl.CDCLClause;
import com.github.williamharmer.utilities.ClauseCopy;

public class Main {

    // Per-solver timeout configuration in milliseconds.
    // Keys must match either the enum name (for enum-based solvers)
    // or literal strings used in dispatch ("BruteForce", "CDCL").
    private static final Map<String,Integer> TIMEOUT_MS_MAP = new HashMap<>();
    static {
        // All solvers currently share a 1000 ms timeout.
        TIMEOUT_MS_MAP.put("BruteForce",                        1000);
        TIMEOUT_MS_MAP.put(SolverType.BruteForceEarlyStopping.name(), 1000);
        TIMEOUT_MS_MAP.put(SolverType.UPAndBF.name(),           1000);
        TIMEOUT_MS_MAP.put(SolverType.PLEAndBF.name(),          1000);
        TIMEOUT_MS_MAP.put(SolverType.UPAndPLEAndBF.name(),     1000);
        TIMEOUT_MS_MAP.put(SolverType.DPLL.name(),              1000);
        TIMEOUT_MS_MAP.put("CDCL",                              1000);
    }

    // Helper to fetch a timeout; falls back to 250 ms if missing.
    private static int getTimeout(String solverName) {
        return TIMEOUT_MS_MAP.getOrDefault(solverName, 250);
    }

    // Controls adaptive skipping: after N consecutive timeouts for a solver,
    // that solver will be skipped for the remainder of the run.
    private static final boolean ENABLE_TIMEOUT_TRACKING = true;
    private static final int MAX_TIMEOUTS_PER_SOLVER = 10;
    // Tracks consecutive timeouts per solver name (enum name or string key).
    private static final Map<String, Integer> consecutiveTimeoutCounters = new HashMap<>();

    public static void main(String[] args) throws IOException {

        // Input file name (relative to cnf-generator/formulae).
        // Adjust only this constant to switch datasets.
        String INPUT_FILE_NAME = "1000_1_500_50_50.txt";

        // Establish project-root-relative paths.
        Path projectRoot = Paths.get("").toAbsolutePath();

        // Input file: cnf-generator/formulae/<file>
        Path inputPath = projectRoot
            .resolve("cnf-generator")
            .resolve("formulae")
            .resolve(INPUT_FILE_NAME);

        // Output directory: sat-solvers/target/output (Maven-friendly)
        Path outputDir = projectRoot
            .resolve("sat-solvers")
            .resolve("target")
            .resolve("output");
        Files.createDirectories(outputDir);

        // CSV file name mirrors input but with .csv extension.
        String csvName = INPUT_FILE_NAME.replace(".txt", ".csv");
        Path outputPath = outputDir.resolve(csvName);

        // Resolve absolute string paths for IO.
        String fileName = inputPath.toString();
        String outputFileName = outputPath.toString();

        // ─── Initialize timeout counters for all tracked solvers ───
        for (SolverType type : SolverType.values()) {
            consecutiveTimeoutCounters.put(type.name(), 0);
        }
        // Non-enum solvers use explicit keys:
        consecutiveTimeoutCounters.put("BruteForce", 0);
        consecutiveTimeoutCounters.put("CDCL", 0);

        // ─── 1) Determine the last processed ID from an existing CSV ───
        // This allows resuming a previous run without reprocessing rows.
        File outFile = new File(outputFileName);
        int lastProcessedId = 0;
        if (outFile.exists()) {
            try (BufferedReader br = new BufferedReader(new FileReader(outFile))) {
                br.readLine();  // Skip header line
                String line;
                while ((line = br.readLine()) != null) {
                    // Each row starts with "ID,"; parse up to first comma.
                    String[] cols = line.split(",", 2);
                    lastProcessedId = Math.max(lastProcessedId, Integer.parseInt(cols[0]));
                }
            }
        }

        // ─── 2) Open CSV for appending and write header if new ───
        boolean needsHeader = !outFile.exists();
        BufferedWriter writer = new BufferedWriter(new FileWriter(outFile, /* append = */ true));
        if (needsHeader) {
            writer.write(
                    "ID,Solver Type,Formula,Answer,Truth Values,Number of Literals,Execution Time (Seconds),Memory Used (MB)\n"
            );
            writer.flush();
        }
        // Ensure writer closes cleanly on JVM shutdown (e.g., Ctrl+C).
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try { writer.close(); } catch (IOException ignored) {}
        }));

        // ─── Warm-up phase to mitigate JIT/GC cold-start effects ───
        System.out.println("Warming up JVM...");
        ArrayList<ArrayList<Character>> warmupClauses = CNFParser.formulaTo2DArrayList("(A)");
        BruteForce.bruteForce(warmupClauses);
        Runtime.getRuntime().gc();
        System.out.println("Warm-up complete. Starting actual formulas...");

        // ─── 3) Stream formulas from input file, resuming after lastProcessedId ───
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String formulaLine;
            int id = 1;

            // Fast-forward: consume lines up to lastProcessedId, skipping comments/blank.
            while (id <= lastProcessedId && (formulaLine = reader.readLine()) != null) {
                if (formulaLine.isBlank() || formulaLine.trim().startsWith("//")) {
                    continue;
                }
                id++;
            }

            // Main processing loop: each non-blank, non-comment line is a formula.
            while ((formulaLine = reader.readLine()) != null) {
                if (formulaLine.isBlank() || formulaLine.trim().startsWith("//")) {
                    continue;
                }

                // Input format allows an optional "!number" suffix for metadata
                // (e.g., presumed number of literals). Example: "(A v B) & (!C) !42"
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

                // Global stop: if every solver has hit the timeout cap, abort run.
                if (ENABLE_TIMEOUT_TRACKING && allSolversTimedOut()) {
                    System.out.println("All solvers have reached the timeout limit. Exiting.");
                    break;
                }

                System.out.println("\nProcessing formula ID: " + id);
                System.out.println("Number of literals: " + formulaNumber);

                // Parse the formula into structures expected by different solvers.
                // Non-CDCL solvers use 2D ArrayList<Character> clauses.
                var clauses     = CNFParser.formulaTo2DArrayList(formula);
                // CDCL solver uses a custom CDCLClause list.
                var cdclClauses = CNFParserCDCL.formulaToCDCLArrayList(formula);

                // For each solver:
                // - Respect per-solver skip if it has timed out too many times.
                // - Use ClauseCopy to avoid cross-solver mutation of shared data.
                // - Each run writes its CSV row immediately.

                if (!shouldSkipSolver("BruteForce")) {
                    runBruteForceSolver(ClauseCopy.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver(SolverType.BruteForceEarlyStopping.name())) {
                    runSolver(SolverType.BruteForceEarlyStopping, ClauseCopy.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver(SolverType.UPAndBF.name())) {
                    runSolver(SolverType.UPAndBF, ClauseCopy.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver(SolverType.PLEAndBF.name())) {
                    runSolver(SolverType.PLEAndBF, ClauseCopy.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver(SolverType.UPAndPLEAndBF.name())) {
                    runSolver(SolverType.UPAndPLEAndBF, ClauseCopy.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver(SolverType.DPLL.name())) {
                    runSolver(SolverType.DPLL, ClauseCopy.clauseCopy(clauses), writer, id, formula, formulaNumber);
                }
                if (!shouldSkipSolver("CDCL")) {
                    runCDCLSolver(cdclClauses, writer, id, formula, formulaNumber);
                }

                id++;
            }
        } finally {
            // 4) Ensure writer is closed on normal program termination.
            writer.close();
        }
    }

    // Returns true if a solver should be skipped due to hitting the timeout cap.
    private static boolean shouldSkipSolver(String solverName) {
        return ENABLE_TIMEOUT_TRACKING && consecutiveTimeoutCounters.getOrDefault(solverName, 0) >= MAX_TIMEOUTS_PER_SOLVER;
    }

    // Returns true if all tracked solvers reached the max consecutive timeouts.
    private static boolean allSolversTimedOut() {
        for (int count : consecutiveTimeoutCounters.values()) {
            if (count < MAX_TIMEOUTS_PER_SOLVER) {
                return false;
            }
        }
        return true;
    }

    // Generic runner for enum-based solvers (non-CDCL, non-full BruteForce).
    // Times the run, enforces timeout via Future.get(timeout),
    // gathers memory delta, updates timeout counters, and writes CSV.
    private static void runSolver(SolverType solverType, ArrayList<ArrayList<Character>> clauses, BufferedWriter writer, int id, String formula, int formulaNumber) {
        int timeout = getTimeout(solverType.name());

        // Memory/time baseline
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        // Run the solver in a single-threaded executor to enable cancellation on timeout.
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<HashMap<Character, Boolean>> solverTask = null;

        // Select the target algorithm based on enum value.
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
            // Enforce timeout; cancel on expiry.
            assignment = future.get(timeout, TimeUnit.MILLISECONDS);

        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println(solverType + " timed out after " + timeout + " ms.");

            timedout = true;
        } catch (InterruptedException | ExecutionException e) {
            // If the solver throws, print stack and record as failure (assignment stays null).
            e.printStackTrace();
        }

        // End timings and compute memory delta.
        long endTime = System.nanoTime();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        double memoryUsed = (memoryAfter - memoryBefore) / (1024.0 * 1024.0);

        // Prepare CSV fields
        String formattedTime = String.format("%.16f", elapsedTime);
        String formattedMemory = String.format("%.16f", memoryUsed);
        String answer = "Satisfiable";
        String truthValues = "None";

        // Interpret solver result:
        // - timedout => "Not finished"
        // - null/empty assignment => "Unsatisfiable"
        // - otherwise => "Satisfiable" with the assignment map
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

        // Write a CSV row for this solver and formula.
        try {
            // Note: The "Formula" column is intentionally written as empty (""), mirroring current behavior.
            writer.write(id + "," + solverType + "," + "" + "," + answer + ",\"" + truthValues + "\"," + formulaNumber + "," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Clean up the executor.
        executor.shutdown();
    }

    // Specialized runner for the exhaustive BruteForce solver that returns all satisfying assignments.
    private static void runBruteForceSolver(ArrayList<ArrayList<Character>> clauses, BufferedWriter writer, int id, String formula, int formulaNumber) {
        int timeout = getTimeout("BruteForce");

        // Memory/time baseline
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        // Execute in a cancellable single-thread executor.
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

        // End timings and compute memory delta.
        long endTime = System.nanoTime();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        double memoryUsed = (memoryAfter - memoryBefore) / (1024.0 * 1024.0);

        String formattedTime = String.format("%.16f", elapsedTime);
        String formattedMemory = String.format("%.16f", memoryUsed);
        String answer = "Satisfiable";
        String truthValues = "None";

        // Interpret results similarly to the generic runner,
        // but include a count of additional assignments if more than one exists.
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

        // Write a CSV row for BruteForce.
        try {
            writer.write(id + ",BruteForce," + "" + "," + answer + ",\"" + truthValues + "\"," + formulaNumber + "," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }

    // Specialized runner for the CDCL solver which uses its own clause representation.
    private static void runCDCLSolver(ArrayList<CDCLClause> CDCLClauses, BufferedWriter writer, int id, String formula, int formulaNumber) {
        int timeout = getTimeout("CDCL");

        // Memory/time baseline
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        // Execute CDCL in an interruptible executor with timeout control.
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

        // End timings and compute memory delta.
        long endTime = System.nanoTime();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        double memoryUsed = (memoryAfter - memoryBefore) / (1024.0 * 1024.0);

        String formattedTime = String.format("%.16f", elapsedTime);
        String formattedMemory = String.format("%.16f", memoryUsed);
        String answer = "Satisfiable";
        String truthValues = "None";

        // Interpret CDCL result as with generic solvers.
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

        // Write a CSV row for CDCL.
        try {
            writer.write(id + ",CDCL," + "" + "," + answer + ",\"" + truthValues + "\"," + formulaNumber + "," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }

    // Enum of solver strategies that share the same clause representation and runner method.
    private enum SolverType {
        BruteForceEarlyStopping,
        UPAndBF,
        PLEAndBF,
        UPAndPLEAndBF,
        DPLL
    }
}