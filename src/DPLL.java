import java.util.ArrayList;
import java.util.HashMap;

public class DPLL {

    private static boolean executeDPLL(ArrayList<ArrayList<Character>> clauses, HashMap<Character, Boolean> literalTruthValues){
        System.out.println("Formula:" + clauses);
        // Is there ever a scenario where you need to unit prop again after pure literal?? With my testing I think no

        UnitPropagation.unitPropagation(clauses);
        System.out.println("Unit prop: " + clauses);

        PureLiteralElimination.pureLiteralElimination(clauses, literalTruthValues);
        System.out.println("Pure lit: " + clauses);
        System.out.println(literalTruthValues);

        if(clauses.isEmpty()){ // SAT
            System.out.println("SAT");
            return true;
        } else if (clauses.stream().anyMatch(ArrayList::isEmpty)) { // Not SAT
            System.out.println("Not sat");
            return false;
        }

        System.out.println("Recursing");

        // Create copies to preserve the original clause structure and truth table
        ArrayList<ArrayList<Character>> clausesCopy = deepCopy(clauses);
        HashMap<Character, Boolean> truthTableCopy = deepCopyTruthTable(literalTruthValues);

        return executeDPLL(addFirstElementAsNewClauseToFormula(clauses, true), literalTruthValues)
                || executeDPLL(addFirstElementAsNewClauseToFormula(clausesCopy, false), truthTableCopy);
    }

    private static ArrayList<ArrayList<Character>> addFirstElementAsNewClauseToFormula(ArrayList<ArrayList<Character>> clauses, boolean negated){
        ArrayList<Character> newClause = new ArrayList<>();
        System.out.println(clauses);
        clauses.add(newClause);
        if(negated){
            clauses.get(clauses.size() - 1).add(Utility.negate(clauses.get(0).get(0)));
            System.out.println(clauses);
        } else {
            clauses.get(clauses.size() - 1).add(clauses.get(0).get(0));
            System.out.println(clauses);
        }
        return clauses;
    }

    private static ArrayList<ArrayList<Character>> deepCopy(ArrayList<ArrayList<Character>> clauses) {
        ArrayList<ArrayList<Character>> copiedClauses = new ArrayList<>();
        for (ArrayList<Character> clause : clauses) {
            copiedClauses.add(new ArrayList<>(clause)); // Create a new ArrayList for each clause
        }
        return copiedClauses;
    }

    private static HashMap<Character, Boolean> deepCopyTruthTable(HashMap<Character, Boolean> originalTruthTable) {
        return new HashMap<>(originalTruthTable);
    }

    // New secondary function to get the literal truth values
    public static HashMap<Character, Boolean> dPLL(ArrayList<ArrayList<Character>> clauses) {
        HashMap<Character, Boolean> literalTruthValues = new HashMap<>();
        boolean result = executeDPLL(clauses, literalTruthValues);

        if (result) {
            System.out.println("DPLL Succeeded, Final truth values: " + literalTruthValues);
        } else {
            System.out.println("DPLL Failed to satisfy the formula.");
        }

        return literalTruthValues;
    }

    public static void main(String[] args) {
        String formula = "(avb)^(avB)^(cvA)";
        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);
        System.out.println("Initial formula clauses: " + clauses);

        // Get literal truth values by calling the new function
        HashMap<Character, Boolean> literalTruthValues = dPLL(clauses);
        System.out.println("Final literal truth values: " + literalTruthValues);
    }
}
