package com.github.williamharmer.utilities;

import java.util.ArrayList;

public class ClauseCopy {
    public static ArrayList<ArrayList<Character>> clauseCopy(ArrayList<ArrayList<Character>> original) {
        ArrayList<ArrayList<Character>> copy = new ArrayList<>();
        for (ArrayList<Character> innerList : original) {
            copy.add(new ArrayList<>(innerList)); // Create a new list and copy elements
        }
        return copy;
    }
}
