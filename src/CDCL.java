import java.util.ArrayList;

public class CDCL {

    // If unit propogation does nothing the decision level should not change
    // The decision level increases when the solver makes a new decision by assigning a literal without unit propagation forcing it.

    private static boolean cDCL(ArrayList<CDCLClause> clauses){
        // Unit propagate once without making any decision
        int decisionLevel = 0;
        cDCLUnitProp(clauses, decisionLevel);

        while (true){
            // or if all clauses are unit clauses then return sat
            boolean allUnitClauses = true;

            for (CDCLClause clause : clauses) {
                if (clause.clause.isEmpty()) {
                    if (decisionLevel == 0) { // If there is an empty clause at decision level 0 return unsat
                        return false;
                    } else { // If empty clause but not at level 0
                        // Find the UIP and the UIP cut from the UIP and from that the learned clause
                        // Move back one decision level and add the learned clause
                        // remember to break out of the for loop
                    }
                }

                if (clause.clause.size() != 1) {
                    allUnitClauses = false;
                }
            }

            if (allUnitClauses) { // If all clauses are unit clauses
                return true;
            }

            // Add element that is not a unit clause, increase decision level and unit prop
        }
    }

    private static void findLearnedClause(ArrayList<CDCLClause> clauses){
        // Find the first UIP


        // From that find the cut


        // From that find the learned clause
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
    }

    public static void main(String[] args) {
        String formula = "(AB)(Ac)(CD)(bde)(EfG)(bgh)(HI)(Hj)(iJk)(JL)(Kl)(a)";
        ArrayList<CDCLClause> cDCLClauses = Utility.formulaToCDCLArrayList(formula);
        print(cDCLClauses);
        System.out.println();

        int decisionLevel = 1;
        cDCLUnitProp(cDCLClauses,decisionLevel);
        print(cDCLClauses);
    }
}
