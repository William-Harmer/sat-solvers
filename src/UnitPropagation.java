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

    public static void unitPropagation(ArrayList<ArrayList<Character>> clauses) {
        boolean formulaModified; // Track if the formula was modified

        do {
            formulaModified = false; // Reset flag at the start of each iteration

            for (int j = 0; j < clauses.size(); j++) { // For each clause
                ArrayList<Character> clause = clauses.get(j); // Get the clause

                if (clause.size() == 1) { // Check if it is a unit clause
                    char unitClauseElement = clause.getFirst(); // Get the unit clause element

                    for (int i = 0; i < clauses.size(); i++) { // For each clause
                        if (i == j) continue; // Apart from its own
                        ArrayList<Character> currentClause = clauses.get(i);

                        if (currentClause.contains(unitClauseElement)) { // If clause contains the unit clause element
                            clauses.remove(i); // Remove the whole clause
                            i--; // Adjust index
                            j--; // Adjust j due to removal
                            formulaModified = true; // Record modification
                        } else if (currentClause.contains(negate(unitClauseElement))) { // If negated version is found
                            clauses.get(i).remove((Character) negate(unitClauseElement)); // Remove the element
                            formulaModified = true; // Record modification
                        }
                    }

                    if (formulaModified) {
                        break; // Restart processing all unit clauses
                    }
                }
            }
        } while (formulaModified); // Continue until no modification occurs
    }


    public static void main(String[] args) {

        String formula = "(b)^(b)^(-bvc)^(Cve)";
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

}
