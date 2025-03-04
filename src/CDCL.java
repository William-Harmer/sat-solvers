import java.util.ArrayList;
import java.util.HashSet;
import java.util.TreeSet;

public class CDCL {

    // If unit propogation does nothing the decision level should not change
    // The decision level increases when the solver makes a new decision by assigning a literal without unit propagation forcing it.
    // Remember the learned clause is the opposite polarity of the clause

    // I haven't set that my decision node is a decision in the data structure, not sure if this will affect things, it shouldn't case it all links back I think.


    private static boolean cDCL(ArrayList<CDCLClause> clauses){
        // Unit propagate once without making any decision
        int decisionLevel = 0;
        cDCLUnitProp(clauses, decisionLevel);

        while (true){
            // or if all clauses are unit clauses then return sat
            boolean allUnitClauses = true;

            for (CDCLClause clause : clauses) {
                if (clause.clause.isEmpty()) { // If we have an empty clause
                    if (decisionLevel == 0) { // At level 0
                        return false;
                    } else { // or if it is above level 0

                        // If empty clause but not at level 0
                        // Find the learned clause
                        // Go back to the level of the learned clause and get that levels formula
                        //

                        // remember to break out of the for loop
                    }
                }
                if (clause.clause.size() != 1) {
                    allUnitClauses = false;
                }
            }

            if (allUnitClauses) { // If all clauses are unit clauses
                return true; // SAT
            }

            // Add element that is not a unit clause, increase decision level and unit prop
            decisionLevel++;

        }
    }

    private static Character findFirstUIP(ArrayList<CDCLClause> clauses, CDCLClause emptyClause, int decisionLevel){
        // Will this go all the way back to the decision or will it give me an error in that case?

        // We have the empty clause already
        // put all the characters from emptyClause.trailElements into the tree set
        // Add each literal from trailElements into the TreeSet
        TreeSet<Character> set = new TreeSet<>(emptyClause.trailElements);

        while (set.size() > 1){
            for (CDCLClause clause : clauses) {
                if (clause.clause.size() == 1 && clause.clause.contains(Character.toLowerCase(set.last())) || clause.clause.contains(Character.toUpperCase(set.last())) && clause.level == decisionLevel) { // Check if the clause is a unit clause, contains the last element, and has the same decision level
                    set.remove(set.last());
                    set.addAll(clause.trailElements); // Add all the letters in trailElements to the set
                    break;
                }
            }
        }
        return set.last();
    }

