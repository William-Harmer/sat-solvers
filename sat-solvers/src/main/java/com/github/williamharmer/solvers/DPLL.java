package com.github.williamharmer.solvers;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.simplifications.PureLiteralElimination;
import com.github.williamharmer.simplifications.UnitPropagation;

// Davis–Putnam–Logemann–Loveland (DPLL) solver.
// This implementation applies Unit Propagation and Pure Literal Elimination,
// then branches by asserting a literal (as a unit clause) derived from the
// first non-unit clause, exploring both polarities. It returns true if a
// satisfying assignment is found, and false otherwise. The public dPLL()
// wrapper returns the assignment map on success or an empty map on failure.
public class DPLL {

    // Core recursive procedure. Mutates 'clauses' and updates 'literalTruthValues'
    // through simplification steps. Branching is performed by adding a new unit
    // clause with a chosen literal to the formula and recursing.
    private static boolean executeDPLL(ArrayList<ArrayList<Character>> clauses, HashMap<Character, Boolean> literalTruthValues){
        // Apply Unit Propagation to simplify with forced assignments.
        UnitPropagation.unitPropagation(clauses);

        // Apply Pure Literal Elimination to fix literals that appear with a single polarity.
        PureLiteralElimination.pureLiteralElimination(clauses, literalTruthValues);

        // Base cases:
        // - If all clauses are satisfied, we have a model.
        if(clauses.isEmpty()){
            return true;
        // - If any clause is empty, a contradiction was derived (unsatisfiable along this branch).
        } else if (clauses.stream().anyMatch(ArrayList::isEmpty)) {
            return false;
        }

        // Branching step:
        // Create a new unit clause from the first literal of the first non-unit clause
        // and recurse on both polarities (lowercase/uppercase here encode negated/positive).
        return executeDPLL(addFirstElementNotAUnitClauseAsNewClauseToFormula(clauses,false), literalTruthValues)
                || executeDPLL(addFirstElementNotAUnitClauseAsNewClauseToFormula(clauses, true), literalTruthValues);
    }

    // Selects the first clause with size > 1, takes its first literal, and creates
    // a new unit clause asserting that literal with a chosen polarity. Appends the
    // unit clause directly to 'clauses' and returns the same reference (in-place mutation).
    // useCaps = true selects the positive (uppercase) version; false selects lowercase.
    private static ArrayList<ArrayList<Character>> addFirstElementNotAUnitClauseAsNewClauseToFormula(
            ArrayList<ArrayList<Character>> clauses, boolean useCaps) {

        for (ArrayList<Character> clause : clauses) {
            if (clause.size() > 1) { // Pick a non-unit clause to branch on
                char firstLiteral = clause.get(0); // Choose the first literal

                ArrayList<Character> newClause = new ArrayList<>();
                newClause.add(useCaps ? Character.toUpperCase(firstLiteral) : Character.toLowerCase(firstLiteral));

                clauses.add(newClause); // Assert the chosen literal as a new unit clause

                return clauses;
            }
        }

        // If no non-unit clause exists, return unchanged.
        return clauses;
    }

    // Public API: runs the recursive DPLL on the given clauses and returns the
    // assignment map if SAT, or an empty map if UNSAT. The input 'clauses'
    // structure is mutated by the algorithm.
    public static HashMap<Character, Boolean> dPLL(ArrayList<ArrayList<Character>> clauses) {
        HashMap<Character, Boolean> literalTruthValues = new HashMap<>();
        boolean result = executeDPLL(clauses, literalTruthValues);

        if (result) {
            // On success, accumulated assignments are returned as is.
        } else {
            // On failure, return an empty map to indicate UNSAT.
            literalTruthValues = new HashMap<>();
        }

        return literalTruthValues;
    }

    // Simple driver to parse a sample CNF, invoke DPLL, and print the result.
    public static void main(String[] args) {
        String formula = "(-lvc)^(-pvd)^(lvpv-cv-d)";
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);
        System.out.println("Initial formula clauses: " + clauses);

        // Invoke DPLL and display the final assignment (empty map if UNSAT).
        HashMap<Character, Boolean> literalTruthValues = dPLL(clauses);
        System.out.println("Final literal truth values: " + literalTruthValues);
    }
}