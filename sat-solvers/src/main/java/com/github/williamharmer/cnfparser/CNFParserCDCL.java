package com.github.williamharmer.cnfparser;

import java.util.ArrayList;

import com.github.williamharmer.solvers.cdcl.CDCLClause;

public class CNFParserCDCL {

        public static ArrayList<CDCLClause> formulaToCDCLArrayList(String formula) {
        // Strip all whitespace from the formula
        formula = formula.replaceAll("\\s", "");

        // Create the ArrayList to store CDCLClause objects
        ArrayList<CDCLClause> clausesData = new ArrayList<>();

        // Temporary list for the current clause (row)
        ArrayList<Character> newRow = null;

        // Iterate through the formula characters
        for (int i = 0; i < formula.length(); i++) {
            char currentChar = formula.charAt(i);

            if (currentChar == '(') {
                // Start a new clause, create a new row
                newRow = new ArrayList<>();
            } else if (currentChar == ')') {
                // End the current clause, add the CDCLClause to the list
                if (newRow != null) {
                    // Create empty trailElements and set level to 0
                    ArrayList<Character> trailElements = new ArrayList<>();
                    int level = 0;

                    // Create a CDCLClause object and add it to the clausesData list
                    CDCLClause cdclClause = new CDCLClause(newRow, trailElements, level);
                    clausesData.add(cdclClause);

                    // Reset for the next clause
                    newRow = null;
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

        return clausesData; // Return the ArrayList of CDCLClause objects
    }
    
}
