import java.io.*;
import java.util.ArrayList;

public class BruteForce {
    public static void bruteForce(ArrayList<ArrayList<Character>> clauses) {
        bruteForce(clauses, false);
    }

    public static void bruteForceEarlyStopping(ArrayList<ArrayList<Character>> clauses) {
        bruteForce(clauses, true);
    }

    private static void bruteForce(ArrayList<ArrayList<Character>> clauses, boolean earlyStopping) {
        // Get each literal in the clause (Maybe a set)

        // Assign each literal a truth value (Start with everything as false and go through all combinations)
        // For each possible combination
        // Assign the values onto the clauses arraylist and then check whether the formula is satisfiable
        // If it is, save those truth values, if you have early stopping to stop now and return what the truth values were

        // After going through all of them and keeping a track of all the truth values if they were satisfiable return them

        // Do I want it to print or to return????
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt")); // Open file for reading
        String line;

        while ((line = reader.readLine()) != null) { // Read each line of the file
            if (line.isEmpty() || line.startsWith("//")) { // If the line is empty or is a comment
                continue; // Break that iteration of the loop and go onto the next line
            }

            System.out.println("Formula: " + line);
            ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(line);
            System.out.println(clauses);
            bruteForce(clauses);
        }
        reader.close(); // Close file reader after going through all lines
    }
}
