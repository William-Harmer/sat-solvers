package com.github.williamharmer.solvers.cdcl;

import java.util.ArrayList;

// Represents a clause in a CDCL (Conflict-Driven Clause Learning) context,
// carrying the clause literals, the trail elements associated with its derivation,
// and the decision level at which it was learned or is relevant.
public class CDCLClause {
    ArrayList<Character> clause;        // The clause as a list of literals (Characters)
    ArrayList<Character> trailElements; // Trail literals involved in deriving/relating to this clause
    int level;                          // Decision level metadata for the clause

    // Construct a CDCLClause with explicit clause literals, trail elements, and level.
    public CDCLClause(ArrayList<Character> clause, ArrayList<Character> trailElements, int level) {
        this.clause = clause;
        this.trailElements = trailElements;
        this.level = level;
    }

    // Copy constructor: creates a new instance with copies of the lists and the same level.
    public CDCLClause(CDCLClause other) {
        this.clause = new ArrayList<>(other.clause);              // Copy of clause literals
        this.trailElements = new ArrayList<>(other.trailElements); // Copy of trail elements
        this.level = other.level;
    }

    // Print a compact representation: ([l1,l2,...], trailElements, level)
    public void print() {
        System.out.print("([");
        for (int i = 0; i < clause.size(); i++) {
            System.out.print(clause.get(i));
            if (i < clause.size() - 1) {
                System.out.print(",");  // Comma-separate literals
            }
        }
        System.out.print("], " + trailElements + ", " + level + ")");
    }
}