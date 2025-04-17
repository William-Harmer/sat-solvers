import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    private static final int TIMEOUT_MS = 10;
    private static final boolean ENABLE_TIMEOUT_TRACKING = true;
    private static final int MAX_TIMEOUTS_PER_SOLVER = 10;
    private static final Map<String, Integer> consecutiveTimeoutCounters = new HashMap<>();

    public static void main(String[] args) {
        String fileName = "datasets+results" + File.separator + "40002_1_20001_50_50.txt";
        String outputFileName = "datasets+results" + File.separator + new File(fileName).getName().replace(".txt", ".csv");

        for (SolverType type : SolverType.values()) {
            consecutiveTimeoutCounters.put(type.name(), 0);
        }
        consecutiveTimeoutCounters.put("BruteForce", 0);
        consecutiveTimeoutCounters.put("CDCL", 0);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            writer.write("ID,Solver Type,Formula,Answer,Truth Values,Number of Literals,Execution Time (Seconds),Memory Used (MB)\n");

            System.out.println("Warming up JVM...");
            ArrayList<ArrayList<Character>> warmupClauses = Utility.formulaTo2DArrayList("(A)");
            BruteForce.bruteForce(warmupClauses);
            Runtime.getRuntime().gc();
            System.out.println("Warm-up complete. Starting actual formulas...");

            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String formula;
                int id = 1;

                while ((formula = reader.readLine()) != null) {
                    if (formula.trim().isEmpty() || formula.trim().startsWith("//")) {
                        continue;
                    }

                    String[] parts = formula.split("!");
                    formula = parts[0].trim();
                    int formulaNumber = 0;

                    if (parts.length > 1) {
                        try {
                            formulaNumber = Integer.parseInt(parts[1].trim());
                        } catch (NumberFormatException e) {
                            System.out.println("Error parsing the number after '!': " + parts[1]);
                            continue;
                        }
                    }

                    if (ENABLE_TIMEOUT_TRACKING && allSolversTimedOut()) {
                        System.out.println("All solvers have reached the timeout limit. Exiting.");
                        break;
                    }

                    System.out.println("\nProcessing formula ID: " + id);
                    System.out.println("Number of literals:" + formulaNumber);
                    ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);
                    ArrayList<CDCLClause> CDCLClauses = Utility.formulaToCDCLArrayList(formula);

                    if (!shouldSkipSolver("BruteForce")) {
                        System.out.println("Performing BruteForce");
                        runBruteForceSolver(Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                    }

                    if (!shouldSkipSolver(SolverType.BruteForceEarlyStopping.name())) {
                        System.out.println("Performing BruteForceEarlyStopping");
                        runSolver(SolverType.BruteForceEarlyStopping, Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                    }

                    if (!shouldSkipSolver(SolverType.UPAndBF.name())) {
                        System.out.println("Performing UPAndBF");
                        runSolver(SolverType.UPAndBF, Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                    }

                    if (!shouldSkipSolver(SolverType.PLEAndBF.name())) {
                        System.out.println("Performing PLEAndBF");
                        runSolver(SolverType.PLEAndBF, Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                    }

                    if (!shouldSkipSolver(SolverType.UPAndPLEAndBF.name())) {
                        System.out.println("Performing UPAndPLEAndBF");
                        runSolver(SolverType.UPAndPLEAndBF, Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                    }

                    if (!shouldSkipSolver(SolverType.DPLL.name())) {
                        System.out.println("Performing DPLL");
                        runSolver(SolverType.DPLL, Utility.clauseCopy(clauses), writer, id, formula, formulaNumber);
                    }

                    if (!shouldSkipSolver("CDCL")) {
                        System.out.println("Performing CDCL");
                        runCDCLSolver(CDCLClauses, writer, id, formula, formulaNumber);
                    }

                    id++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
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
            assignment = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Solver timed out after " + TIMEOUT_MS + " ms.");
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
            allSatAssignments = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Solver timed out after " + TIMEOUT_MS + " ms.");
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
            assignment = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Solver timed out after " + TIMEOUT_MS + " ms.");
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
