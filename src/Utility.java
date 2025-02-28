import java.util.ArrayList;

public class Utility {

    public static ArrayList<ArrayList<Character>> addFirstElementNotAUnitClauseAsNewClauseToFormula(
            ArrayList<ArrayList<Character>> clauses, boolean useCaps) {

        if (clauses.isEmpty()) {
            return clauses; // No clauses exist, return as is.
        }

        for (ArrayList<Character> clause : clauses) {
            if (clause.size() > 1) { // Ensure it's not a unit clause
                char firstLiteral = clause.get(0); // Get the first literal

                ArrayList<Character> newClause = new ArrayList<>();
                newClause.add(useCaps ? Character.toUpperCase(firstLiteral) : Character.toLowerCase(firstLiteral));

                clauses.add(newClause); // Add it to the formula

                return clauses;
            }
        }

        return clauses; // If all clauses are unit clauses, return unchanged
    }


    public static char oppositePolarity(char literal) {
        if (Character.isUpperCase(literal)) {
            return Character.toLowerCase(literal);
        } else {
            return Character.toUpperCase(literal);
        }
    }

    public static ArrayList<ArrayList<Character>> clauseCopy(ArrayList<ArrayList<Character>> original) {
        ArrayList<ArrayList<Character>> copy = new ArrayList<>();
        for (ArrayList<Character> innerList : original) {
            copy.add(new ArrayList<>(innerList)); // Create a new list and copy elements
        }
        return copy;
    }

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
