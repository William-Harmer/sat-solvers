package com.github.williamharmer.simplifications;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.utilities.OppositePolarity;

public class PureLiteralElimination {

    public static void pureLiteralElimination(ArrayList<ArrayList<Character>> clauses, HashMap<Character, Boolean> pureLiterals){
        // Create a map to store all the literals
        HashSet<Character> uniqueLiterals = new HashSet<>();

        // Iterate through each clause
        for (ArrayList<Character> clause : clauses) {
            uniqueLiterals.addAll(clause);
        }

//        System.out.println("Unique literals: " + uniqueLiterals);

        for (Character literal : uniqueLiterals) {
            if (uniqueLiterals.contains(OppositePolarity.oppositePolarity(literal))) { // Not pure
            } else{ // Pure
                if (Character.isUpperCase(literal)) {
                    pureLiterals.put(Character.toLowerCase(literal), false);
                } else {
                    pureLiterals.put(literal, true);
                }
            }
        }
        uniqueLiterals.clear();
//        System.out.println("Pure literals: " + pureLiterals);


        for (int i = 0; i < clauses.size(); i++) { // For each clause
            for (Character literal : clauses.get(i)) { // For each literal in the clause
                if (pureLiterals.containsKey(Character.toLowerCase(literal))) { // If the literal is pure
//                    System.out.println("removing " + literal);
                    // Remove the whole clause
                    clauses.remove(i);
                    i--;
                    break;
                }

            }
        }
    }

    public static void main(String[] args) {
        String formula = "(B)^(a)^(a)";
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);
        System.out.println("Formula: " + clauses);
        HashMap<Character, Boolean> pureLiteralsTruthValues = new HashMap<>();
        pureLiteralElimination(clauses, pureLiteralsTruthValues);
        System.out.println("New formula: " + clauses);
        System.out.println(pureLiteralsTruthValues);
    }
}
