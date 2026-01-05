package com.github.williamharmer.solvers;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.simplifications.PureLiteralElimination;

public class PLEAndBF {
    public static HashMap<Character, Boolean> pLEAAndBF(ArrayList<ArrayList<Character>> clauses) {
//        System.out.println("Clauses in PLEAANDBF" + clauses);

        HashMap<Character, Boolean> literalTruthValues = new HashMap<>();

        // PLE the formula

        PureLiteralElimination.pureLiteralElimination(clauses, literalTruthValues);
        // So after it is doing this, it is permanently changing clauses, how do i stop this?

//        System.out.println("Clauses in middle" + clauses);


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
//        System.out.println("Brute force result: " + satAssignment);
//
//        System.out.println("Clauses at end" + clauses);

        if (satAssignment == null) {
            return null;
        } else {
            literalTruthValues.putAll(satAssignment);
            return literalTruthValues;  // Return the final truth assignments
        }
    }

    public static void main(String[] args) {
        String formula = "(-o v -j) ^ (-f v l v l v t) ^ (-u) ^ (k) ^ (-q v n v -e v s) ^ (b v r) ^ (i) ^ (a v m) ^ (g v c) ^ (p) ^ (-d) ^ (w) ^ (h)";
        System.out.println(formula);
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);
        System.out.println(clauses);

        HashMap<Character, Boolean> satAssignment = pLEAAndBF(clauses); // Changed to HashMap
        if (satAssignment != null) {
            System.out.println("SAT Assignment: " + satAssignment);
        } else {
            System.out.println("Formula is unsatisfiable.");
        }
    }
}
