import java.io.*;
import java.util.*;

public class DPLLSolver {
    private static Map<Character, Boolean> literalValues; // Stores the literal values (true/false)
    private static List<List<Character>> clauses; // List of clauses in the formula

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt")); // Open file for reading
        String line;

        while ((line = reader.readLine()) != null) { // Read each line of the file
            line = line.trim(); // Remove any whitespace
            if (line.isEmpty() || line.startsWith("#")) { // Skip empty or comment lines
                continue;
            }

            System.out.println("Formula: " + line);
            long startTime = System.nanoTime(); // Start timer
            solve(line); // Solve the formula
            long endTime = System.nanoTime(); // End timer
            long duration = (endTime - startTime) / 1_000; // Calculate time in microseconds
            System.out.println("Time taken: " + duration + " µs\n");
        }
        reader.close(); // Close the file reader after processing all lines
    }

    // Solve the boolean formula using the DPLL algorithm
    private static void solve(String formula) {
        literalValues = new HashMap<>(); // Reinitialize the map for each formula
        clauses = parseFormula(formula); // Parse the formula into clauses

        if (dpll()) { // Try to solve using DPLL
            System.out.println("Formula is satisfiable with assignment: " + literalValues);
        } else {
            System.out.println("Formula is unsatisfiable.");
        }
    }

    // Parse the formula into a list of clauses
    private static List<List<Character>> parseFormula(String formula) {
        List<List<Character>> clauses = new ArrayList<>();
        String[] rawClauses = formula.split("\\)\\s*\\(\\s*"); // Split the formula into clauses

        for (String rawClause : rawClauses) {
            rawClause = rawClause.replaceAll("[\\(\\)]", "").trim();
            String[] literals = rawClause.split("\\s+");
            List<Character> clause = new ArrayList<>();
            for (String literal : literals) {
                clause.add(literal.charAt(0));
            }
            clauses.add(clause);
        }
        return clauses;
    }

    // DPLL algorithm implementation
    private static boolean dpll() {
        // Perform unit propagation and pure literal elimination
        while (true) {
            boolean unitClauseFound = unitPropagation();
            boolean pureLiteralFound = pureLiteralElimination();
            if (!unitClauseFound && !pureLiteralFound) {
                break; // No unit clauses or pure literals found, exit loop
            }
        }

        // If all clauses are satisfied, return true
        if (isFormulaSatisfied()) {
            return true;
        }

        // If there are no clauses left, return false (unsatisfiable)
        if (hasEmptyClause()) {
            return false;
        }

        // Choose a literal and try both assignments (true and false)
        char unassignedLiteral = chooseLiteral();
        literalValues.put(unassignedLiteral, true);
        if (dpll()) {
            return true; // If true assignment satisfies the formula, return true
        }

        // If false assignment doesn't work, backtrack and try the false assignment
        literalValues.put(unassignedLiteral, false);
        return dpll();
    }

    // Unit propagation step: Assign values to unit clauses
    private static boolean unitPropagation() {
        boolean changed = false;
        for (List<Character> clause : clauses) {
            // If the clause has exactly one unassigned literal, it's a unit clause
            if (clause.size() == 1) {
                char literal = clause.get(0);
                if (!literalValues.containsKey(literal) && !literalValues.containsKey(getNegation(literal))) {
                    literalValues.put(literal, true); // Assign the literal to true
                    changed = true;
                }
            }
        }
        return changed;
    }

    // Pure literal elimination step: Eliminate literals that appear in only one polarity
    private static boolean pureLiteralElimination() {
        Set<Character> pureLiterals = new HashSet<>();
        Map<Character, Integer> literalCounts = new HashMap<>();

        for (List<Character> clause : clauses) {
            for (char literal : clause) {
                literalCounts.put(literal, literalCounts.getOrDefault(literal, 0) + 1);
                literalCounts.put(getNegation(literal), literalCounts.getOrDefault(getNegation(literal), 0) + 1);
            }
        }

        // If a literal appears only in one polarity, eliminate it
        for (Map.Entry<Character, Integer> entry : literalCounts.entrySet()) {
            if (entry.getValue() == 1 && !literalValues.containsKey(entry.getKey())) {
                pureLiterals.add(entry.getKey());
            }
        }

        boolean changed = false;
        for (char literal : pureLiterals) {
            literalValues.put(literal, true); // Assign pure literal to true
            changed = true;
        }
        return changed;
    }

    // Check if the formula is satisfied with the current assignment
    private static boolean isFormulaSatisfied() {
        for (List<Character> clause : clauses) {
            boolean satisfied = false;
            for (char literal : clause) {
                if (literalValues.containsKey(literal) && literalValues.get(literal)) {
                    satisfied = true;
                    break;
                } else if (literalValues.containsKey(getNegation(literal)) && !literalValues.get(getNegation(literal))) {
                    satisfied = true;
                    break;
                }
            }
            if (!satisfied) {
                return false;
            }
        }
        return true;
    }

    // Check if any clause is empty (i.e., unsatisfied)
    private static boolean hasEmptyClause() {
        for (List<Character> clause : clauses) {
            boolean empty = true;
            for (char literal : clause) {
                if (literalValues.containsKey(literal) || literalValues.containsKey(getNegation(literal))) {
                    empty = false;
                    break;
                }
            }
            if (empty) {
                return true;
            }
        }
        return false;
    }

    // Choose an unassigned literal (arbitrary choice)
    private static char chooseLiteral() {
        for (List<Character> clause : clauses) {
            for (char literal : clause) {
                if (!literalValues.containsKey(literal) && !literalValues.containsKey(getNegation(literal))) {
                    return literal;
                }
            }
        }
        throw new IllegalStateException("No unassigned literals left");
    }

    // Get the negation of a literal
    private static char getNegation(char literal) {
        return (literal >= 'a' && literal <= 'u') ? (char) (literal ^ 32) : literal;
    }
}
