package com.github.williamharmer.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.simplifications.PureLiteralElimination;
import com.github.williamharmer.simplifications.UnitPropagation;

// Combines two common SAT simplifications—Unit Propagation (UP) and
// Pure Literal Elimination (PLE)—followed by a fallback Brute Force
// search with early stopping. Returns a satisfying assignment if found,
// or null if the formula is determined unsatisfiable.
public class UPAndPLEAndBF {

    // Runs UP, then PLE, then BruteForceEarlyStopping if needed.
    // Input:
    // - clauses: CNF as a list of clauses, each clause is a list of Character literals.
    // Output:
    // - HashMap<Character, Boolean> with a satisfying assignment if SAT,
    //   or null if UNSAT is detected.
    // Notes:
    // - This method mutates the input clauses via simplification steps.
    // - The returned map may include assignments derived from PLE plus
    //   any additional assignments required by the brute-force step.
    public static HashMap<Character, Boolean> uPAndPLEAndBF(ArrayList<ArrayList<Character>> clauses) {
        // Accumulates assignments asserted by PLE and the final solver.
        HashMap<Character, Boolean> literalTruthValues = new HashMap<>();

        // Apply Unit Propagation to simplify using unit clauses.
        UnitPropagation.unitPropagation(clauses);

        // Apply Pure Literal Elimination and record those assignments.
        PureLiteralElimination.pureLiteralElimination(clauses, literalTruthValues);

        // If all clauses were satisfied during simplification, return what we have.
        if (clauses.isEmpty()) {
            return literalTruthValues;
        }

        // If any clause is empty after simplification, the formula is contradictory (UNSAT).
        if (clauses.stream().anyMatch(ArrayList::isEmpty)) {
            return null;
        }

        // Otherwise, defer to a bounded brute force search that stops at first solution.
        HashMap<Character, Boolean> satAssignment = BruteForce.bruteForceEarlyStopping(clauses);

        // Merge any brute-force-found assignment into the accumulated ones from PLE.
        literalTruthValues.putAll(satAssignment);

        // Return the combined satisfying assignment.
        return literalTruthValues;
    }

    // Simple demo entry point to run the combined approach on a hard-coded formula.
    public static void main(String[] args) throws IOException {
        String formula = "(-o v -j) ^ (-f v l v l v t) ^ (-u) ^ (k) ^ (-q v n v -e v s) ^ (b v r) ^ (i) ^ (a v m) ^ (g v c) ^ (p) ^ (-d) ^ (w) ^ (h)";
        System.out.println("Formula: " + formula);

        // Parse the textual CNF into the 2D list representation expected by the solvers.
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);

        // Run UP + PLE + BF pipeline.
        HashMap<Character, Boolean> literalTruthValues = uPAndPLEAndBF(clauses);

        // Report result.
        if (literalTruthValues != null) {
            System.out.println("Final Combined Assignments: " + literalTruthValues);
        } else {
            System.out.println("Formula is not satisfiable.");
        }
    }
}