import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class UPAndPLEAndBF {

    public static LinkedHashMap<Character, Boolean> uPAndPLEAndBF(String formula, HashMap<Character, Boolean> literalTruthValues) {
        System.out.println("Formula: " + formula);

        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);
        System.out.println("The 2D arraylist: " + clauses);

        UnitPropagation.unitPropagation(clauses);
        System.out.println("The 2D arraylist after unit propagation: " + clauses);

        PureLiteralElimination.pureLiteralElimination(clauses, literalTruthValues);
        System.out.println("The 2D arraylist after pure literal elimination: " + clauses);

        return BruteForce.bruteForceEarlyStopping(clauses);
    }

    public static void main(String[] args) throws IOException {
        String formula = "(avb)^(avB)^(cvA)";
        HashMap<Character, Boolean> literalTruthValues = new HashMap<>();
        LinkedHashMap<Character, Boolean> satAssignment = uPAndPLEAndBF(formula, literalTruthValues);

        // Could combine these two data structures later if I needed to
        System.out.println(satAssignment);
        System.out.println(literalTruthValues);
    }
}
