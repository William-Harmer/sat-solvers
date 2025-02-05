import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Main {
    public static void main(String[] args) {
        String fileName = "datasets" + File.separator + "100_1_50_35_50.txt"; // Specify the input formula file
        String outputFileName = "results.csv"; // Specify the output CSV file name

        try (BufferedReader br = new BufferedReader(new FileReader(fileName));
             FileWriter csvWriter = new FileWriter(outputFileName)) {

            // Write header to CSV file
            csvWriter.append("Solver Type,Formula,Execution Time (seconds),Memory Used (MB)\n");

            String formula;
            while ((formula = br.readLine()) != null) {
                // Skip empty lines or lines starting with "//" (comments in the dataset)
                if (formula.trim().isEmpty() || formula.trim().startsWith("//")) {
                    continue;
                }

                // Process the line (convert the formula to 2D array of clauses)
                System.out.println("Processing formula: " + formula);
                ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);

                // Start timer for execution time
                long startTime = System.nanoTime();

                // Get initial memory usage before solving the SAT problem
                Runtime runtime = Runtime.getRuntime();
                runtime.gc(); // Suggest to garbage collect
                long startMemory = runtime.totalMemory() - runtime.freeMemory();

                // Apply brute-force SAT solver (early stopping) to the formula
                System.out.println("Running brute-force solver...");
                LinkedHashMap<Character, Boolean> satAssignment = BruteForce.bruteForceEarlyStopping(clauses);

                // Stop timer for execution time
                long endTime = System.nanoTime();

                // Get final memory usage after solving the SAT problem
                long endMemory = runtime.totalMemory() - runtime.freeMemory();

                // Calculate execution time in seconds
                double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;

                // Calculate memory used in MB
                double memoryUsedMB = (endMemory - startMemory) / (1024.0 * 1024.0);

                // Write the results to the CSV file
                System.out.println("Writing results to CSV...");
                csvWriter.append("BruteForce," + formula + "," + durationInSeconds + "," + memoryUsedMB + "\n");
            }

            // Confirmation message once all formulas have been processed
            System.out.println("Results saved to " + outputFileName);
        } catch (IOException e) {
            // Handle IO exceptions
            e.printStackTrace();
        }
    }
}
