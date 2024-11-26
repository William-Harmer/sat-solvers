import java.io.*;
import java.util.*;

public class UnitPropagation {

    private static char negate(char literal) {
        if (Character.isUpperCase(literal)) {
            return Character.toLowerCase(literal);
        } else {
            return Character.toUpperCase(literal);
        }
    }

    private static void unitPropagation(ArrayList<ArrayList<Character>> clauses){
        boolean unitClauseFound = true;
        while (unitClauseFound) {
            unitClauseFound = false;
//            System.out.println("Current Clauses: " + clauses); // Print current state of clauses

            // Check if only unit clauses are left, if so break the loop
            boolean allUnitClauses = true;
            for (ArrayList<Character> clause : clauses) { // Go through each clause
//                System.out.println("Checking clause for all unit clauses: " + clause);
                if (clause.size() != 1) { // If this clause is not a unit clause
                    allUnitClauses = false;
                    break; // No need to check further, we found a non-unit clause
                }
            }
            if (allUnitClauses) {
//                System.out.println("All clauses are unit clauses, breaking loop.");
                break;
            }

            for (int j = 0; j < clauses.size(); j++) { // For each clause
                ArrayList<Character> clause = clauses.get(j); // Get the clause
//                System.out.println("Checking if it is a unit clause at index " + j + ": " + clause);
                if (clause.size() == 1) { // If the clause is a unit clause
                    char unitClauseElement = clause.get(0); // Get the element
//                    System.out.println("Found unit clause element: " + unitClauseElement);
                    unitClauseFound = true;

                    for (int i = 0; i < clauses.size(); i++) { // For each clause

                        if (i == j) continue; // Apart from its own clause
                        ArrayList<Character> currentClause = clauses.get(i); // get the current clause
//                        System.out.println("Checking if unit clause is in this clause " + i + ": " + currentClause);

                        if (currentClause.contains(unitClauseElement)) { // If the clause contains the unit clause element
//                            System.out.println("Removing clause " + currentClause + " because it contains the unit clause element " + unitClauseElement);
                            clauses.remove(i); // Remove the whole clause
                            i--;
                            j--;

                        }  else if (currentClause.contains(negate(unitClauseElement))) { // If the negated version of the element is found
//                             System.out.println("Removing element " + negate(unitClauseElement) + " from clause " + currentClause);
                            clauses.get(i).remove((Character) negate(unitClauseElement));
                        }
                    }
                    break; // Break out of looping through all the clauses as we have just edited the formula so it needs to start again
                }
            }
        }
    }

    private static void solve(String formula) {
        // Break each clause up of the formula {a,^,c}{}{}{}{}
        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);

        // Print the array
        for (ArrayList<Character> row : clauses) {
            for (Character element : row) {
                System.out.print(element);
            }
            System.out.print(" ");
        }

        // Now apply unit propagation to formula
        unitPropagation(clauses);

        // Print the array
        System.out.println();
        for (ArrayList<Character> row : clauses) {
            for (Character element : row) {
                System.out.print(element);
            }
            System.out.print(" ");
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt")); // Open file for reading
        String line;

        while ((line = reader.readLine()) != null) { // Read each line of the file
            if (line.isEmpty() || line.startsWith("#")) { // If the line is empty or is a comment
                continue; // Break that iteration of the loop and go onto the next line
            }

            System.out.println("Formula: " + line);

            // Remove all whitespace from the line
            line =  line.replaceAll("\\s+", "");
            System.out.println(line);

//            long startTime = System.nanoTime(); // Start timer


            solve(line); // Solve that line
//            long endTime = System.nanoTime(); // End timer
//            long duration = (endTime - startTime) / 1_000; // Calculate the time taken in microseconds
//            System.out.println("Time taken: " + duration + " µs\n");
        }
        reader.close(); // Close file reader after going through all lines
    }

}
