package com.github.williamharmer.solvers.cdcl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Stack;
import java.util.TreeSet;

import com.github.williamharmer.cnfparser.CNFParserCDCL;
import com.github.williamharmer.utilities.OppositePolarity;

// Conflict-Driven Clause Learning (CDCL) solver.
// Maintains a stack of formulas at decision levels, performs unit propagation,
// detects conflicts, learns a clause, backtracks to an appropriate level,
// and continues until SAT or UNSAT is determined.
public class CDCL {

    // Entry point for the CDCL procedure.
    // Returns a satisfying assignment as a map (variable -> value) if SAT,
    // or an empty map if UNSAT is proven at level 0.
    public static HashMap<Character, Boolean> cDCL(ArrayList<CDCLClause> formula){
        // Stack holds snapshots of the formula at each decision level for backtracking.
        Stack<ArrayList<CDCLClause>> formulaStack = new Stack<>();
        int decisionLevel = 0;

        // Initialize stack with a deep copy of the starting formula.
        ArrayList<CDCLClause> copiedFormula2 = deepCopyFormula(formula);
        formulaStack.push(copiedFormula2);

        outerLoop: while(true){
            // Propagate implications from current unit clauses at this level.
            cDCLUnitProp(formula,decisionLevel);

            // Check for SAT/UNSAT and prepare for decisions or conflict handling.
            boolean allUnitClauses = true;
            for (CDCLClause clause : formula) {
                if (clause.clause.size() != 1) {
                    allUnitClauses = false;
                }
                if (clause.clause.isEmpty()) {
                    // Conflict found
                    if (decisionLevel == 0) {
                        // At level 0, a conflict implies UNSAT.
                        return new HashMap<>();
                    } else {
                        // Learn a new clause from the conflict and backtrack.
                        CDCLClause learnedClause = getLearnedClause(formula, clause, decisionLevel);

                        // Backtrack the stack to the learned clause's target level.
                        while(formulaStack.size() > learnedClause.level+1){
                            formulaStack.pop();
                        }

                        // Restore formula from the stack snapshot at that level.
                        formula = formulaStack.pop();

                        // Add the learned clause to the formula.
                        formula.add(learnedClause);

                        // Push updated formula back to the stack.
                        formulaStack.push(deepCopyFormula(formula));

                        // Set current decision level to the learned backtrack level.
                        decisionLevel = learnedClause.level;

                        // Restart the outer loop at the new level.
                        continue outerLoop;
                    }
                }
            }

            // If all clauses are unit, we can read off a model from them (SAT).
            if (allUnitClauses) {
                HashMap<Character, Boolean> answer = new HashMap<>();
                for(CDCLClause clause : formula){
                    if (Character.isLowerCase(clause.clause.getFirst())) {
                        answer.put(clause.clause.getFirst(),true);
                    } else {
                        answer.put(Character.toLowerCase(clause.clause.getFirst()),false);
                    }
                }
                return answer;
            }

            // No conflict and not all unit: make a decision.
            if (decisionLevel != 0){
                ArrayList<CDCLClause> copiedFormula = deepCopyFormula(formula);
                formulaStack.push(copiedFormula);
            } else {
                // Replace the base snapshot when at level 0.
                formulaStack.set(formulaStack.size() - 1, deepCopyFormula(formula));
            }

            // Increase decision level and assert a new decision literal as a unit clause.
            decisionLevel++;
            addFirstElementNotAUnitClauseAsNewClauseToFormula(formula,decisionLevel,false);
        }
    }

    // Deep copy the formula (list of CDCLClause) using the copy constructor.
    private static ArrayList<CDCLClause> deepCopyFormula(ArrayList<CDCLClause> formula) {
        ArrayList<CDCLClause> copiedFormula = new ArrayList<>();
        for (CDCLClause clause : formula) {
            copiedFormula.add(new CDCLClause(clause));
        }
        return copiedFormula;
    }

