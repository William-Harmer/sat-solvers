package com.github.williamharmer.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.simplifications.PureLiteralElimination;
import com.github.williamharmer.simplifications.UnitPropagation;

public class UPAndPLEAndBF {

    public static HashMap<Character, Boolean> uPAndPLEAndBF(ArrayList<ArrayList<Character>> clauses) {
        // Initialize literalTruthValues inside the method
        HashMap<Character, Boolean> literalTruthValues = new HashMap<>();

//        System.out.println("The 2D arraylist: " + clauses);

        // Apply Unit Propagation
        UnitPropagation.unitPropagation(clauses);
//        System.out.println("The 2D arraylist after unit propagation: " + clauses);

        // Apply Pure Literal Elimination
        PureLiteralElimination.pureLiteralElimination(clauses, literalTruthValues);
//        System.out.println("The 2D arraylist after pure literal elimination: " + clauses);

        // Early exit if the formula is already satisfiable or unsatisfiable
        if (clauses.isEmpty()) {
//            System.out.println("SAT");
            return literalTruthValues;
        }

        // Check if any clauses are empty (Unsatisfiable)
        if (clauses.stream().anyMatch(ArrayList::isEmpty)) {
//            System.out.println("Not SAT");
            return null;  // Return null to indicate unsatisfiable
        }

        // Solve using Brute Force if the formula is not immediately satisfiable or unsatisfiable
        HashMap<Character, Boolean> satAssignment = BruteForce.bruteForceEarlyStopping(clauses);
//        System.out.println("SAT Assignment after brute force: " + satAssignment);

        // Merge results (combine the pure literal elimination with brute force solution)
        literalTruthValues.putAll(satAssignment);
//        System.out.println("Combined Assignments: " + literalTruthValues);

        return literalTruthValues;  // Return the final truth assignments
    }

    public static void main(String[] args) throws IOException {
        String formula = "(-o v -j) ^ (-f v l v l v t) ^ (-u) ^ (k) ^ (-q v n v -e v s) ^ (b v r) ^ (i) ^ (a v m) ^ (g v c) ^ (p) ^ (-d) ^ (w) ^ (h)";
        System.out.println("Formula: " + formula);

        // Parse formula into clauses
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);

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
