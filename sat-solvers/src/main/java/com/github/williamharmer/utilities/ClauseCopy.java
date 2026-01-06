package com.github.williamharmer.utilities;

import java.util.ArrayList;

// Utility for making a deep-ish copy of a CNF clause structure.
// Input/Output shape:
// - original: ArrayList of clauses, where each clause is an ArrayList<Character>
//   representing literals (e.g., 'A' for positive, 'a' for negated).
// Behavior:
// - Produces a new outer list and, for each inner list (clause), creates a new
//   ArrayList with the same Character elements.
// - Characters are immutable, so copying the inner lists yields an effective
//   deep copy for this 2D structure (no shared inner lists).
// - The method avoids aliasing between solvers so mutations in one do not affect others.

public class ClauseCopy {
    public static ArrayList<ArrayList<Character>> clauseCopy(ArrayList<ArrayList<Character>> original) {
        ArrayList<ArrayList<Character>> copy = new ArrayList<>();
        for (ArrayList<Character> innerList : original) {
            copy.add(new ArrayList<>(innerList)); // Create a new list and copy elements
        }
        return copy;
    }
}