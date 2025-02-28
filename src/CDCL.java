import java.util.ArrayList;
import java.util.HashMap;

public class CDCL {

//    private static HashMap<Character, Boolean> cDCL(ArrayList<CDCLClause> clauses){
//
//        // Create the trail, the trail starts the same size as the 2D arraylist but the clauses are empty
//        // Also all the node levels start at 0
//
//
//        // First of all unitprop once
//        // Check that it is not unsat just straight off the bat
//
//        // If all clauses are unit clauses, the formula is SAT
//
//        // Otherwise add element that is not a unit clause
//
//    }

    private static void cDCLUnitProp(ArrayList<CDCLClause> clauses) {

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
    }
}
