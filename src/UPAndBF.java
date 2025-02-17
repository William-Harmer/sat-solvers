import java.io.*;
import java.util.*;

public class UPAndBF {

    public static HashMap<Character, Boolean> uPAndBF(ArrayList<ArrayList<Character>> clauses) {
//        System.out.println("The 2D arraylist: " + clauses);

        UnitPropagation.unitPropagation(clauses);
//        System.out.println("The 2D arraylist after unit propagation: " + clauses);

        if (clauses.stream().anyMatch(ArrayList::isEmpty)) {
            return null;
        }

        return BruteForce.bruteForceEarlyStopping(clauses);
    }

    public static void main(String[] args) throws IOException {
        String formula = "(avb)^(cvB)^(avd)^(A)^(a)";
        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);

        HashMap<Character, Boolean> satAssignment = uPAndBF(clauses); // Changed to HashMap
        if (satAssignment != null) {
            System.out.println("SAT Assignment: " + satAssignment);
        } else {
            System.out.println("Formula is unsatisfiable.");
        }
    }
}
