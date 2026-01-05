package com.github.williamharmer.cnfparser;

import java.util.ArrayList;

public class CNFParser {
    public static ArrayList<ArrayList<Character>> formulaTo2DArrayList(String formula) {
        // Strip all whitespace from the formula
        formula = formula.replaceAll("\\s", "");

        // Create the 2D ArrayList to store clauses
        ArrayList<ArrayList<Character>> clauses = new ArrayList<>();

        // Temporary list for the current clause (row)
        ArrayList<Character> newRow = null;

        // Iterate through the formula characters
        for (int i = 0; i < formula.length(); i++) {
            char currentChar = formula.charAt(i);

            if (currentChar == '(') {
                // Start a new clause, create a new row
                newRow = new ArrayList<>();
            } else if (currentChar == ')') {
                // End the current clause, add the row to the 2D ArrayList
                if (newRow != null) {
                    clauses.add(newRow);
                    newRow = null; // Reset for the next clause
                }
            } else if (newRow != null) {
                if (currentChar == '-') {
                    // If a '-' is found, check the next character
                    if (i + 1 < formula.length()) {
                        char nextChar = formula.charAt(i + 1);
                        if (nextChar != 'v') {
                            newRow.add(Character.toUpperCase(nextChar)); // Add the next character as uppercase
                            i++; // Skip the next character since it's already processed
                        }
                    }
                } else if (currentChar != 'v') {
                    // Add any other character (except 'v') to the current clause
                    newRow.add(currentChar);
                }
            }
        }

        return clauses; // Return the 2D ArrayList
    }   
}
