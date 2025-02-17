import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class UPAndPLEAndBF {

    public static HashMap<Character, Boolean> uPAndPLEAndBF(ArrayList<ArrayList<Character>> clauses) {
        // Initialize literalTruthValues inside the method
        HashMap<Character, Boolean> literalTruthValues = new HashMap<>();

        System.out.println("The 2D arraylist: " + clauses);

        // Apply Unit Propagation
        UnitPropagation.unitPropagation(clauses);
        System.out.println("The 2D arraylist after unit propagation: " + clauses);

        // Apply Pure Literal Elimination
        PureLiteralElimination.pureLiteralElimination(clauses, literalTruthValues);
        System.out.println("The 2D arraylist after pure literal elimination: " + clauses);

        // Check if clauses are empty (SAT) or contain empty clauses (Unsatisfiable)
        if (clauses.isEmpty()) {
            // Formula is satisfiable after unit propagation and pure literal elimination
            System.out.println("SAT");
            return literalTruthValues;
        } else if (clauses.stream().anyMatch(ArrayList::isEmpty)) {
            // Formula is unsatisfiable due to empty clauses
            System.out.println("Not SAT");
            return null; // Return null to indicate unsatisfiable
        }

        // Solve using Brute Force if the formula is not immediately satisfiable or unsatisfiable
        HashMap<Character, Boolean> satAssignment = BruteForce.bruteForceEarlyStopping(clauses);
        System.out.println("SAT Assignment after brute force: " + satAssignment);

        // Merge results (combine the pure literal elimination with brute force solution)
        literalTruthValues.putAll(satAssignment);
        System.out.println("Combined Assignments: " + literalTruthValues);

        return literalTruthValues;  // Return the final truth assignments
    }

    public static void main(String[] args) throws IOException {
        String formula = "(a v b) ^ (a v B) ^ (c v A)";
        System.out.println("Formula: " + formula);

        // Parse formula into clauses
        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);

        // Call the function and store the result
        HashMap<Character, Boolean> literalTruthValues = uPAndPLEAndBF(clauses);

        // Final output
        if (literalTruthValues != null) {
            System.out.println("Final Combined Assignments: " + literalTruthValues);
        } else {
            System.out.println("Formula is not satisfiable.");
        }
    }
}
