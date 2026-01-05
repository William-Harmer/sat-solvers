package com.github.williamharmer.solvers;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import com.github.williamharmer.cnfparser.CNFParser;
import com.github.williamharmer.simplifications.UnitPropagation;

public class UPAndBF {

    public static HashMap<Character, Boolean> uPAndBF(ArrayList<ArrayList<Character>> clauses) {
//        System.out.println("The 2D arraylist: " + clauses);

        UnitPropagation.unitPropagation(clauses);
//        System.out.println("The 2D arraylist after unit propagation: " + clauses);

        if (clauses.stream().anyMatch(ArrayList::isEmpty)) {
            return null;
        }

        return BruteForce.bruteForceEarlyStopping(clauses);
    }

    public static void main(String[] args) throws IOException {
        String formula = "(-o v -j) ^ (-f v l v l v t) ^ (-u) ^ (k) ^ (-q v n v -e v s) ^ (b v r) ^ (i) ^ (a v m) ^ (g v c) ^ (p) ^ (-d) ^ (w) ^ (h)";
        ArrayList<ArrayList<Character>> clauses = CNFParser.formulaTo2DArrayList(formula);

        HashMap<Character, Boolean> satAssignment = uPAndBF(clauses); // Changed to HashMap
        if (satAssignment != null) {
            System.out.println("SAT Assignment: " + satAssignment);
        } else {
            System.out.println("Formula is unsatisfiable.");
        }
    }
}
