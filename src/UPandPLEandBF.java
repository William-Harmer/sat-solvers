import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class UPandPLEandBF {

    // Unit prop
    // pure lit
    // if changes were made, go back to unit prop
    // If no changes were made, brute force the rest

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt")); // Open file for reading
        String line;

        while ((line = reader.readLine()) != null) { // Read each line of the file
            if (line.isEmpty() || line.startsWith("//")) { // If the line is empty or is a comment
                continue; // Break that iteration of the loop and go onto the next line
            }

            System.out.println("Formula: " + line);
            ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(line);
            System.out.println("The 2D arraylist: " + clauses);

            boolean formulaChanged;
            do {
                formulaChanged = false;

                // Create a deep copy of the current clauses to detect changes
                ArrayList<ArrayList<Character>> previousClauses = new ArrayList<>();
                for (ArrayList<Character> clause : clauses) {
                    previousClauses.add(new ArrayList<>(clause)); // Deep copy of each clause
                }

                // Apply Unit Propagation
                UnitPropagation.unitPropagation(clauses);
                System.out.println("The 2D ArrayList after Unit Propagation: " + clauses);

                // Apply Pure Literal Elimination
                HashMap<Character, Boolean> pureLiteralsTruthValues = PureLiteralElimination.pureLiteralElimination(clauses);
                System.out.println("The truth values of the pure literals: " + pureLiteralsTruthValues);
                System.out.println("The 2D ArrayList after Pure Literal Elimination: " + clauses);

                // Check if the clauses content has changed
                if (!Objects.equals(previousClauses, clauses)) {
                    System.out.println("Content has changed going again");
                    formulaChanged = true;
                } else {
                    System.out.println("Content has not changed not going again");
                }

            } while (formulaChanged);

            System.out.println("Final 2D ArrayList: " + clauses);


            // Need to sort out the fact that the truth values dont save after wrapping around the second time


            // If the clause is completely empty it means it is sat, end here and give values
            // If the clause has at least one empty clause, it means it is not sat, end here and give values

            // Need to add pureLiteralsTruthValues with the other truth values, but make sure to only do that if sat
            System.out.println();
            LinkedHashMap<Character, Boolean> satAssignment = BruteForce.bruteForceEarlyStopping(clauses);
            System.out.println("Brute force with early stopping:");
            System.out.println(satAssignment);
            System.out.println();

        }
        reader.close();
    }

}
