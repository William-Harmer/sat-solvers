import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    private static final int TIMEOUT_SECONDS = 120;

    public static void main(String[] args) {
        String fileName = "datasets" + File.separator + "100_1_50_35_50.txt";
        String outputFileName = "results.csv";

        // Create a BufferedWriter to write to the CSV file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFileName))) {
            // Write header to the CSV file
            writer.write("ID,Solver Type,Formula,Answer,Truth Values,Execution Time (seconds),Memory Used (MB)\n");

            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            // WARMUP
            ///////////////////////////////////////////////////////////////////////////////////////////////////////////
            System.out.println("Warming up JVM...");
            ArrayList<ArrayList<Character>> warmupClauses = Utility.formulaTo2DArrayList("(A)");
            BruteForce.bruteForce(warmupClauses); // Warm-up for brute force solver
            Runtime.getRuntime().gc(); // Run garbage collection
            System.out.println("Warm-up complete. Starting actual formulas...\n");

            // Create a BufferedReader to read the file
            try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
                String formula;
                int id = 1;  // Start ID from 1

                // Iterate through each line of the file
                while ((formula = reader.readLine()) != null) {
                    if (formula.trim().isEmpty() || formula.trim().startsWith("//")) {
                        continue;
                    }

                    System.out.println("\n" + "Processing formula ID: " + id);
                    System.out.println("Formula: " + formula);
                    ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);
                    ArrayList<CDCLClause> CDCLClauses = Utility.formulaToCDCLArrayList(formula);

                    System.out.println("2D arraylist: " + clauses);

                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    // BRUTE FORCE
                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println("Performing BruteForce");
                    runBruteForceSolver(Utility.clauseCopy(clauses), writer, id, formula);

                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    // BRUTE FORCE EARLY STOPPING
                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println("Performing BruteForceEarlyStopping");
                    runSolver(SolverType.BruteForceEarlyStopping, Utility.clauseCopy(clauses), writer, id, formula);

                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    // UP + BF
                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println("Performing UPAndBF");
                    runSolver(SolverType.UPAndBF, Utility.clauseCopy(clauses), writer, id, formula);

                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    // PLE + BF
                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println("Performing PLEAndBF");
                    runSolver(SolverType.PLEAndBF, Utility.clauseCopy(clauses), writer, id, formula);

                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    // UP + PLE + BF
                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println("Performing UPAndPLEAndBF");
                    runSolver(SolverType.UPAndPLEAndBF, Utility.clauseCopy(clauses), writer, id, formula);

                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    // DPLL
                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println("Performing DPLL");
                    runSolver(SolverType.DPLL, Utility.clauseCopy(clauses), writer, id, formula);

                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    // CDCL
                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println("Performing CDCL");
                    runCDCLSolver(CDCLClauses, writer, id, formula);

                    id++;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Method that handles Brute Force and UP + PLE + BF logic
    private static void runSolver(SolverType solverType, ArrayList<ArrayList<Character>> clauses, BufferedWriter writer, int id, String formula) {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Garbage collection for accurate memory measurement
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        // Create an ExecutorService for handling time-limited computation
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<HashMap<Character, Boolean>> solverTask = null;

        if (solverType == SolverType.BruteForceEarlyStopping) {
            solverTask = () -> BruteForce.bruteForceEarlyStopping(clauses);
        } else if(solverType == SolverType.UPAndBF) {
            solverTask = () -> UPAndBF.uPAndBF(clauses);
        } else if (solverType == SolverType.PLEAndBF) {
            solverTask = () -> PLEAndBF.pLEAAndBF(clauses);
        } else if (solverType == SolverType.UPAndPLEAndBF) {
            solverTask = () -> UPAndPLEAndBF.uPAndPLEAndBF(clauses);
        } else if (solverType == SolverType.DPLL) {
            solverTask = () -> DPLL.dPLL(clauses);
        }

        Future<HashMap<Character, Boolean>> future = executor.submit(solverTask);
        HashMap<Character, Boolean> assignment = null;
        boolean timedout = false;

        try {
            assignment = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Solver timed out after " + TIMEOUT_SECONDS + " seconds.");
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
        } else if (assignment == null || assignment.isEmpty()) {
            answer = "Unsatisfiable";
        } else {
            truthValues = assignment.toString();
        }

        try {
            writer.write(id + "," + solverType + "," + formula + "," + answer + ",\"" + truthValues + "\"," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }

    // Brute Force Solver using HashSet<HashMap>
    private static void runBruteForceSolver(ArrayList<ArrayList<Character>> clauses, BufferedWriter writer, int id, String formula) {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Garbage collection for accurate memory measurement
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        // Create an ExecutorService for handling time-limited computation
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<HashSet<HashMap<Character, Boolean>>> bruteForceTask = () -> BruteForce.bruteForce(clauses);
        Future<HashSet<HashMap<Character, Boolean>>> future = executor.submit(bruteForceTask);

        HashSet<HashMap<Character, Boolean>> allSatAssignments = null;
        boolean timedout = false;

        try {
            allSatAssignments = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Solver timed out after " + TIMEOUT_SECONDS + " seconds.");
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
        } else if (allSatAssignments == null || allSatAssignments.isEmpty()) {
            answer = "Unsatisfiable";
        } else {
            HashMap<Character, Boolean> firstAssignment = allSatAssignments.iterator().next();
            truthValues = firstAssignment.toString();

            if (allSatAssignments.size() > 1) {
                truthValues += " plus " + (allSatAssignments.size() - 1) + " more";
            }
        }

        try {
            writer.write(id + ",BruteForce," + formula + "," + answer + ",\"" + truthValues + "\"," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }

    // Method that handles CDCL solver
    private static void runCDCLSolver(ArrayList<CDCLClause> CDCLClauses, BufferedWriter writer, int id, String formula) {
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Garbage collection for accurate memory measurement
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        // Create an ExecutorService for handling time-limited computation
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<HashMap<Character, Boolean>> cdclTask = () -> CDCL.cDCL(CDCLClauses);
        Future<HashMap<Character, Boolean>> future = executor.submit(cdclTask);

        HashMap<Character, Boolean> assignment = null;
        boolean timedout = false;

        try {
            assignment = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            future.cancel(true);
            System.out.println("Solver timed out after " + TIMEOUT_SECONDS + " seconds.");
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
        } else if (assignment == null || assignment.isEmpty()) {
            answer = "Unsatisfiable";
        } else {
            truthValues = assignment.toString();
        }

        try {
            writer.write(id + ",CDCL," + formula + "," + answer + ",\"" + truthValues + "\"," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        executor.shutdown();
    }

    // Enum for Solver Types to distinguish different solving methods
    private enum SolverType {
        BruteForceEarlyStopping,
        UPAndBF,
        PLEAndBF,
        UPAndPLEAndBF,
        DPLL
    }
}
