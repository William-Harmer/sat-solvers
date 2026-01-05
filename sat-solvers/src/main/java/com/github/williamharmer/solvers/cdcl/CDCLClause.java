package com.github.williamharmer.solvers.cdcl;

import java.util.ArrayList;

public class CDCLClause {
    ArrayList<Character> clause;
    ArrayList<Character> trailElements;
    int level;

    // Constructor to initialize the CDCLClause
    public CDCLClause(ArrayList<Character> clause, ArrayList<Character> trailElements, int level) {
        this.clause = clause;
        this.trailElements = trailElements;
        this.level = level;
    }

     public CDCLClause(CDCLClause other) {
         this.clause = new ArrayList<>(other.clause);  // Deep copy the clause list
         this.trailElements = new ArrayList<>(other.trailElements);  // Deep copy the trailElements list
         this.level = other.level;
     }

     public void print() {
        System.out.print("([");
        for (int i = 0; i < clause.size(); i++) {
            System.out.print(clause.get(i));
            if (i < clause.size() - 1) {
                System.out.print(",");  // Add a comma between elements
            }
        }
        System.out.print("], " + trailElements + ", " + level + ")");
    }
}
