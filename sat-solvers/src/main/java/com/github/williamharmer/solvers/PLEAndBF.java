package com.github.williamharmer.solvers;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.simplifications.PureLiteralElimination;

// Applies Pure Literal Elimination (PLE) to simplify a CNF, then uses a
// brute-force search with early stopping to find a satisfying assignment.
// Returns a combined assignment map if SAT, or null if UNSAT.
public class PLEAndBF {
    public static HashMap<Character, Boolean> pLEAAndBF(ArrayList<ArrayList<Character>> clauses) {
        // Accumulates assignments implied by PLE and any additional ones from brute force.
        HashMap<Character, Boolean> literalTruthValues = new HashMap<>();

        // Perform Pure Literal Elimination; this mutates 'clauses' to simplify it
        // and records fixed assignments in 'literalTruthValues'.
        PureLiteralElimination.pureLiteralElimination(clauses, literalTruthValues);

        // If all clauses are satisfied after PLE, return the assignments collected so far.
        if (clauses.isEmpty()) {
            return literalTruthValues;
        }

        // If any clause becomes empty, the formula is contradictory (UNSAT).
        if (clauses.stream().anyMatch(ArrayList::isEmpty)) {
            return null;
        }

        // Otherwise, attempt to find a model via brute force with early stopping.
        HashMap<Character, Boolean> satAssignment = BruteForce.bruteForceEarlyStopping(clauses);

        // If brute force finds no model, UNSAT.
        if (satAssignment == null) {
            return null;
        } else {
            // Merge brute-force assignments with those found by PLE and return.
            literalTruthValues.putAll(satAssignment);
            return literalTruthValues;
        }
    }

    // Demo main: parse a hard-coded CNF, run PLE + BF, and print the outcome.
    public static void main(String[] args) {
        String formula = "(-o v -j) ^ (-f v l v l v t) ^ (-u) ^ (k) ^ (-q v n v -e v s) ^ (b v r) ^ (i) ^ (a v m) ^ (g v c) ^ (p) ^ (-d) ^ (w) ^ (h)";
        System.out.println(formula);
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);
        System.out.println(clauses);

        HashMap<Character, Boolean> satAssignment = pLEAAndBF(clauses);
        if (satAssignment != null) {
            System.out.println("SAT Assignment: " + satAssignment);
        } else {
            System.out.println("Formula is unsatisfiable.");
        }
    }
}