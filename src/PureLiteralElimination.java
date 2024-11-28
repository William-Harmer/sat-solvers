import java.util.*;

public class PureLiteralElimination {
    private static HashMap<Character, Boolean> pureLiteralElimination(ArrayList<ArrayList<Character>> clauses){
        // Create a map to store all the literals
        HashMap<Character, Boolean> pureLiterals = new HashMap<>();
        HashSet<Character> uniqueLiterals = new HashSet<>();

        // Iterate through each clause
        for (ArrayList<Character> clause : clauses) {
            uniqueLiterals.addAll(clause);
        }

        for (Character literal : uniqueLiterals) {
            if (uniqueLiterals.contains(Utility.negate(literal))) { // Not pure
            } else{ // Pure
                if (Character.isUpperCase(literal)) {
                    pureLiterals.put(literal, false);
                } else {
                    pureLiterals.put(literal, true);
                }
            }
        }
        uniqueLiterals.clear();
        System.out.println("Pure literals: " + pureLiterals);


        for (int i = 0; i < clauses.size(); i++) { // For each clause
            for (Character literal : clauses.get(i)) { // For each literal in the clause
                if (pureLiterals.containsKey(literal)) { // If the literal is pure
                    // Remove the whole clause
                    clauses.remove(i);
                    i--;
                    break;
                }
            }
        }
        System.out.println("New formula: " + clauses);

        // Now return the hashmap
        return pureLiterals;
    }

    public static void main(String[] args) {
        String formula = "(b)^(bvc)^(Bvc)^(B)";
        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);
        System.out.println("Formula: " + clauses);
        HashMap<Character, Boolean> pureLiteralsTruthValues  = pureLiteralElimination(clauses);
    }
}
