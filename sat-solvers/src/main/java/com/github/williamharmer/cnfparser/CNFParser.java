package com.github.williamharmer.cnfparser;

import java.util.ArrayList;

// Parses a simple CNF string format into a 2D ArrayList representation.
// Representation:
// - The result is a list of clauses; each clause is a list of Character literals.
// - Lowercase character => positive literal (e.g., 'a').
// - A preceding '-' indicates negation; the literal is stored uppercase (e.g., "-a" -> 'A').
// Input format expectations:
// - Clauses are enclosed in parentheses: (a b c) or (a v b v C).
// - Inside a clause, 'v' acts as a disjunction separator and is ignored.
// - Whitespace is ignored entirely.
// - Clauses can be concatenated: (abC)(-d v e)(f).
public class CNFParser {
    public static ArrayList<ArrayList<Character>> formulaTo2DArrayList(String formula) {
        // Remove all whitespace for simpler character-by-character parsing.
        formula = formula.replaceAll("\\s", "");

        // Output structure: list of clauses, each clause is a list of literals.
        ArrayList<ArrayList<Character>> clauses = new ArrayList<>();

        // Holds literals for the clause currently being parsed; null when not inside parentheses.
        ArrayList<Character> newRow = null;

        // Walk the string and build clauses.
        for (int i = 0; i < formula.length(); i++) {
            char currentChar = formula.charAt(i);

            if (currentChar == '(') {
                // Begin a new clause.
                newRow = new ArrayList<>();
            } else if (currentChar == ')') {
                // End current clause: if we were building one, add it to the output.
                if (newRow != null) {
                    clauses.add(newRow);
                    newRow = null; // Reset for next clause.
                }
            } else if (newRow != null) {
                if (currentChar == '-') {
                    // Negation marker: consume the next character as the negated literal.
                    if (i + 1 < formula.length()) {
                        char nextChar = formula.charAt(i + 1);
                        if (nextChar != 'v') {
                            // Store negated literal as uppercase to encode polarity.
                            newRow.add(Character.toUpperCase(nextChar));
                            i++; // Skip the next character; we've processed it.
                        }
                    }
                } else if (currentChar != 'v') {
                    // Regular literal (positive) or any non-separator character: add to clause.
                    newRow.add(currentChar);
                }
                // If currentChar == 'v', it's a separator; skip it.
            }
        }

        // Return parsed CNF as list of clauses.
        return clauses;
    }   
}