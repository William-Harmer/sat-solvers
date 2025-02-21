import java.io.*;
import java.util.*;

public class UnitPropagation {

    public static void unitPropagation(ArrayList<ArrayList<Character>> clauses) {
        boolean formulaModified; // Track if the formula was modified

        do {
            System.out.println(clauses);
            formulaModified = false; // Reset flag at the start of each iteration

            for (int j = 0; j < clauses.size(); j++) { // For each clause
                ArrayList<Character> clause = clauses.get(j); // Get the clause

                if (clause.size() == 1) { // Check if it is a unit clause
                    char unitClauseElement = clause.getFirst(); // Get the unit clause element
                    System.out.println("UNIT CLAUSE FOUND: " + unitClauseElement);

                    for (int i = 0; i < clauses.size(); i++) { // For each clause
                        if (i == j) continue; // Apart from its own

                        if (clauses.get(i).contains(unitClauseElement)) { // If clause contains the unit clause element
                            System.out.println("Clause " + i + " contains the unit element " + unitClauseElement);
                            clauses.remove(i); // Remove the whole clause
                            i--; // Adjust index
                            j--; // Adjust j due to removal
                            formulaModified = true; // Record modification
                            System.out.println("Removed clause " + i + ", new clauses: " + clauses);

                        } else if (clauses.get(i).contains(Utility.negate(unitClauseElement))) {
                            // If negated version is found

                            clauses.get(i).remove((Character) Utility.negate(unitClauseElement));
                            formulaModified = true; // Record modification
                            System.out.println("Clause " + i + " has " + Utility.negate(unitClauseElement));
                            System.out.println("New formula: " + clauses);
                        }
                    }

                    if (formulaModified) {
                        System.out.println("Formula modified, restarting processing unit clauses");
                        break; // Restart processing all unit clauses
                    }
                }
            }
        } while (formulaModified); // Continue until no modification occurs
    }

    public static void main(String[] args) {

        String formula = "(bc)(bC)(Bd)(BD)(EF)(A)(B)";
        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);

        // Print the array

        // Now apply unit propagation to formula
        unitPropagation(clauses);

        // Print the array
        System.out.println(clauses);
    }

}
