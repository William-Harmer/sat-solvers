package com.github.williamharmer.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.github.williamharmer.cnfparser.CNFParser;

public class BruteForce {
    public static HashSet<HashMap<Character, Boolean>> bruteForce(ArrayList<ArrayList<Character>> clauses) {
        return (HashSet<HashMap<Character, Boolean>>) bruteForce(clauses, false);
    }

    public static HashMap<Character, Boolean> bruteForceEarlyStopping(ArrayList<ArrayList<Character>> clauses) {
        return (HashMap<Character, Boolean>) bruteForce(clauses, true);
    }

    private static Object bruteForce(ArrayList<ArrayList<Character>> clauses, boolean earlyStopping) {
        HashMap<Character, Boolean> literalTruthAssignments = new HashMap<>();

        // Get all unique literals
        for (ArrayList<Character> clause : clauses) {
            for (Character literal : clause) {
                literalTruthAssignments.put(Character.toLowerCase(literal), false);
            }
        }

        int totalCombinations = (int) Math.pow(2, literalTruthAssignments.size());  // 2^n combinations

        // Create the hashset to store all the truth assignments if early stopping is not true
        HashSet<HashMap<Character, Boolean>> satTruthAssignments = null;
        if (!earlyStopping) {
            satTruthAssignments = new HashSet<>();
        }

        int literalIndex = 0;
        for (int i = 0; i < totalCombinations; i++) { // For all combinations there are
            for (Character literal : literalTruthAssignments.keySet()) { // For each key in the map

                // Calculate the truth value for the jth bit in i, j is literalIndex.
                boolean truthValue = (i & (1 << literalIndex)) != 0;

                // Set the corresponding truth value in the map
                literalTruthAssignments.put(literal, truthValue);

                // Move to the next literal index
                literalIndex++;
            }
            literalIndex = 0;

            if (isSat(clauses, literalTruthAssignments)) { // If the truth values make it sat
                if (earlyStopping) { // if early stopping is on, just return the hash map
                    return literalTruthAssignments;
                } else { // If early stopping is on, add it to the set
                    satTruthAssignments.add(new HashMap<>(literalTruthAssignments));
                }
            }
        }
        return satTruthAssignments;
    }

    private static boolean isSat(ArrayList<ArrayList<Character>> clauses, HashMap<Character, Boolean> literalTruthAssignments){
        boolean sat = true;
        for (ArrayList<Character> clause : clauses) {
            boolean clauseBool = false;
            for (Character literal : clause) {
                // Check if the literal is uppercase (indicating negation)
                boolean literalBool = literalTruthAssignments.get(Character.toLowerCase(literal));

                // If literal is uppercase, negate the value
                if (Character.isUpperCase(literal)) {
                    literalBool = !literalBool;
                }

                // OR it with clauseBool
                clauseBool |= literalBool;
            }
            sat &= clauseBool;
        }
        return sat;
    }

    public static void main(String[] args) throws IOException {

        String formula = "(j) ^ (m v -c) ^ (e v h v j) ^ (k) ^ (d) ^ (a v i) ^ (l) ^ (d v -k) ^ (b) ^ (-k v -f) ^ (g)";
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);

        // Brute force with early stopping
        long startTime = System.nanoTime();

        // Get initial memory usage
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Suggest to garbage collect
        long startMemory = runtime.totalMemory() - runtime.freeMemory();

        // Execute your method
        HashMap<Character, Boolean> satAssignment = bruteForceEarlyStopping(clauses);

        // Stop timer
        long endTime = System.nanoTime();

        // Get final memory usage
        long endMemory = runtime.totalMemory() - runtime.freeMemory();

        // Calculate time in seconds
        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;
        // Calculate memory used in MB
        double memoryUsedMB = (endMemory - startMemory) / (1024.0 * 1024.0);

        // Print results
        System.out.println("Execution time: " + durationInSeconds + " seconds");
        System.out.println("Memory used: " + memoryUsedMB + " MB");

        System.out.println();
        System.out.println("Brute force with early stopping:");
        System.out.println(satAssignment);
        // System.out.println(satAssignment);  // Your commented-out println preserved
        System.out.println();

        // Brute force without early stopping
        HashSet<HashMap<Character, Boolean>> allSatAssignments = bruteForce(clauses);
        System.out.println();
        System.out.println("Brute force sat:");
        System.out.println(allSatAssignments);
        // System.out.println(allSatAssignments); // Your commented-out println preserved
        System.out.println();
    }
}
