import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {

    private static final int TIMEOUT_SECONDS = 1;

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

                // Create an ExecutorService for handling time-limited computation
                ExecutorService executor = Executors.newSingleThreadExecutor();

                // Iterate through each line of the file
                while ((formula = reader.readLine()) != null) {
                    if (formula.trim().isEmpty() || formula.trim().startsWith("//")) {
                        continue;
                    }

                    System.out.println("\n" + "Processing formula ID: " + id);
                    System.out.println("Formula: " + formula);
                    ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);
                    System.out.println("2D arraylist: " + clauses);

                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    // BRUTE FORCE
                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println("Performing brute force");
                    runBruteForceSolver(clauses, writer, id, formula);

                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    // BRUTE FORCE EARLY STOPPING
                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println("Performing brute force with early stopping");
                    runBruteForceEarlyStoppingSolver(clauses, writer, id, formula);

                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    // UP + PLE + BF
                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println("Performing UPAndPLEAndBF");
                    runUPAndPLEAndBFSolver(clauses, writer, id, formula);

                    id++;
                }

                executor.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Hashmap
    private static void runUPAndPLEAndBFSolver(ArrayList<ArrayList<Character>> clauses, BufferedWriter writer, int id, String formula) {
        // Perform the UP, PLE, and Brute Force logic
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Garbage collection for accurate memory measurement
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        // Create an ExecutorService for handling time-limited computation
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<HashMap<Character, Boolean>> solverTask = () -> UPAndPLEAndBF.uPAndPLEAndBF(clauses);
        Future<HashMap<Character, Boolean>> future = executor.submit(solverTask);

        // Use the constant TIMEOUT_SECONDS for the timeout duration
        HashMap<Character, Boolean> assignment = null;
        boolean timedout = false; // Flag to check if the solver timed out

        try {
            assignment = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // If time exceeds TIMEOUT_SECONDS, cancel the task and set timedout to true
            future.cancel(true);
            System.out.println("Solver timed out after " + TIMEOUT_SECONDS + " seconds.");
            timedout = true;  // Set the timedout flag to true
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        double memoryUsed = (memoryAfter - memoryBefore) / (1024.0 * 1024.0);

        String formattedTime = String.format("%.16f", elapsedTime);
        String formattedMemory = String.format("%.16f", memoryUsed);

        String answer = "Satisfiable"; // Default is satisfiable
        String truthValues = "None";

        // Use timedout flag to determine answer
        if (timedout) {
            answer = "Not finished";
        } else if (assignment == null || assignment.isEmpty()) {
            // If assignment is null or empty, formula is unsatisfiable
            answer = "Unsatisfiable";
        } else {
            // Extract the truth assignment and format it
            truthValues = assignment.toString();
        }

        // Write results to CSV (Truth Values right after Answer, keeping {} in the same column)
        try {
            writer.write(id + ",UPAndPLEAndBF," + formula + "," + answer + ",\"" + truthValues + "\"," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }

    // Hashset of linked hashmaps
    private static void runBruteForceSolver(ArrayList<ArrayList<Character>> clauses, BufferedWriter writer, int id, String formula) {
        // Perform the brute force logic
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Garbage collection for accurate memory measurement
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        // Create an ExecutorService for handling time-limited computation
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<HashSet<LinkedHashMap<Character, Boolean>>> bruteForceTask = () -> BruteForce.bruteForce(clauses);
        Future<HashSet<LinkedHashMap<Character, Boolean>>> future = executor.submit(bruteForceTask);

        // Use the constant TIMEOUT_SECONDS for the timeout duration
        HashSet<LinkedHashMap<Character, Boolean>> allSatAssignments = null;
        boolean timedout = false; // Flag to check if the solver timed out

        try {
            allSatAssignments = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // If time exceeds TIMEOUT_SECONDS, cancel the task and set timedout to true
            future.cancel(true);
            System.out.println("Solver timed out after " + TIMEOUT_SECONDS + " seconds.");
            timedout = true;  // Set the timedout flag to true
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        double memoryUsed = (memoryAfter - memoryBefore) / (1024.0 * 1024.0);

        String formattedTime = String.format("%.16f", elapsedTime);
        String formattedMemory = String.format("%.16f", memoryUsed);

        String answer = "Satisfiable"; // Default is satisfiable
        String truthValues = "None";

        // Use timedout flag to determine answer
        if (timedout) {
            answer = "Not finished";
        } else if (allSatAssignments == null || allSatAssignments.isEmpty()) {
            // If no assignment found, the formula is unsatisfiable
            answer = "Unsatisfiable";
        } else {
            // Extract the first truth assignment and format it
            LinkedHashMap<Character, Boolean> firstAssignment = allSatAssignments.iterator().next();
            truthValues = firstAssignment.toString();

            // Append "plus X more" if there are additional assignments
            if (allSatAssignments.size() > 1) {
                truthValues += " plus " + (allSatAssignments.size() - 1) + " more";
            }
        }

        // Write results to CSV (Truth Values right after Answer, keeping {} in the same column)
        try {
            writer.write(id + ",Brute Force," + formula + "," + answer + ",\"" + truthValues + "\"," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }

    // Linked hashmap
    private static void runBruteForceEarlyStoppingSolver(ArrayList<ArrayList<Character>> clauses, BufferedWriter writer, int id, String formula) {
        // Perform the brute force early stopping logic
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Garbage collection for accurate memory measurement
        long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
        long startTime = System.nanoTime();

        // Create an ExecutorService for handling time-limited computation
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Callable<LinkedHashMap<Character, Boolean>> bruteForceTask = () -> BruteForce.bruteForceEarlyStopping(clauses);
        Future<LinkedHashMap<Character, Boolean>> future = executor.submit(bruteForceTask);

        // Use the constant TIMEOUT_SECONDS for the timeout duration
        LinkedHashMap<Character, Boolean> assignment = null;
        boolean timedout = false; // Flag to check if the solver timed out

        try {
            assignment = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            // If time exceeds TIMEOUT_SECONDS, cancel the task and set timedout to true
            future.cancel(true);
            System.out.println("Solver timed out after " + TIMEOUT_SECONDS + " seconds.");
            timedout = true;  // Set the timedout flag to true
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        long endTime = System.nanoTime();
        long memoryAfter = runtime.totalMemory() - runtime.freeMemory();

        double elapsedTime = (endTime - startTime) / 1_000_000_000.0;
        double memoryUsed = (memoryAfter - memoryBefore) / (1024.0 * 1024.0);

        String formattedTime = String.format("%.16f", elapsedTime);
        String formattedMemory = String.format("%.16f", memoryUsed);

        String answer = "Satisfiable"; // Default is satisfiable
        String truthValues = "None";

        // Use timedout flag to determine answer
        if (timedout) {
            answer = "Not finished";
        } else if (assignment == null || assignment.isEmpty()) {
            // If assignment is null or empty, formula is unsatisfiable
            answer = "Unsatisfiable";
        } else {
            // Extract the truth assignment and format it
            truthValues = assignment.toString();
        }

        // Write results to CSV (Truth Values right after Answer, keeping {} in the same column)
        try {
            writer.write(id + ",Brute Force Early Stopping," + formula + "," + answer + ",\"" + truthValues + "\"," + formattedTime + "," + formattedMemory + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        executor.shutdown();
    }

}