    // Find the first Unique Implication Point (UIP) for the given conflict at a decision level.
    // Traverses unit implications backwards using trail information to identify the UIP.
    private static Character findFirstUIP(ArrayList<CDCLClause> formula, CDCLClause emptyClause, int decisionLevel){
        // Track frontier of implication literals; TreeSet orders by variable identity (case-insensitive).
        TreeSet<Character> set = new TreeSet<>(Comparator.comparingInt(Character::toLowerCase));
        set.addAll(emptyClause.trailElements);

        // Reduce the frontier until only one literal remains at the current level (the UIP).
        while (set.size() > 1){
            for (CDCLClause clause : formula) {
                // Follow implication edges from unit clauses at the same decision level.
                if (clause.clause.size() == 1 && (clause.clause.contains(Character.toLowerCase(set.last())) || clause.clause.contains(Character.toUpperCase(set.last()))) && clause.level == decisionLevel) {
                    set.remove(set.last());
                    set.addAll(clause.trailElements);
                    break;
                }
            }
        }
        return set.last();
    }

    // Derive a learned clause from a conflict by analyzing the implication graph
    // and computing a backtrack level. Returns the learned clause with its target level.
    private static CDCLClause getLearnedClause(ArrayList<CDCLClause> formula, CDCLClause emptyClause, int decisionLevel) {
        // Identify the first UIP for this conflict.
        Character firstUIP = findFirstUIP(formula, emptyClause, decisionLevel);

        // Find the earliest decision node (lowest level > 0) among unit decision clauses.
        CDCLClause startNode = null;
        for (CDCLClause clause : formula) {
            if (clause.clause.size() == 1 && clause.trailElements.isEmpty()) {
                if (startNode == null || clause.level < startNode.level) {
                    startNode = clause;
                }
            }
        }

        // Ordered set to process literals deterministically by variable identity.
        TreeSet<Character> set = new TreeSet<>(Comparator.comparingInt(Character::toLowerCase));
        // Learned clause literal set (unique).
        HashSet<Character> learnedSet = new HashSet<>();

        // Seed with the decision literal from the earliest decision node.
        set.add(startNode.clause.getFirst());

        // Traverse forward until we cross beyond the UIP in ordering.
        whileLoop:
        while (Character.toLowerCase(set.first()) <= Character.toLowerCase(firstUIP)) {
            char smallest = set.first();

            for (CDCLClause clause : formula) {

                // If this clause was implied using the current literal, extend the frontier.
                if (clause.clause.size() == 1 &&
                        (clause.trailElements.contains(Character.toLowerCase(smallest)) || clause.trailElements.contains(Character.toUpperCase(smallest)))) {

                    char unitLiteral = clause.clause.getFirst();

                    // If the implication is beyond the UIP, we add the opposite polarity to the learned set.
                    if (Character.toLowerCase(unitLiteral) > Character.toLowerCase(firstUIP)) {
                        learnedSet.add(OppositePolarity.oppositePolarity(smallest));
                    }

                    set.add(unitLiteral);
                } else if (clause.clause.isEmpty() &&
                        (clause.trailElements.contains(Character.toLowerCase(smallest)) || clause.trailElements.contains(Character.toUpperCase(smallest)))){
                    // Direct conflict reached; add opposing literal and stop.
                    learnedSet.add(OppositePolarity.oppositePolarity(smallest));
                    break whileLoop;
                }
            }

            // Remove processed literal from the frontier.
            set.remove(smallest);
        }

        // Build the learned clause from the collected literals.
        ArrayList<Character> learnedClause = new ArrayList<>(learnedSet);

        // Compute a backtrack level based on the minimum decision level of involved unit clauses.
        int level = -1;
        for (Character literal : learnedClause) {
            for (CDCLClause clause : formula) {
                if (clause.clause.size() == 1 &&
                        (clause.clause.contains(literal) || clause.clause.contains(OppositePolarity.oppositePolarity(literal)))) {
                    if (level == -1 || clause.level < level) {
                        level = clause.level;
                    }
                }
            }
        }

        // Ensure non-negative decision level. Return the learned clause with computed level.
        return new CDCLClause(learnedClause, new ArrayList<>(), Math.max(0, level - 1));
    }

