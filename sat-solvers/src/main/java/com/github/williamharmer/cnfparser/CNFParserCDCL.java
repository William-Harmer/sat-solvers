package com.github.williamharmer.cnfparser;

import java.util.ArrayList;

import com.github.williamharmer.solvers.cdcl.CDCLClause;

// Parser for a simple CNF string format into a list of CDCLClause objects.
// Expected input format examples:
// - Literals inside parentheses form a clause: (abc), (aB), (Aef)
// - 'v' is used as the disjunction separator inside a clause: (a v b v C)
// - A leading '-' before a literal indicates negation; the literal is stored uppercase.
//   e.g., "(-a v b)" becomes clause ['A','b']
// - Clauses are concatenated: (abc)(De)(-f v g)
// - Whitespace is ignored.
public class CNFParserCDCL {

    public static ArrayList<CDCLClause> formulaToCDCLArrayList(String formula) {
        // Remove all whitespace to simplify parsing.
        formula = formula.replaceAll("\\s", "");

        // Output list of CDCLClause objects for the formula.
        ArrayList<CDCLClause> clausesData = new ArrayList<>();

        // Holds literals for the clause currently being parsed.
        ArrayList<Character> newRow = null;

        // Scan the input one character at a time.
        for (int i = 0; i < formula.length(); i++) {
            char currentChar = formula.charAt(i);

            if (currentChar == '(') {
                // Begin a new clause.
                newRow = new ArrayList<>();
            } else if (currentChar == ')') {
                // End of the current clause: wrap into a CDCLClause and add to output.
                if (newRow != null) {
                    ArrayList<Character> trailElements = new ArrayList<>(); // initially empty trail
                    int level = 0; // initial decision level for parsed clauses

                    CDCLClause cdclClause = new CDCLClause(newRow, trailElements, level);
                    clausesData.add(cdclClause);

                    // Reset for the next clause.
                    newRow = null;
                }
            } else if (newRow != null) {
                if (currentChar == '-') {
                    // Negation marker: look ahead for the literal to negate.
                    if (i + 1 < formula.length()) {
                        char nextChar = formula.charAt(i + 1);
                        if (nextChar != 'v') {
                            // Store negated literal as uppercase to encode polarity.
                            newRow.add(Character.toUpperCase(nextChar));
                            i++; // Consume the next character since we handled it.
                        }
                    }
                } else if (currentChar != 'v') {
                    // Regular literal: store as-is (lowercase means positive).
                    newRow.add(currentChar);
                }
                // If currentChar == 'v', skip it (literal separator).
            }
        }

        // Return the list of parsed CDCLClause objects.
        return clausesData;
    }
    
}