    private static CDCLClause findLearnedClause(ArrayList<CDCLClause> clauses, CDCLClause emptyClause, int decisionLevel) {
        // Find the first UIP (Unique Implication Point)
        Character firstUIP = findFirstUIP(clauses, emptyClause, decisionLevel);
//        System.out.println("First UIP: " + firstUIP);

        // Find the earliest decision node (lowest decision level > 0)
        CDCLClause startNode = null;
        for (CDCLClause clause : clauses) {
            if (clause.clause.size() == 1 && clause.trailElements.isEmpty() && clause.level > 0) {
                // Set startNode to the clause with the smallest decision level
                if (startNode == null || clause.level < startNode.level) {
                    startNode = clause;
                }
            }
        }

//        System.out.println("Earliest decision node:");
//        startNode.print();
//        System.out.println();

        // Use TreeSet to maintain an ordered set of literals
        TreeSet<Character> set = new TreeSet<>();

        // Use HashSet to store learned clause literals while ensuring uniqueness
        HashSet<Character> learnedSet = new HashSet<>();

        // Ensure startNode is not null before proceeding
        assert startNode != null;

        // Add the first literal of the startNode to the set
        set.add(startNode.clause.getFirst());

        // Process until all elements in the set are greater than the UIP
        while (Character.toLowerCase(set.first()) <= Character.toLowerCase(firstUIP)) {
            char smallest = set.first(); // Get the smallest element (ignoring case)
//            System.out.println("Smallest element: " + smallest);

            // Iterate through all clauses
            for (CDCLClause clause : clauses) {
                // Check if the clause is a unit clause and contains the smallest element
                if (clause.clause.size() == 1 &&
                        (clause.trailElements.contains(Character.toLowerCase(smallest)) ||
                                clause.trailElements.contains(Character.toUpperCase(smallest)))) {

                    char unitLiteral = clause.clause.getFirst(); // Get the only literal in the unit clause

                    // If the unit literal is greater than the UIP (ignoring case)
                    if (Character.toLowerCase(unitLiteral) > Character.toLowerCase(firstUIP)) {
                        learnedSet.add(Utility.oppositePolarity(smallest)); // Add opposite polarity to HashSet (no duplicates)
                    }

                    // Add unitLiteral to the processing set
                    set.add(unitLiteral);
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
        for (Character literal : learnedClause) {
            for (CDCLClause clause : clauses) {
                // Check if the clause contains the literal (or its opposite polarity)
                if (clause.clause.size() == 1 &&
                        (clause.clause.contains(literal) || clause.clause.contains(Utility.oppositePolarity(literal)))) {

                    // Update the level to the lowest encountered level
                    if (level == -1 || clause.level < level) {
                        level = clause.level;
                    }
                }
            }
        }

        // Return the new learned clause with its computed decision level
        return new CDCLClause(learnedClause, new ArrayList<>(), level-1);
    }



    private static void addFirstElementNotAUnitClauseAsNewClauseToFormula(
            ArrayList<CDCLClause> clauses, int decisionLevel, boolean useOppositePolarity) {

        for (CDCLClause cdclClause : clauses) {
            // Check if the clause is not a unit clause
            if (cdclClause.clause.size() > 1) {
                char firstLiteral = cdclClause.clause.getFirst(); // Get the first literal

                // If useOppositePolarity is true, apply the opposite polarity to the first literal
                if (useOppositePolarity) {
                    firstLiteral = Utility.oppositePolarity(firstLiteral);
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
                        boolean containsOpposite = clauseToCheck.clause.contains(Utility.oppositePolarity(unitClause));

                        if (containsUnit) { // Clause contains the same polarity unit clause
                            clauses.remove(i); // Remove the clause altogether
                            i--; // Adjust index to account for removal
                            j--; // Adjust to revisit the current clause
                            formulaModified = true;

                        } else if (containsOpposite) { // Clause contains the opposite polarity unit clause
                            while (clauseToCheck.clause.contains(Utility.oppositePolarity(unitClause))) {
                                // Move opposite unit to trailElements
                                clauseToCheck.trailElements.add(Utility.oppositePolarity(unitClause));
                                clauseToCheck.clause.remove((Character) Utility.oppositePolarity(unitClause));
                            }
                            clauseToCheck.level = decisionLevel; // Set decision level for the clause
                            formulaModified = true;
                        }

                        // If a modification has occurred, restart processing the unit clauses
                        if (formulaModified) {
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void print(ArrayList<CDCLClause> clauses) {
        for (CDCLClause clause : clauses) {
            clause.print();
            System.out.print(" ");
        }
        System.out.println();
    }

    public static void main(String[] args) {
        String formula = "(AB)(Ac)(CD)(bde)(EfG)(bgh)(HI)(Hj)(iJk)(JL)(Kl)";
        ArrayList<CDCLClause> cDCLClauses = Utility.formulaToCDCLArrayList(formula);
        print(cDCLClauses);

        int decisionLevel = 0;
        cDCLUnitProp(cDCLClauses,decisionLevel);
        print(cDCLClauses);

        decisionLevel = 1;
        addFirstElementNotAUnitClauseAsNewClauseToFormula(cDCLClauses,decisionLevel,true);
        print(cDCLClauses);
        cDCLUnitProp(cDCLClauses,decisionLevel);
        print(cDCLClauses);

        decisionLevel = 2;
        addFirstElementNotAUnitClauseAsNewClauseToFormula(cDCLClauses,decisionLevel,true);
        print(cDCLClauses);
        cDCLUnitProp(cDCLClauses,decisionLevel);
        print(cDCLClauses);

        // We have error so
        for (CDCLClause clause : cDCLClauses) {
            if (clause.clause.isEmpty()) {
                 CDCLClause test = findLearnedClause(cDCLClauses, clause, decisionLevel);
                 test.print();
                break;
            }
        }
    }
}
