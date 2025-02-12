import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.concurrent.*;

public class Main {
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

                    System.out.println("Formula: " + formula);
                    ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);
                    System.out.println("2D arraylist: " + clauses);

                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    // BRUTE FORCE
                    ///////////////////////////////////////////////////////////////////////////////////////////////////
                    System.out.println("Performing brute force");
                    Runtime runtime = Runtime.getRuntime();
                    runtime.gc(); // Garbage collection for accurate memory measurement
                    long memoryBefore = runtime.totalMemory() - runtime.freeMemory();
                    long startTime = System.nanoTime();

                    // Submit the brute force task to the executor
                    Callable<HashSet<LinkedHashMap<Character, Boolean>>> bruteForceTask = () -> BruteForce.bruteForce(clauses);
                    Future<HashSet<LinkedHashMap<Character, Boolean>>> future = executor.submit(bruteForceTask);

                    // Set a time limit of 2 minutes (120 seconds)
                    HashSet<LinkedHashMap<Character, Boolean>> allSatAssignments = null;
                    try {
                        allSatAssignments = future.get(2, TimeUnit.SECONDS);
                    } catch (TimeoutException e) {
                        // If time exceeds 2 minutes, cancel the task and set the result to Not finished
                        future.cancel(true);
                        System.out.println("Solver timed out after 2 minutes.");
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

                    // If the solver timed out or the result is empty, update the answer
                    if (allSatAssignments == null || allSatAssignments.isEmpty()) {
                        answer = "Unsatisfiable";
                    }

                    // If the task was canceled due to timeout, mark it as "Not finished"
                    if (allSatAssignments == null) {
                        answer = "Not finished";
                    } else {
                        // Extract the first truth assignment
                        if (!allSatAssignments.isEmpty()) {
                            LinkedHashMap<Character, Boolean> firstAssignment = allSatAssignments.iterator().next();
                            truthValues = firstAssignment.toString();

                            // Append "plus X more" if there are additional assignments
                            if (allSatAssignments.size() > 1) {
                                truthValues += " plus " + (allSatAssignments.size() - 1) + " more";
                            }
                        }
                    }

                    // Write results to CSV (Truth Values right after Answer, keeping {} in the same column)
                    writer.write(id + ",Brute Force," + formula + "," + answer + ",\"" + truthValues + "\"," + formattedTime + "," + formattedMemory + "\n");
                    writer.flush();

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
}
