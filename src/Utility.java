import java.util.ArrayList;
import java.util.HashMap;

public class Utility {

    public static ArrayList<ArrayList<Character>> addFirstElementAsNewClauseToFormula(
            ArrayList<ArrayList<Character>> clauses,
            boolean useCaps,
            HashMap<Character, Boolean> literalMap) {

        ArrayList<Character> newClause = new ArrayList<>();
        clauses.add(newClause);  // Add an empty clause to the formula

        // Get the first element from the first clause
        char firstLiteral = clauses.get(0).get(0);

        if (useCaps) {
            // Add the capitalized (uppercase) version of the literal
            clauses.get(clauses.size() - 1).add(Character.toUpperCase(firstLiteral));
        } else {
            // Add the non-capitalized (lowercase) version of the literal
            clauses.get(clauses.size() - 1).add(Character.toLowerCase(firstLiteral));
        }

        // If a HashMap is passed, add the firstLiteral to the map with a value of true
        if (literalMap != null) {
            literalMap.put(firstLiteral, true);
        }

        return clauses;  // Return the modified formula
    }

    public static char negate(char literal) {
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
}
