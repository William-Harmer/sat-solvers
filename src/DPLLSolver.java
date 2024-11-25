import java.io.*;
import java.util.*;

public class DPLLSolver {

    // Method to parse the CNF formula from a file
    public static List<String> parseCNFFile(String filename) throws IOException {
        List<String> formulas = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(filename));
        String line;
        while ((line = reader.readLine()) != null) {
            line = line.trim(); // Trim whitespace
            // Ignore comment lines and empty lines
            if (!line.isEmpty() && !line.startsWith("#")) {
                formulas.add(line);
            }
        }
        reader.close();
        return formulas;
    }

    // Method to perform DPLL on a CNF formula
    public static boolean dpll(List<Set<String>> clauses, Map<String, Boolean> assignment) {
        // Unit Propagation
        unitPropagation(clauses, assignment);

        // Check if the formula is satisfied
        if (clauses.isEmpty()) return true;

        // Check for an empty clause (unsatisfiable)
        for (Set<String> clause : clauses) {
            if (clause.isEmpty()) return false;
        }

        // Pure literal elimination
        pureLiteralElimination(clauses, assignment);

        // Select the next variable to assign a value (non-deterministic choice)
        String variable = selectVariable(clauses);

        // Try assigning true to the variable and recurse
        clauses = new ArrayList<>(clauses);
        assignment.put(variable, true);
        if (dpll(clauses, assignment)) return true;

        // Backtrack and try assigning false to the variable
        assignment.put(variable, false);
        return dpll(clauses, assignment);
    }

    // Perform unit propagation
    private static void unitPropagation(List<Set<String>> clauses, Map<String, Boolean> assignment) {
        boolean changed = true;
        while (changed) {
            changed = false;
            Iterator<Set<String>> iterator = clauses.iterator();
            while (iterator.hasNext()) {
                Set<String> clause = iterator.next();
                if (clause.size() == 1) {
                    String unit = clause.iterator().next();
                    boolean value = !unit.startsWith("-");
                    String var = unit.replace("-", "");
                    assignment.put(var, value);
                    iterator.remove();
                    changed = true;
                }
            }
        }
    }

    // Perform pure literal elimination
    private static void pureLiteralElimination(List<Set<String>> clauses, Map<String, Boolean> assignment) {
        Set<String> allLiterals = new HashSet<>();
        Set<String> pureLiterals = new HashSet<>();

        // Collect all literals in the formula
        for (Set<String> clause : clauses) {
            for (String literal : clause) {
                allLiterals.add(literal);
            }
        }

        // Find pure literals (appear with only one polarity)
        for (String literal : allLiterals) {
            String negLiteral = "-" + literal;
            if (!allLiterals.contains(negLiteral)) {
                pureLiterals.add(literal);
            }
        }

        // Assign truth values to pure literals
        for (String literal : pureLiterals) {
            boolean value = !literal.startsWith("-");
            String var = literal.replace("-", "");
            assignment.put(var, value);
            removeLiteralFromClauses(clauses, literal);
        }
    }

    // Remove literal from clauses
    private static void removeLiteralFromClauses(List<Set<String>> clauses, String literal) {
        for (Iterator<Set<String>> iterator = clauses.iterator(); iterator.hasNext();) {
            Set<String> clause = iterator.next();
            clause.remove(literal);
            if (clause.isEmpty()) {
                iterator.remove(); // Remove empty clause
            }
        }
    }

    // Select an unassigned variable (heuristic choice)
    private static String selectVariable(List<Set<String>> clauses) {
        for (Set<String> clause : clauses) {
            for (String literal : clause) {
                String var = literal.replace("-", "");
                return var; // Return first unassigned variable
            }
        }
        return null; // No more variables to select
    }

    // Convert the CNF formula string to a list of clauses (sets of literals)
    private static List<Set<String>> convertToClauses(String formula) {
        List<Set<String>> clauses = new ArrayList<>();
        String[] clauseStrings = formula.split("\\^");
        for (String clauseString : clauseStrings) {
            clauseString = clauseString.trim().replace("(", "").replace(")", "");
            String[] literals = clauseString.split("v");
            Set<String> clause = new HashSet<>();
            for (String literal : literals) {
                clause.add(literal.trim());
            }
            clauses.add(clause);
        }
        return clauses;
    }

    public static void main(String[] args) {
        try {
            // Read CNF formulas from the file
            List<String> formulas = parseCNFFile("input.txt");

            // Process each CNF formula
            for (String formula : formulas) {
                System.out.println("Processing: " + formula);
                List<Set<String>> clauses = convertToClauses(formula);
                Map<String, Boolean> assignment = new HashMap<>();

                // Run DPLL on the formula
                boolean isSatisfiable = dpll(clauses, assignment);

                // Output the result
                if (isSatisfiable) {
                    System.out.println("Satisfiable with assignment: " + assignment);
                } else {
                    System.out.println("Unsatisfiable");
                }
                System.out.println();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
