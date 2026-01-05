package com.github.williamharmer.solvers.cdcl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeSet;

import com.github.williamharmer.cnfparser.CNFParserCDCL;
import com.github.williamharmer.utilities.OppositePolarity;

public class CDCL {


    public static HashMap<Character, Boolean> cDCL(ArrayList<CDCLClause> formula){
        // Create a stack that holds the formula at each decision level
        Stack<ArrayList<CDCLClause>> formulaStack = new Stack<>();
        int decisionLevel = 0;
//        System.out.println("The formula as it is being added in the stack:");
//        print(formula);
//        System.out.println();
        ArrayList<CDCLClause> copiedFormula2 = deepCopyFormula(formula);
        formulaStack.push(copiedFormula2);

        outerLoop: while(true){
            // Unit prop the formula
            cDCLUnitProp(formula,decisionLevel);

//            System.out.println("The formula after unit prop:");
//            print(formula);

            // Check for sat / unsat
            boolean allUnitClauses = true;
            for (CDCLClause clause : formula) {
                if (clause.clause.size() != 1) {
                    allUnitClauses = false;
                }
                if (clause.clause.isEmpty()) {
                    if (decisionLevel == 0) {
//                        System.out.println("UNSAT");
                        // return an empty hashmap
                        return new HashMap<>();
                    } else {
//                        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                        System.out.println("Going to have to learn a new clause");

                        // Get the learned clause
                        CDCLClause learnedClause = getLearnedClause(formula, clause, decisionLevel);

//                        System.out.println("The learned clause is ");
//                        learnedClause.print();
//                        System.out.println();

//                        System.out.println("Going to now backtrack the stack ");
//                        System.out.println("--------------------------");
//                        System.out.println("Before Stack:");
//                        for (ArrayList<CDCLClause> stackItem : formulaStack) {
//                            print(stackItem);
//                        }
//                        System.out.println("--------------------------");

                        // Backtrack to that learned clauses formula on the stack
                        while(formulaStack.size() > learnedClause.level+1){
                            formulaStack.pop();
                        }

//                        System.out.println("--------------------------");
//                        System.out.println("After Stack:");
//                        for (ArrayList<CDCLClause> stackItem : formulaStack) {
//                            print(stackItem);
//                        }
//                        System.out.println("--------------------------");
//
//                        System.out.println("Get the top item from the stack, set that as the formula and pop it");

                        // Now get the top item and set that as the formula
                        formula = formulaStack.pop();
//                        print(formula);

//                        System.out.println("--------------------------");
//                        System.out.println("Stack after formula is taken and popped");
//                        for (ArrayList<CDCLClause> stackItem : formulaStack) {
//                            print(stackItem);
//                        }
//                        System.out.println("--------------------------");
//
//                        System.out.println("Add learned clause onto end");
                        // Add the learned clause to the end
                        formula.add(learnedClause);
//                        print(formula);

//                        System.out.println("Put back onto stack with the new learned clause");
                        // Add that back onto the stack
                        formulaStack.push(deepCopyFormula(formula));
//                        System.out.println("--------------------------");
//                        System.out.println("Stack after added back on with learned clause");
//                        for (ArrayList<CDCLClause> stackItem : formulaStack) {
//                            print(stackItem);
//                        }
//                        System.out.println("--------------------------");
                        // Change the decision level
                        decisionLevel = learnedClause.level; // Decision level meant to be learnedClause.level+1??????
//                        System.out.println("Decision level is now "+ decisionLevel);
//                        System.out.println("End of learning!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
//                        System.out.println("iteration++");
//                        System.out.println();

                        // Go through the while loop again
                        continue outerLoop;
                    }
                }
            }

            if (allUnitClauses) {
//                System.out.println("SAT");
                HashMap<Character, Boolean> answer = new HashMap<>();
                for(CDCLClause clause : formula){ // for each clause in the formula
                    if (Character.isLowerCase(clause.clause.getFirst())) {
                        answer.put(clause.clause.getFirst(),true);
                    } else {
                        answer.put(Character.toLowerCase(clause.clause.getFirst()),false);
                    }
                }
                return answer;
            }


//            System.out.println("No conflict, decision being made");
//            System.out.println("Formula before decision");
//            print(formula);

            // If neither then add the clause to the stack and make a decision

            if (decisionLevel != 0){
                ArrayList<CDCLClause> copiedFormula = deepCopyFormula(formula);
                formulaStack.push(copiedFormula);
            } else {
                // Replace the top item in the stack
                formulaStack.set(formulaStack.size() - 1, deepCopyFormula(formula));
            }


            decisionLevel++;
            addFirstElementNotAUnitClauseAsNewClauseToFormula(formula,decisionLevel,false);

            // Print out the stack for debugging
//            System.out.println("--------------------------");
//            System.out.println("Current Stack:");
//            for (ArrayList<CDCLClause> stackItem : formulaStack) {
//                print(stackItem);
//            }
//            System.out.println("--------------------------");


//            System.out.println("Formula after decision");
//            print(formula);
//            System.out.println("Iteration ++ with decision level now set as: " + decisionLevel);
//            System.out.println();

        }
    }

