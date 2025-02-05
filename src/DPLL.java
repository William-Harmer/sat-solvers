import java.util.ArrayList;
import java.util.HashMap;

public class DPLL {

    private static boolean dPLL(ArrayList<ArrayList<Character>> clauses, HashMap<Character, Boolean> literalTruthValues){
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

        ArrayList<ArrayList<Character>> clausesCopy = deepCopy(clauses);
        HashMap<Character, Boolean> truthTableCopy = deepCopyTruthTable(literalTruthValues);


        return dPLL(addFirstElementAsNewClauseToFormula(clauses, true), literalTruthValues)
                || dPLL(addFirstElementAsNewClauseToFormula(clausesCopy, false), truthTableCopy);
    }

    private static ArrayList<ArrayList<Character>> addFirstElementAsNewClauseToFormula(ArrayList<ArrayList<Character>> clauses, boolean negated){
        ArrayList<Character> newClause = new ArrayList<>();
        System.out.println(clauses);
        clauses.add(newClause);
        if(negated){
            clauses.getLast().add(Utility.negate(clauses.getFirst().getFirst()));
            System.out.println(clauses);
        } else {
            clauses.getLast().add(clauses.getFirst().getFirst());
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

    public static void main(String[] args) {
        String formula = "(avb)^(avB)^(cvA)";
        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);
        System.out.println(clauses);
        HashMap<Character, Boolean> literalTruthValues = new HashMap<>();
        dPLL(clauses, literalTruthValues);
        System.out.println("Values i got:" + literalTruthValues);
    }

}
