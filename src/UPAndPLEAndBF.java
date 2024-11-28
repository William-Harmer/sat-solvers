import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class UPAndPLEAndBF {

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
            HashMap<Character, Boolean> pureLiteralsTruthValues =  new HashMap<>();
            do {
                formulaChanged = false;

                // Create a copy of the current clauses to detect changes
                ArrayList<ArrayList<Character>> previousClauses = new ArrayList<>();
                for (ArrayList<Character> clause : clauses) {
                    previousClauses.add(new ArrayList<>(clause)); // Deep copy of each clause
                }

                // Apply Unit Propagation
                UnitPropagation.unitPropagation(clauses);
                System.out.println("The 2D ArrayList after Unit Propagation: " + clauses);

                // Apply Pure Literal Elimination
                PureLiteralElimination.pureLiteralElimination(clauses, pureLiteralsTruthValues);
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
            // This formula changed system is really bad

            System.out.println("Final 2D ArrayList: " + clauses);

            if (clauses.isEmpty()){ // If arraylist is completely empty it is sat
                System.out.println("Sat");
                System.out.println(pureLiteralsTruthValues);
                continue;
            } else if (clauses.stream().anyMatch(List::isEmpty)) { // If the clause has at least one empty clause
                System.out.println("Not sat");
                continue;
            }

            // Otherwise brute force to find the rest and then add all truth values together

            System.out.println();
            LinkedHashMap<Character, Boolean> satAssignment = BruteForce.bruteForceEarlyStopping(clauses);
            System.out.println("Brute force with early stopping:");
            if (satAssignment.isEmpty()) {
                System.out.println("Not sat");
            } else {
                System.out.println("Sat");
                satAssignment.putAll(pureLiteralsTruthValues);
            }
        }
        reader.close();
    }

}