    private static ArrayList<CDCLClause> deepCopyFormula(ArrayList<CDCLClause> formula) {
        ArrayList<CDCLClause> copiedFormula = new ArrayList<>();
        for (CDCLClause clause : formula) {
            copiedFormula.add(new CDCLClause(clause)); // Use the copy constructor
        }
        return copiedFormula;
    }



    private static Character findFirstUIP(ArrayList<CDCLClause> formula, CDCLClause emptyClause, int decisionLevel){
//        System.out.println("Decision level: " + decisionLevel);
        // Will this go all the way back to the decision or will it give me an error in that case?

        // We have the empty clause already
        // put all the characters from emptyClause.trailElements into the tree set
        // Add each literal from trailElements into the TreeSet
        TreeSet<Character> set = new TreeSet<>(Comparator.comparingInt(Character::toLowerCase));
        set.addAll(emptyClause.trailElements);
//        System.out.println("Finding UIP:");
//        System.out.println("Starting set : "+ set);

        while (set.size() > 1){
            for (CDCLClause clause : formula) {
                if (clause.clause.size() == 1 && (clause.clause.contains(Character.toLowerCase(set.last())) || clause.clause.contains(Character.toUpperCase(set.last()))) && clause.level == decisionLevel) { // Check if the clause is a unit clause, contains the last element, and has the same decision level
//                    System.out.println(set.last() + " connected to...");
                    set.remove(set.last());
//                    System.out.println(clause.trailElements);
                    set.addAll(clause.trailElements); // Add all the letters in trailElements to the set
//                    System.out.println(set);
                    break;
                }
            }
        }
        return set.last();
    }

    private static CDCLClause getLearnedClause(ArrayList<CDCLClause> formula, CDCLClause emptyClause, int decisionLevel) {
        // Find the first UIP (Unique Implication Point)
        Character firstUIP = findFirstUIP(formula, emptyClause, decisionLevel);
//        System.out.println("First UIP: " + firstUIP);
//
//        System.out.println("The formula after finding the first UIP (Should not change):");
//        print(formula);

        // Find the earliest decision node (lowest decision level > 0)
        CDCLClause startNode = null;
        for (CDCLClause clause : formula) {
            if (clause.clause.size() == 1 && clause.trailElements.isEmpty()) {
                // Set startNode to the clause with the smallest decision level
                if (startNode == null || clause.level < startNode.level) {
                    startNode = clause;
                }
            }
        }
//        System.out.println();
//        System.out.println("Start node is: ");
//        startNode.print();
//        System.out.println();

//        System.out.println("Earliest decision node:");
//        startNode.print();
//        System.out.println();

        // Use TreeSet to maintain an ordered set of literals
        TreeSet<Character> set = new TreeSet<>(Comparator.comparingInt(Character::toLowerCase));

        // Use HashSet to store learned clause literals while ensuring uniqueness
        HashSet<Character> learnedSet = new HashSet<>();

//        System.out.println();
//        System.out.println("Now we are getting the learned clause: ");

        // Add the first literal of the startNode to the set
        set.add(startNode.clause.getFirst());
//        System.out.println("Set start: ");
//        System.out.println(set);

        whileLoop:
            while (Character.toLowerCase(set.first()) <= Character.toLowerCase(firstUIP)) {
//                System.out.println("All elements in the set are not greater than the UIP SO we must go forward");
                char smallest = set.first(); // Get the smallest element (ignoring case)


                // Iterate through all formula
                for (CDCLClause clause : formula) {

                    // Check if the clause is a unit clause and contains the smallest element
                    if (clause.clause.size() == 1 &&
                            (clause.trailElements.contains(Character.toLowerCase(smallest)) || clause.trailElements.contains(Character.toUpperCase(smallest)))) {
//                        System.out.println("Found a clause that contains element " + smallest);

                        char unitLiteral = clause.clause.getFirst(); // Get the only literal in the unit clause

                        // If the unit literal is greater than the UIP (ignoring case)
                        if (Character.toLowerCase(unitLiteral) > Character.toLowerCase(firstUIP)) {
                            learnedSet.add(OppositePolarity.oppositePolarity(smallest)); // Add opposite polarity to HashSet (no duplicates)
                        }

                        // Add unitLiteral to the processing set
                        set.add(unitLiteral);
                    } else if (clause.clause.isEmpty() &&
                            (clause.trailElements.contains(Character.toLowerCase(smallest)) || clause.trailElements.contains(Character.toUpperCase(smallest)))){ // Start node is directly hitting the conflict
//                        System.out.println("Start node is directly hitting conflict");

                        learnedSet.add(OppositePolarity.oppositePolarity(smallest));

                        // Somehow break out of the while loop
                        break whileLoop;

                    }
                }

                // Remove the smallest element from the set after processing
                set.remove(smallest);
    //            System.out.println("Current set: " + set);
            }

        // Convert HashSet to ArrayList for the learned clause
        ArrayList<Character> learnedClause = new ArrayList<>(learnedSet);
//        System.out.println("Learned clause: " + learnedClause);

        // Determine the backtracking level (lowest decision level in learned clause)
        int level = -1; // Initialize level to -1 (unset)

        // Iterate over literals in the learned clause to determine the lowest decision level
        // This can be optimised and included in the for loop above??
        for (Character literal : learnedClause) {
            for (CDCLClause clause : formula) {
                // Check if the clause contains the literal (or its opposite polarity)
                if (clause.clause.size() == 1 &&
                        (clause.clause.contains(literal) || clause.clause.contains(OppositePolarity.oppositePolarity(literal)))) {
                    // Update the level to the lowest encountered level
                    if (level == -1 || clause.level < level) {
                        level = clause.level;
                    }
                }
            }
        }

        // Return the new learned clause with its computed decision level
        return new CDCLClause(learnedClause, new ArrayList<>(), Math.max(0, level - 1));
    }