    // Make a decision by taking the first literal of the first non-unit clause
    // and asserting it (or its opposite) as a new unit clause at the current level.
    private static void addFirstElementNotAUnitClauseAsNewClauseToFormula(
            ArrayList<CDCLClause> clauses, int decisionLevel, boolean useOppositePolarity) {

        for (CDCLClause cdclClause : clauses) {
            if (cdclClause.clause.size() > 1) {
                char firstLiteral = cdclClause.clause.getFirst();

                if (useOppositePolarity) {
                    firstLiteral = OppositePolarity.oppositePolarity(firstLiteral);
                }

                ArrayList<Character> newClause = new ArrayList<>();
                newClause.add(firstLiteral);

                CDCLClause newCDCLClause = new CDCLClause(newClause, new ArrayList<>(), decisionLevel);
                clauses.add(newCDCLClause);
                return;
            }
        }
    }

    // Unit propagation for the CDCL representation.
    // - Removes clauses satisfied by a unit literal.
    // - Removes opposite literals from clauses, recording them in trailElements.
    // - Sets the decision level on modified clauses.
    private static void cDCLUnitProp(ArrayList<CDCLClause> clauses, int decisionLevel) {
        boolean formulaModified = true;

        // Repeat until no further simplification occurs.
        while (formulaModified) {
            formulaModified = false;

            for (int j = 0; j < clauses.size(); j++) {
                CDCLClause currentClause = clauses.get(j);

                // Only process unit clauses as sources of implications.
                if (currentClause.clause.size() == 1) {
                    char unitClause = currentClause.clause.getFirst();

                    // Scan all other clauses and apply the unit literal.
                    for (int i = 0; i < clauses.size(); i++) {
                        CDCLClause clauseToCheck = clauses.get(i);

                        if (clauseToCheck == currentClause) continue;

                        boolean containsUnit = clauseToCheck.clause.contains(unitClause);
                        boolean containsOpposite = clauseToCheck.clause.contains(OppositePolarity.oppositePolarity(unitClause));

                        if (containsUnit) {
                            // Clause satisfied: remove it from the formula.
                            clauses.remove(i);
                            i--;
                            if (i < j) {
                                j--;
                            }
                            formulaModified = true;

                        } else if (containsOpposite) {
                            // Remove opposite literals and record them in the trail.
                            while (clauseToCheck.clause.contains(OppositePolarity.oppositePolarity(unitClause))) {
                                clauseToCheck.trailElements.add(OppositePolarity.oppositePolarity(unitClause));
                                clauseToCheck.clause.remove((Character) OppositePolarity.oppositePolarity(unitClause));
                            }
                            // Mark the level at which this clause was affected.
                            clauseToCheck.level = decisionLevel;
                            formulaModified = true;
                        }
                    }
                }
            }
        }
    }

    // Utility to print the current formula state.
    public static void print(ArrayList<CDCLClause> clauses) {
        for (CDCLClause clause : clauses) {
            clause.print();
            System.out.print(" ");
        }
        System.out.println();
    }

    // Simple driver for testing the CDCL pipeline with a string parser to CDCL clauses.
    public static void main(String[] args) {
        String formula = "(abc)(abC)(Bd)(aBD)(Aef)(AeF)(EF)(AEf)";
        ArrayList<CDCLClause> CDCLClauses = CNFParserCDCL.formulaToCDCLArrayList(formula);

        HashMap<Character, Boolean> answer = cDCL(CDCLClauses);
        System.out.println(answer);
    }
}