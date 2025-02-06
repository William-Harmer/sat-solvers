import java.io.*;
import java.util.*;
import java.util.concurrent.*;

public class Main {
    public static void main(String[] args) {
        String fileName = "datasets" + File.separator + "100_1_50_35_50.txt"; // Input file
        String outputFileName = "results.csv"; // Output CSV file

        try (BufferedReader br = new BufferedReader(new FileReader(fileName));
             FileWriter csvWriter = new FileWriter(outputFileName)) {

            // Write header to CSV file
            csvWriter.append("ID,Solver Type,Formula,Answer,Execution Time (seconds),Memory Used (MB)\n");

            String formula;
            int id = 1; // ID counter
            while ((formula = br.readLine()) != null) {
                // Skip empty lines or comments
                if (formula.trim().isEmpty() || formula.trim().startsWith("//")) {
                    continue;
                }

                System.out.println("Processing formula: " + formula);
                ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);

                // Run different solvers (keeping the same ID for the same formula)
                runSolver(csvWriter, id, "BruteForceEarlyStopping", clauses, BruteForce::bruteForceEarlyStopping, formula);
                runSolver(csvWriter, id, "BruteForce", clauses, BruteForce::bruteForce, formula);

                id += 1; // Increment ID for next formula after all solvers have run
            }

            System.out.println("Results saved to " + outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void runSolver(FileWriter csvWriter, int id, String solverType,
                                  ArrayList<ArrayList<Character>> clauses,
                                  SATSolver solver,
                                  String formula) throws IOException {
        long startTime = System.nanoTime();
        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long startMemory = runtime.totalMemory() - runtime.freeMemory();

        System.out.println("Running " + solverType + " solver...");

        // Create a callable for the solver
        Callable<Object> task = () -> solver.solve(clauses);

        // Use ExecutorService to run the solver with a timeout of 2 minutes (120 seconds)
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Object> future = executor.submit(task);

        Object result = null;
        try {
            result = future.get(1, TimeUnit.SECONDS);  // Wait for the result, with a 2-minute timeout
        } catch (TimeoutException e) {
            // If the task times out, cancel it and return "not finished"
            future.cancel(true);
            System.out.println(solverType + " solver took too long. Marking as 'not finished'.");
            result = "Not finished";
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        } finally {
            executor.shutdown();  // Always shut down the executor
        }

        long endTime = System.nanoTime();
        long endMemory = runtime.totalMemory() - runtime.freeMemory();

        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;
        double memoryUsedMB = (endMemory - startMemory) / (1024.0 * 1024.0);

        // Convert result to string representation, handle null cases
        String answer = convertResultToString(result);

        // Write results to CSV with an ID and answer
        System.out.println("Writing results to CSV...");
        csvWriter.append(id + "," + solverType + "," + formula + "," + answer + "," + durationInSeconds + "," + memoryUsedMB + "\n");
    }

    private static String convertResultToString(Object result) {
        if (result == null) {
            return "null";
        } else if (result == "Not finished"){
            return "Not finished";
        }

        // If the result is a HashSet of LinkedHashMaps
        if (result instanceof HashSet) {
            HashSet<LinkedHashMap<Character, Boolean>> set = (HashSet<LinkedHashMap<Character, Boolean>>) result;

            // If the HashSet is empty, return "null"
            if (set.isEmpty()) {
                return "null";
            }

            // Get the first element (if it exists)
            LinkedHashMap<Character, Boolean> firstAnswer = set.stream().findFirst().orElse(null);

            // Prepare the result string for the first answer
            StringBuilder answer = new StringBuilder();
            if (firstAnswer != null) {
                answer.append(firstAnswer.toString());  // First answer
            }

            // Add "plus x more" if there are additional answers
            int additionalAnswers = set.size() - 1;
            if (additionalAnswers > 0) {
                answer.append(" plus ").append(additionalAnswers).append(" more");
            }

            return "\"" + answer.toString() + "\"";  // Wrap in quotes for CSV
        }

        // Handle other result types (e.g., LinkedHashMap, HashMap, etc.)
        if (result instanceof LinkedHashMap) {
            return "\"" + result.toString() + "\"";  // Wrap in quotes for CSV
        } else if (result instanceof HashMap) {
            return "\"" + result.toString() + "\"";  // Wrap in quotes for CSV
        }

        return "Unsupported result type";
    }


    @FunctionalInterface
    interface SATSolver {
        Object solve(ArrayList<ArrayList<Character>> clauses);
    }
}
