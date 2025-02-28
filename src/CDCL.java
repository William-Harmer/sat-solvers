import java.util.ArrayList;
import java.util.HashMap;

public class CDCL {

    private static HashMap<Character, Boolean> cDCL(ArrayList<CDCLClause> clauses){
        int decisionLevel = 0;
        while (true){ // Do this until something has returned

            decisionLevel++;
            // Unit prop


            // If there is conflict, and it is decision level 0 then the whole formula is not sat
                // Otherwise if the conflict is not at decision level 0
                // Find the UIP and the UIP cut from the UIP and from that the learned clause
                // Move back one decision level and add the learned clause
                //
            // If all clauses are unit clauses, the formula is SAT
            // Otherwise add element that is not a unit clause
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
