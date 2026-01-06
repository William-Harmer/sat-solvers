package com.github.williamharmer.simplifications;

import java.util.ArrayList;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.utilities.OppositePolarity;

// Implements Unit Propagation for CNF formulas represented as a list of clauses,
// where each clause is a list of Character literals. A unit clause forces its
// literal to be true, which allows removing satisfied clauses and removing the
// opposite literal from other clauses. Repeats until no changes occur.
public class UnitPropagation {

    // In-place unit propagation:
    // - For each unit clause (single literal L):
    //   * Remove any clause containing L (it is satisfied).
    //   * From any clause containing ¬L, remove ¬L.
    // - Iterate until a full pass makes no modifications.
    public static void unitPropagation(ArrayList<ArrayList<Character>> clauses) {
        boolean formulaModified; // Tracks whether any change was made in the pass

        do {
            formulaModified = false;

            // Iterate over clauses to find unit clauses
            for (int j = 0; j < clauses.size(); j++) {
                ArrayList<Character> clause = clauses.get(j);

                if (clause.size() == 1) { // Found a unit clause
                    char unitClauseElement = clause.getFirst();

                    // Apply the unit literal to all other clauses
                    for (int i = 0; i < clauses.size(); i++) {
                        if (i == j) continue; // Skip the unit clause itself

                        if (clauses.get(i).contains(unitClauseElement)) {
                            // Clause satisfied by the unit literal: remove it
                            clauses.remove(i);
                            i--; // Adjust index after removal
                            j--; // Adjust j because the list shrank before it
                            formulaModified = true;

                        } else if (clauses.get(i).contains(OppositePolarity.oppositePolarity(unitClauseElement))) {
                            // Clause contains the opposite literal: remove that literal
                            clauses.get(i).remove((Character) OppositePolarity.oppositePolarity(unitClauseElement));
                            formulaModified = true;
                        }
                    }

                    // If we modified the formula, restart scanning for unit clauses
                    if (formulaModified) {
                        break;
                    }
                }
            }
        } while (formulaModified); // Continue until a pass makes no changes
    }

    // Simple driver to demonstrate unit propagation on a small formula.
    public static void main(String[] args) {

        String formula = "(a c) (a b)";
        // Example format supports multiple clauses like:
        // (A B) (A c) (C D) (b d e) (E f G) (b g h) (H I) (H j) (i J k) (J L) (K l)
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);

        // Before propagation
        System.out.println(clauses);

        // Apply unit propagation
        unitPropagation(clauses);

        // After propagation
        System.out.println(clauses);
    }

}