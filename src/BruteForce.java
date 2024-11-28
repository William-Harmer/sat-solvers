import java.io.*;
import java.util.*;

public class BruteForce {
    public static HashSet<LinkedHashMap<Character, Boolean>> bruteForce(ArrayList<ArrayList<Character>> clauses) {
        return (HashSet<LinkedHashMap<Character, Boolean>>) bruteForce(clauses, false);
    }

    public static LinkedHashMap<Character, Boolean> bruteForceEarlyStopping(ArrayList<ArrayList<Character>> clauses) {
        return (LinkedHashMap<Character, Boolean>) bruteForce(clauses, true);
    }

    private static Object bruteForce(ArrayList<ArrayList<Character>> clauses, boolean earlyStopping) {
        LinkedHashMap<Character, Boolean> literalTruthAssignments = new LinkedHashMap<>();

        // Get all unique literals
        for (ArrayList<Character> clause : clauses) {
            for (Character literal : clause) {
                literalTruthAssignments.put(Character.toLowerCase(literal), false);
            }
        }

        int totalCombinations = (int) Math.pow(2, literalTruthAssignments.size());  // 2^n combinations
        System.out.println("Total combinations: " + totalCombinations);

        // Create the hashset to store all the truth assignments if early stopping is not true
        HashSet<LinkedHashMap<Character, Boolean>> satTruthAssignments = null;
        if (!earlyStopping) {
            satTruthAssignments = new HashSet<>();
        }

        int literalIndex = 0;
        for (int i = 0; i < totalCombinations; i++) { // For all combinations there are (Apart from the first one)
            for (Character literal : literalTruthAssignments.keySet()) { // For each key in the map

                // Calculate the truth value for the jth bit in i, j is literalIndex.
                // Check notes if confused on this line
                boolean truthValue = (i & (1 << literalIndex)) != 0;

                // Set the corresponding truth value in the map
                literalTruthAssignments.put(literal, truthValue);

                // Move to the next literal index
                literalIndex++;
            }
            System.out.println("Assignment for combination " + i + ": " + literalTruthAssignments);
            literalIndex = 0;

            if (isSat(clauses, literalTruthAssignments)){ // If the truth values make it sat
                System.out.println("Sat with assignments:" + literalTruthAssignments);
                if (earlyStopping) { // if early stopping is on, just return the linked hashmap
                    System.out.println("Stopping now as early stopping");
                    return literalTruthAssignments;
                } else { // If early stopping is on, add it to the set
                    System.out.println("Added to the set");
                    satTruthAssignments.add(new LinkedHashMap<>(literalTruthAssignments));
                }
            }
        }
        return satTruthAssignments;
    }

    private static boolean isSat(ArrayList<ArrayList<Character>> clauses, HashMap<Character, Boolean> literalTruthAssignments){
        boolean sat = true;
        for (ArrayList<Character> clause : clauses) {
            boolean clauseBool = false;
//            System.out.println("Clause: " + clause);
//            System.out.println("ClauseBool: " + clauseBool);
            for (Character literal : clause) {
                // Check if the literal is uppercase (indicating negation)
                boolean literalBool = literalTruthAssignments.get(Character.toLowerCase(literal));

                // If literal is uppercase, negate the value
                if (Character.isUpperCase(literal)) {
                    literalBool = !literalBool;
                }
//                System.out.println(literal + ": " + literalBool);

                // OR it with clauseBool
                clauseBool |= literalBool;
//                System.out.println("ClauseBool: " + clauseBool);
            }
            sat &= clauseBool;
        }
        return sat;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt")); // Open file for reading
        String line;

        while ((line = reader.readLine()) != null) { // Read each line of the file
            if (line.isEmpty() || line.startsWith("//")) { // If the line is empty or is a comment
                continue; // Break that iteration of the loop and go onto the next line
            }

            System.out.println("Formula: " + line);
            ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(line);
            System.out.println("The 2D arraylist: " + clauses);

            // Brute force with early stopping
//            LinkedHashMap<Character, Boolean> satAssignment = bruteForceEarlyStopping(clauses);
//            System.out.println();
//            System.out.println("Brute force with early stopping:");
//            System.out.println(satAssignment);
//            System.out.println();

            // Brute force without early stopping
            HashSet<LinkedHashMap<Character, Boolean>> allSatAssignments = bruteForce(clauses);
            System.out.println();
            System.out.println("Brute force sat:");
            System.out.println(allSatAssignments);
            System.out.println();

        }
        reader.close(); // Close file reader after going through all lines
    }
}
