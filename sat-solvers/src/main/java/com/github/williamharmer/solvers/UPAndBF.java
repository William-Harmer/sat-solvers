package com.github.williamharmer.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.simplifications.UnitPropagation;

// Pipeline that applies Unit Propagation (UP) to simplify a CNF formula
// and, if necessary, falls back to a brute-force search with early stopping.
// Returns a satisfying assignment if one exists, otherwise null.
public class UPAndBF {

    // Runs Unit Propagation on the clauses, checks for immediate UNSAT,
    // and otherwise invokes a brute-force solver that stops at the first model.
    // Input:
    // - clauses: CNF as list of clauses (each clause is a list of Character literals).
    // Output:
    // - HashMap<Character, Boolean> representing a satisfying assignment if SAT,
    //   or null if the formula is unsatisfiable after simplification.
    public static HashMap<Character, Boolean> uPAndBF(ArrayList<ArrayList<Character>> clauses) {
        // Simplify using unit clauses derived assignments.
        UnitPropagation.unitPropagation(clauses);

        // If any clause becomes empty, the formula is contradictory (UNSAT).
        if (clauses.stream().anyMatch(ArrayList::isEmpty)) {
            return null;
        }

        // Otherwise, attempt to find a model via brute force with early stopping.
        return BruteForce.bruteForceEarlyStopping(clauses);
    }

    // Demonstration main: parses a sample formula, runs UP+BF, and prints the result.
    public static void main(String[] args) throws IOException {
        String formula = "(-o v -j) ^ (-f v l v l v t) ^ (-u) ^ (k) ^ (-q v n v -e v s) ^ (b v r) ^ (i) ^ (a v m) ^ (g v c) ^ (p) ^ (-d) ^ (w) ^ (h)";
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);

        HashMap<Character, Boolean> satAssignment = uPAndBF(clauses);
        if (satAssignment != null) {
            System.out.println("SAT Assignment: " + satAssignment);
        } else {
            System.out.println("Formula is unsatisfiable.");
        }
    }
}