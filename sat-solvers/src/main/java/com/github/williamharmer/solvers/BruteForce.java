package com.github.williamharmer.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.github.williamharmer.cnfparser.CNFParser;

// Brute-force SAT utilities:
// - bruteForce: enumerate all assignments and collect every satisfying model.
// - bruteForceEarlyStopping: stop at the first satisfying model and return it.
// Representation notes:
// - Variables are Characters; lowercase denotes the base variable key.
// - An uppercase literal in a clause represents negation of that variable.
public class BruteForce {
    // Public API: return the set of all satisfying assignments for the CNF.
    public static HashSet<HashMap<Character, Boolean>> bruteForce(ArrayList<ArrayList<Character>> clauses) {
        return bruteForceAll(clauses);
    }

    // Public API: return the first satisfying assignment found, or null if none exists.
    public static HashMap<Character, Boolean> bruteForceEarlyStopping(ArrayList<ArrayList<Character>> clauses) {
        return bruteForceFirst(clauses);
    }

    // Enumerate all possible assignments over the variables present and
    // collect every assignment that satisfies the CNF.
    private static HashSet<HashMap<Character, Boolean>> bruteForceAll(ArrayList<ArrayList<Character>> clauses) {
        HashMap<Character, Boolean> literalTruthAssignments = new HashMap<>();

        // Initialize the variable map (keys are lowercase variables) to false.
        for (ArrayList<Character> clause : clauses) {
            for (Character literal : clause) {
                literalTruthAssignments.put(Character.toLowerCase(literal), false);
            }
        }

        // There are 2^(number of variables) possible assignments.
        int totalCombinations = (int) Math.pow(2, literalTruthAssignments.size());
        HashSet<HashMap<Character, Boolean>> satTruthAssignments = new HashSet<>();

        // Iterate each bit pattern and map bits to variable truth values.
        int literalIndex = 0;
        for (int i = 0; i < totalCombinations; i++) {
            for (Character literal : literalTruthAssignments.keySet()) {
                boolean truthValue = (i & (1 << literalIndex)) != 0;
                literalTruthAssignments.put(literal, truthValue);
                literalIndex++;
            }
            literalIndex = 0;

            // If this assignment satisfies the formula, add a copy to the set.
            if (isSat(clauses, literalTruthAssignments)) {
                satTruthAssignments.add(new HashMap<>(literalTruthAssignments));
            }
        }
        return satTruthAssignments;
    }

    // Enumerate assignments until a satisfying one is found; return it immediately.
    // Returns null if no satisfying assignment exists.
    private static HashMap<Character, Boolean> bruteForceFirst(ArrayList<ArrayList<Character>> clauses) {
        HashMap<Character, Boolean> literalTruthAssignments = new HashMap<>();

        // Initialize variable map to false for all discovered variables.
        for (ArrayList<Character> clause : clauses) {
            for (Character literal : clause) {
                literalTruthAssignments.put(Character.toLowerCase(literal), false);
            }
        }

        int totalCombinations = (int) Math.pow(2, literalTruthAssignments.size());

        // Iterate assignments; return the first one that satisfies the CNF.
        int literalIndex = 0;
        for (int i = 0; i < totalCombinations; i++) {
            for (Character literal : literalTruthAssignments.keySet()) {
                boolean truthValue = (i & (1 << literalIndex)) != 0;
                literalTruthAssignments.put(literal, truthValue);
                literalIndex++;
            }
            literalIndex = 0;

            if (isSat(clauses, literalTruthAssignments)) {
                return new HashMap<>(literalTruthAssignments);
            }
        }
        return null;
    }

    // Evaluate whether the current assignment satisfies all clauses.
    // - Each clause is a disjunction of literals (OR).
    // - The formula is a conjunction of clauses (AND).
    // - Uppercase literal means negate the underlying variable's truth value.
    private static boolean isSat(ArrayList<ArrayList<Character>> clauses, HashMap<Character, Boolean> literalTruthAssignments){
        boolean sat = true;
        for (ArrayList<Character> clause : clauses) {
            boolean clauseBool = false;
            for (Character literal : clause) {
                boolean literalBool = literalTruthAssignments.get(Character.toLowerCase(literal));
                if (Character.isUpperCase(literal)) {
                    literalBool = !literalBool;
                }
                clauseBool |= literalBool;
            }
            sat &= clauseBool;
        }
        return sat;
    }

    // Demo/benchmark: runs early-stopping and full enumeration, printing timing and memory.
    public static void main(String[] args) throws IOException {
        String formula = "(-o v -j) ^ (-f v l v l v t) ^ (-u) ^ (k) ^ (-q v n v -e v s) ^ (b v r) ^ (i) ^ (a v m) ^ (g v c) ^ (p) ^ (-d) ^ (w) ^ (h)";
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);

        long startTime = System.nanoTime();

        Runtime runtime = Runtime.getRuntime();
        runtime.gc();
        long startMemory = runtime.totalMemory() - runtime.freeMemory();

        // Early-stopping brute force
        HashMap<Character, Boolean> satAssignment = bruteForceEarlyStopping(clauses);

        long endTime = System.nanoTime();
        long endMemory = runtime.totalMemory() - runtime.freeMemory();

        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;
        double memoryUsedMB = (endMemory - startMemory) / (1024.0 * 1024.0);

        System.out.println("Execution time: " + durationInSeconds + " seconds");
        System.out.println("Memory used: " + memoryUsedMB + " MB");

        System.out.println();
        System.out.println("Brute force with early stopping:");
        System.out.println(satAssignment);
        System.out.println();

        // Full enumeration of all models
        HashSet<HashMap<Character, Boolean>> allSatAssignments = bruteForce(clauses);
        System.out.println();
        System.out.println("Brute force sat:");
        System.out.println(allSatAssignments);
        System.out.println();
    }
}