import java.io.*;
import java.util.*;

public class UPAndBF {
    public static void main(String[] args) throws IOException {
        String formula = "(avb)^(cvB)";

        System.out.println("Formula: " + formula);
        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);
        System.out.println("The 2D arraylist: " + clauses);
        UnitPropagation.unitPropagation(clauses);
        System.out.println("The 2D arraylist after unit propagation: " + clauses);
        LinkedHashMap<Character, Boolean> satAssignment = BruteForce.bruteForceEarlyStopping(clauses);
        System.out.println();
        System.out.println("Brute force with early stopping:");
        System.out.println(satAssignment);
        System.out.println();
    }
}

