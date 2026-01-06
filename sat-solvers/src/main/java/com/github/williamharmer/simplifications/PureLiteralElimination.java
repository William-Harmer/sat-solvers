package com.github.williamharmer.simplifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.utilities.OppositePolarity;

// Implements Pure Literal Elimination (PLE) for CNF formulas.
// A literal is pure if its opposite polarity does not appear anywhere in the formula.
// Pure literals can be assigned in a way that satisfies all clauses containing them,
// so those clauses can be removed from the formula. This method records the
// assignments for pure literals and removes any clause containing them.
public class PureLiteralElimination {

    // In-place PLE:
    // - Collect all literals that appear in the formula.
    // - Identify pure literals (those whose opposite polarity never appears).
    // - Record their satisfying assignments into 'pureLiterals'.
    // - Remove any clause that contains any pure literal.
    // Parameters:
    // - clauses: CNF as list of clauses (each clause is a list of Character literals).
    // - pureLiterals: output map of variable -> boolean assignment for detected pure literals.
    public static void pureLiteralElimination(ArrayList<ArrayList<Character>> clauses, HashMap<Character, Boolean> pureLiterals){
        // Track all literals present across all clauses.
        HashSet<Character> uniqueLiterals = new HashSet<>();

        // Gather every literal found in the formula.
        for (ArrayList<Character> clause : clauses) {
            uniqueLiterals.addAll(clause);
        }

        // Determine which literals are pure and record their truth values.
        for (Character literal : uniqueLiterals) {
            if (uniqueLiterals.contains(OppositePolarity.oppositePolarity(literal))) {
                // Literal appears with both polarities => not pure; skip.
            } else {
                // Literal is pure: assign it to satisfy all its occurrences.
                if (Character.isUpperCase(literal)) {
                    // Uppercase denotes negation in this representation => variable is false.
                    pureLiterals.put(Character.toLowerCase(literal), false);
                } else {
                    // Lowercase denotes positive literal => variable is true.
                    pureLiterals.put(literal, true);
                }
            }
        }
        uniqueLiterals.clear();

        // Remove any clause that contains a pure literal (since it is satisfied).
        for (int i = 0; i < clauses.size(); i++) {
            for (Character literal : clauses.get(i)) {
                if (pureLiterals.containsKey(Character.toLowerCase(literal))) {
                    // Clause satisfied by a pure literal; remove it.
                    clauses.remove(i);
                    i--;
                    break;
                }
            }
        }
    }

    // Simple demo to show PLE on a tiny formula.
    public static void main(String[] args) {
        String formula = "(B)^(a)^(a)";
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);
        System.out.println("Formula: " + clauses);
        HashMap<Character, Boolean> pureLiteralsTruthValues = new HashMap<>();
        pureLiteralElimination(clauses, pureLiteralsTruthValues);
        System.out.println("New formula: " + clauses);
        System.out.println(pureLiteralsTruthValues);
    }
}