import java.io.*;
import java.util.*;

public class UnitPropagation {

    public static void unitPropagation(ArrayList<ArrayList<Character>> clauses) {
        boolean formulaModified; // Track if the formula was modified

        do {
            formulaModified = false; // Reset flag at the start of each iteration
//            System.out.println("Starting a new iteration");

            for (int j = 0; j < clauses.size(); j++) { // For each clause
                ArrayList<Character> clause = clauses.get(j); // Get the clause
//                System.out.println("Checking clause " + j + ": " + clause);

                if (clause.size() == 1) { // Check if it is a unit clause
                    char unitClauseElement = clause.getFirst(); // Get the unit clause element
//                    System.out.println("Found unit clause with element: " + unitClauseElement);

                    for (int i = 0; i < clauses.size(); i++) { // For each clause
                        if (i == j) continue; // Apart from its own
//                        System.out.println("Checking clause " + i + ": " + currentClause);

                        if (clauses.get(i).contains(unitClauseElement)) { // If clause contains the unit clause element
//                            System.out.println("Clause " + i + " contains the unit element " + unitClauseElement);
                            clauses.remove(i); // Remove the whole clause
                            i--; // Adjust index
                            j--; // Adjust j due to removal
                            formulaModified = true; // Record modification
//                            System.out.println("Removed clause " + i + ", new clauses: " + clauses);
                        } else if (clauses.get(i).contains(Utility.negate(unitClauseElement))) { // If negated version is found
                            if(clauses.get(i).size() == 1) { // If the negated literal is just by itself in a clause, remove the whole clause
                                clauses.remove(i);
                                i--;
                                j--;
                            } else { // Otherwise just remove that element
                                clauses.get(i).remove((Character) Utility.negate(unitClauseElement));
                            }
                            formulaModified = true; // Record modification

//                            System.out.println("Clause " + i + " contains the negation of " + unitClauseElement);
//                            System.out.println("Updated clause " + i + ": " + currentClause);
                        }
                    }

                    if (formulaModified) {
//                        System.out.println("Formula modified, restarting processing unit clauses");
                        break; // Restart processing all unit clauses
                    }
                }
            }
        } while (formulaModified); // Continue until no modification occurs
    }

    public static void main(String[] args) {

        String formula = "(b)^(bvc)^(Bvc)^(B)";
        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);

        // Print the array
        System.out.println(clauses);

        // Now apply unit propagation to formula
        unitPropagation(clauses);

        // Print the array
        System.out.println(clauses);
    }

}
