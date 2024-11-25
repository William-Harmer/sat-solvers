import java.io.*;
import java.util.*;

public class DPLLSolver {

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt")); // Open file for reading
        String line;

        while ((line = reader.readLine()) != null) { // Read each line of the file
            line = line.trim(); // Remove any whitespace there is from that line (Which is being stored in the string 'line')
            if (line.isEmpty() || line.startsWith("#")) { // If the line is empty or is a comment
                continue; // Break that iteration of the loop and go onto the next line
            }

            System.out.println("Formula: " + line);
            long startTime = System.nanoTime(); // Start timer


            // Parse the input formula string into a list of clauses, where each clause is represented as a set of literals.
            List<Set<String>> clauses = parseFormula(line);

            // Extract all unique literals (both positive and negative) from the clauses and store them in a set
            Set<String> literals = extractLiteralsFromClauses(clauses);

            // Go through dpll steps
            boolean satisfiable = dpll(clauses, literals, new HashMap<>()); // function
            long endTime = System.nanoTime();

            if (satisfiable) {
                System.out.println("Satisfiable.");
            } else {
                System.out.println("Unsatisfiable.");
            }
            System.out.println("Time taken: " + (endTime - startTime) / 1_000 + " µs\n");
        }
        reader.close();
    }

    // Parse the formula into a list of clauses
    private static List<Set<String>> parseFormula(String formula) {
        List<Set<String>> clauses = new ArrayList<>();

        // Split at each and gate
        String[] clauseStrings = formula.split("\\^"); // Assume clauses are AND-separated

        // process each clause and only take the literals
        for (String clauseString : clauseStrings) {
            Set<String> clause = new HashSet<>();
            String[] literals = clauseString.replace("(", "").replace(")", "").trim().split("v");
            for (String literal : literals) {
                clause.add(literal.trim());
            }
            clauses.add(clause);
        }
        return clauses;
        // After processing all the clauses, the method returns the list of sets (clauses), where each set represents a clause in the CNF formula.
        // clauses = [{"A", "B"}, {"C", "D"}, {"E", "F"}];
        // Only the literals
    }

    // Extract literals from clauses

    // have a set of all the unique literals
    // e.g. {"A", "B", "C", "D", "E"}
    private static Set<String> extractLiteralsFromClauses(List<Set<String>> clauses) {
        Set<String> literals = new HashSet<>();
        for (Set<String> clause : clauses) {
            for (String literal : clause) {
                literals.add(literal.replace("-", "")); // Remove negation for literals
            }
        }
        return literals;
    }

    private static boolean dpll(List<Set<String>> clauses, Set<String> literals, Map<String, Boolean> assignment) {

        // function
        // Apply unit propogation
        clauses = unitPropagation(clauses, assignment);

        // If the whole set is empty theformula is satisfied
        if (clauses.isEmpty()) {
            return true;
        }

        // And if that is not true

        for (Set<String> clause : clauses) {
            if (clause.isEmpty()) {
                // If a single clause in the set is empty while the whole clause is not empty it is unsatisfiable
                return false;
            }
        }


        // Next apply pure literal elimination
        // function
        applyPureLiteralElimination(clauses, literals, assignment);

        // Backtracking
        for (String literal : literals) { // for all literals
            if (!assignment.containsKey(literal)) { // if that literal has not been assigned a truth value

                Map<String, Boolean> newAssignment = new HashMap<>(assignment); // temp assignment so if it doesn't work we can go back

                newAssignment.put(literal, true); // Assign that literal true

                // Simplify removes clauses that are satisfied by the literal being set to
                // Now call dpll recursively
                // if dpll returns true (From above), return true to stop the recursion, it is then satisfiable
                if (dpll(simplify(clauses, literal, true), literals, newAssignment)) {
                    return true;
                }

                // Otherwise try again but with the assignment for false and recursively check
                newAssignment = new HashMap<>(assignment);
                newAssignment.put(literal, false);
                if (dpll(simplify(clauses, literal, false), literals, newAssignment)) {
                    return true;
                }

                // If that didn't work as well it is unsatisfiable for that literal, try the next literal
                return false;
            }
        }
        // All literals did not work, the formula is not satisfiable.
        return false;
    }

    // Remove the single literal from the clauses
    private static List<Set<String>> simplify(List<Set<String>> clauses, String literal, boolean value) {
        List<Set<String>> newClauses = new ArrayList<>();

        for (Set<String> clause : clauses) { // iterate over each clause

            if (clause.contains(literal) && value) { // If the literal is present in the clause and its assigned value is true
                continue; // skip to next clause as satisfied
            } else if (clause.contains("-" + literal) && !value) { // If the literal is present in the clause with a - and its assigned value is false
                continue; // skip to next clause as satisfied
            }

            // Otherwise

            Set<String> newClause = new HashSet<>(clause); // Create a new set newClause which is a copy of the current clause
            newClause.remove(value ? "-" + literal : literal); // If the literal is present in the newClause set, it is removed. If the literal is not present, then nothing happens
            // If the assigned value is true, then we remove the negation of the literal (i.e., "-" + literal). This is because, with value = true, the negated literal cannot be true, so it is no longer needed in the clause.
            // If the assigned value is false, then we remove the literal itself (i.e., literal). This is because, with value = false, the literal itself cannot be true, so it is no longer needed in the clause.
            newClauses.add(newClause); // Add to the new clauses set
        }
        return newClauses;
    }

    // Perform unit propagation
    private static List<Set<String>> unitPropagation(List<Set<String>> clauses, Map<String, Boolean> assignment) {
        boolean changed;
        do {
            changed = false;
            Iterator<Set<String>> it = clauses.iterator();

            while (it.hasNext()) { // Go over each clause
                Set<String> clause = it.next();
                if (clause.size() == 1) { // If there is only one literal (A unit clause)
                    String literal = clause.iterator().next(); // Get the single literal
                    boolean value = !literal.startsWith("-"); // If the literal does not start with a "-", it is considered true; otherwise, it is considered false.
                    String variable = literal.replace("-", ""); // Remove negation now to get the variable name
                    assignment.put(variable, value); // add to the assignment map

                    it.remove(); // remove that clause

                    // then go through the remaining clauses and look for any clauses that are now satisfied or need simplification due to the new assignment.
                    clauses = simplify(clauses, variable, value); // function
                    // Now the clauses have been fixed with this new single literal being assigned a value

                    changed = true;
                }
            }
        } while (changed);

        return clauses;
    }


    private static void applyPureLiteralElimination(List<Set<String>> clauses, Set<String> literals, Map<String, Boolean> assignment) {
        Map<String, Integer> literalCounts = new HashMap<>();

        for (Set<String> clause : clauses) { // For each clause
            for (String literal : clause) { // iterates through the literals in that clause.
                literalCounts.put(literal, literalCounts.getOrDefault(literal, 0) + 1); // For each literal, it increments the count of occurrences in the literalCounts map.
                // So it counts the amount of literals across multiple clauses
            }
        }

        for (String literal : literalCounts.keySet()) { // iterates through each literal
            String variable = literal.replace("-", ""); // remove the negation symbol
            boolean isPositive = !literal.startsWith("-"); //  a flag that tells whether the literal is positive, it is positive if it does not start with a negative
            if (!assignment.containsKey(variable)) { // if the assignment map does not contain an entry for the given variable.
                boolean isPure = literalCounts.containsKey(variable) && !literalCounts.containsKey("-" + variable); // check if pure
                if (isPure) { // If the literal is pure
                    assignment.put(variable, isPositive); // assign the appropriate positive or negative assignment
                    clauses = simplify(clauses, variable, isPositive); // Simplify the remaining
                }
            }
        }
    }
}
