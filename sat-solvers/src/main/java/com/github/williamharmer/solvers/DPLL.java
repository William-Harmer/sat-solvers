package com.github.williamharmer.solvers;

import java.util.ArrayList;
import java.util.HashMap;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.simplifications.PureLiteralElimination;
import com.github.williamharmer.simplifications.UnitPropagation;

public class DPLL {

    private static boolean executeDPLL(ArrayList<ArrayList<Character>> clauses, HashMap<Character, Boolean> literalTruthValues){
//        System.out.println("Formula:" + clauses);
        // Is there ever a scenario where you need to unit prop again after pure literal?? With my testing I think no

        UnitPropagation.unitPropagation(clauses);
//        System.out.println("Unit prop: " + clauses);

        PureLiteralElimination.pureLiteralElimination(clauses, literalTruthValues);
//        System.out.println("Pure lit: " + clauses);
//        System.out.println(literalTruthValues);

        if(clauses.isEmpty()){ // SAT
//            System.out.println("SAT");
            return true;
        } else if (clauses.stream().anyMatch(ArrayList::isEmpty)) { // Not SAT
//            System.out.println("Not sat");
            return false;
        }

//        System.out.println("Recursing");

        // Create copies to preserve the original clause structure and truth table
//        ArrayList<ArrayList<Character>> clausesCopy = Utility.clauseCopy(clauses);
//        HashMap<Character, Boolean> truthTableCopy = deepCopyTruthTable(literalTruthValues);

        return executeDPLL(addFirstElementNotAUnitClauseAsNewClauseToFormula(clauses,false), literalTruthValues)
                || executeDPLL(addFirstElementNotAUnitClauseAsNewClauseToFormula(clauses, true), literalTruthValues);
    }

    private static ArrayList<ArrayList<Character>> addFirstElementNotAUnitClauseAsNewClauseToFormula(
            ArrayList<ArrayList<Character>> clauses, boolean useCaps) {

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

    // New secondary function to get the literal truth values
    public static HashMap<Character, Boolean> dPLL(ArrayList<ArrayList<Character>> clauses) {
        HashMap<Character, Boolean> literalTruthValues = new HashMap<>();
        boolean result = executeDPLL(clauses, literalTruthValues);

        if (result) {
//            System.out.println("DPLL Succeeded, Final truth values: " + literalTruthValues);
        } else {
//            System.out.println("DPLL Failed to satisfy the formula.");
            literalTruthValues = new HashMap<>();  // Return an empty map to indicate failure
        }

        return literalTruthValues;
    }

    public static void main(String[] args) {
        String formula = "(-lvc)^(-pvd)^(lvpv-cv-d)";
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);
        System.out.println("Initial formula clauses: " + clauses);

        // Get literal truth values by calling the new function
        HashMap<Character, Boolean> literalTruthValues = dPLL(clauses);
        System.out.println("Final literal truth values: " + literalTruthValues);
    }
}
