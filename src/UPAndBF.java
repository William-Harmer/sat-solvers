import java.io.*;
import java.util.*;

public class UPAndBF {
    public static LinkedHashMap<Character, Boolean> uPAndBF(String formula){
        System.out.println("Formula: " + formula);

        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);
        System.out.println("The 2D arraylist: " + clauses);

        UnitPropagation.unitPropagation(clauses);
        System.out.println("The 2D arraylist after unit propagation: " + clauses);

        return BruteForce.bruteForceEarlyStopping(clauses);
    }

    public static void main(String[] args) throws IOException {
        String formula = "(avb)^(cvB)^(avd)^(A)^(a)";
        LinkedHashMap<Character, Boolean> satAssignment = uPAndBF(formula);
        System.out.println(satAssignment);

    }
}