    private static void addFirstElementNotAUnitClauseAsNewClauseToFormula(
            ArrayList<CDCLClause> clauses, int decisionLevel, boolean useOppositePolarity) {

        for (CDCLClause cdclClause : clauses) {
            // Check if the clause is not a unit clause
            if (cdclClause.clause.size() > 1) {
                char firstLiteral = cdclClause.clause.getFirst(); // Get the first literal

                // If useOppositePolarity is true, apply the opposite polarity to the first literal
                if (useOppositePolarity) {
                    firstLiteral = OppositePolarity.oppositePolarity(firstLiteral);
                }

                // Create the new clause with the modified first literal
                ArrayList<Character> newClause = new ArrayList<>();
                newClause.add(firstLiteral);  // No need for useCaps anymore

                // Create a new CDCLClause object with the updated clause and current decision level
                // Leave trailElements empty and set the decisionLevel
                CDCLClause newCDCLClause = new CDCLClause(newClause, new ArrayList<>(), decisionLevel);

                // Add the new CDCLClause to the clauses list
                clauses.add(newCDCLClause);

                // Return the updated clauses
                return;
            }
        }
    }



    private static void cDCLUnitProp(ArrayList<CDCLClause> clauses, int decisionLevel) {
        boolean formulaModified = true;

        // Keep processing until no more modifications are made
        while (formulaModified) {
//            print(clauses);
            formulaModified = false;

            // Iterate through the clauses
            for (int j = 0; j < clauses.size(); j++) {
                CDCLClause currentClause = clauses.get(j);

                // Check if the clause is a unit clause (only one literal)
                if (currentClause.clause.size() == 1) {
                    char unitClause = currentClause.clause.getFirst();

                    // Iterate through clauses again to handle the unit clause
                    for (int i = 0; i < clauses.size(); i++) {
                        CDCLClause clauseToCheck = clauses.get(i);

                        // Skip the current clause (j-th clause)
                        if (clauseToCheck == currentClause) continue;

                        boolean containsUnit = clauseToCheck.clause.contains(unitClause);
                        boolean containsOpposite = clauseToCheck.clause.contains(OppositePolarity.oppositePolarity(unitClause));

                        if (containsUnit) { // Clause contains the same polarity unit clause
                            clauses.remove(i); // Remove the clause altogether
                            i--; // Adjust index to account for removal
                            if (i < j) {
                                j--;
                            } // Adjust to revisit the current clause
                            formulaModified = true;

                        } else if (containsOpposite) { // Clause contains the opposite polarity unit clause
                            while (clauseToCheck.clause.contains(OppositePolarity.oppositePolarity(unitClause))) {
                                // Move opposite unit to trailElements
                                clauseToCheck.trailElements.add(OppositePolarity.oppositePolarity(unitClause));
                                clauseToCheck.clause.remove((Character) OppositePolarity.oppositePolarity(unitClause));
                            }
                            clauseToCheck.level = decisionLevel; // Set decision level for the clause
                            formulaModified = true;
                        }
                    }
                }
            }
        }
    }

    public static void print(ArrayList<CDCLClause> clauses) {
        for (CDCLClause clause : clauses) {
            clause.print();
            System.out.print(" ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        String formula = "(abc)(abC)(Bd)(aBD)(Aef)(AeF)(EF)(AEf)";
        ArrayList<CDCLClause> CDCLClauses = CNFParserCDCL.formulaToCDCLArrayList(formula);

        HashMap<Character, Boolean> answer = cDCL(CDCLClauses);
        System.out.println(answer);
    }
}
