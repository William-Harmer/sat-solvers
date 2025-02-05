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
//        System.out.println("Total combinations: " + totalCombinations);

        // Create the hashset to store all the truth assignments if early stopping is not true
        HashSet<LinkedHashMap<Character, Boolean>> satTruthAssignments = null;
        if (!earlyStopping) {
            satTruthAssignments = new HashSet<>();
        }

        int literalIndex = 0;
        for (int i = 0; i < totalCombinations; i++) { // For all combinations there are
            for (Character literal : literalTruthAssignments.keySet()) { // For each key in the map

                // Calculate the truth value for the jth bit in i, j is literalIndex.
                // Check notes if confused on this line
                boolean truthValue = (i & (1 << literalIndex)) != 0;

                // Set the corresponding truth value in the map
                literalTruthAssignments.put(literal, truthValue);

                // Move to the next literal index
                literalIndex++;
            }
//            System.out.println("Assignment for combination " + i + ": " + literalTruthAssignments);
            literalIndex = 0;

            if (isSat(clauses, literalTruthAssignments)){ // If the truth values make it sat
//                System.out.println("Sat with assignments:" + literalTruthAssignments);
                if (earlyStopping) { // if early stopping is on, just return the linked hashmap
//                    System.out.println("Stopping now as early stopping");
                    return literalTruthAssignments;
                } else { // If early stopping is on, add it to the set
//                    System.out.println("Added to the set");
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

        String formula = "(-n) ^ (t) ^ (j v m) ^ (t) ^ (q) ^ (m) ^ (b) ^ (h) ^ (-h) ^ (-b v -l) ^ (r) ^ (-o) ^ (-x v x v -o) ^ (c v -z) ^ (-d v -k) ^ (e v -c v y v -u) ^ (o v i) ^ (-q) ^ (-g) ^ (n) ^ (-b) ^ (s v -a) ^ (b v g) ^ (h) ^ (-n v -y) ^ (-b) ^ (-i) ^ (-h) ^ (z) ^ (b) ^ (j v m) ^ (k v -d) ^ (m) ^ (-a) ^ (x v h) ^ (k v g v o) ^ (-x v t v -b) ^ (-p v -j) ^ (e) ^ (-a v -f) ^ (p) ^ (x v -b) ^ (m) ^ (x) ^ (l v -u v l) ^ (t) ^ (n) ^ (-a) ^ (g v -d) ^ (-w v g) ^ (z v b) ^ (-j) ^ (l v h v g) ^ (t) ^ (d) ^ (h v l) ^ (-i v -r v d) ^ (j v -f) ^ (r v -t) ^ (p v p v d) ^ (l v q) ^ (-g v -a) ^ (a v -d) ^ (w v q v -r v y v g v -s v s v w) ^ (u v o) ^ (-q) ^ (-r v -q v -u) ^ (-d) ^ (g v d) ^ (-q) ^ (j v i v m) ^ (-h v -x) ^ (w) ^ (e) ^ (-t) ^ (-a v d v h v -y v s) ^ (q v -s v o) ^ (-f) ^ (m v -q) ^ (-a v u v l v m) ^ (-o v g v i) ^ (j) ^ (k) ^ (-g) ^ (m) ^ (-d) ^ (e) ^ (p) ^ (-c v g) ^ (-i v m) ^ (x) ^ (-q) ^ (-y) ^ (d) ^ (x v o) ^ (r) ^ (-x v k) ^ (g) ^ (k) ^ (-x v -a v e) ^ (b) ^ (z v u v l v u) ^ (q v o v i) ^ (h v g v -a) ^ (l) ^ (n v -t) ^ (j) ^ (e v b) ^ (f v -l v f) ^ (i) ^ (-a) ^ (u v k) ^ (t v y v -n v g v t v t v y v p v -n v -d v m v j v -x) ^ (i) ^ (n) ^ (-o) ^ (-r v g) ^ (-n) ^ (-i v n) ^ (u) ^ (-b) ^ (a v -l) ^ (i v e) ^ (m) ^ (-x) ^ (q v -t) ^ (-n) ^ (w v -m) ^ (-l) ^ (w v u) ^ (k) ^ (h) ^ (-g v -n) ^ (-b v d v z) ^ (q) ^ (m) ^ (-m v t v e) ^ (r v -x) ^ (o) ^ (-n) ^ (q) ^ (c) ^ (b) ^ (z) ^ (-u) ^ (-s) ^ (j v x) ^ (l) ^ (e) ^ (-f v -k) ^ (u) ^ (i v o) ^ (-f) ^ (-u v -o) ^ (c v h) ^ (-g v t v e v y v g) ^ (g) ^ (-h) ^ (-d) ^ (x) ^ (j) ^ (n v p) ^ (u) ^ (t v y) ^ (-f v -c v c) ^ (g v n v s v -t) ^ (k) ^ (z) ^ (-p v s) ^ (-x) ^ (q v -e) ^ (d) ^ (y) ^ (s) ^ (-w v l v p) ^ (-a) ^ (e v g) ^ (-g) ^ (c) ^ (j) ^ (q) ^ (b) ^ (p) ^ (a) ^ (u) ^ (-e) ^ (-x) ^ (-h) ^ (-f) ^ (u) ^ (e v p v n v -x v -b v l) ^ (m) ^ (l) ^ (y v l v m v h) ^ (f) ^ (-j v y) ^ (f) ^ (-z) ^ (d v l v -k) ^ (-i) ^ (-z v -i v -r) ^ (g) ^ (-e v t) ^ (-w) ^ (-r v s) ^ (-w v -p v t v -y v -u) ^ (d) ^ (f) ^ (-e v i) ^ (-f) ^ (-a) ^ (-y) ^ (i v -p) ^ (n) ^ (s) ^ (-y) ^ (h) ^ (-w v c v e) ^ (n) ^ (q) ^ (n) ^ (f) ^ (-e v -c v w v j v e) ^ (-a v y v z) ^ (g v e) ^ (-g) ^ (g) ^ (y v -l) ^ (z) ^ (-k v p) ^ (-d) ^ (i v -p) ^ (s) ^ (-c v b)";
        // System.out.println("Formula: " + formula);
        ArrayList<ArrayList<Character>> clauses = Utility.formulaTo2DArrayList(formula);
        // System.out.println("The 2D arraylist: " + clauses);

        // Brute force with early stopping

        // Start timer
        long startTime = System.nanoTime();

        // Get initial memory usage
        Runtime runtime = Runtime.getRuntime();
        runtime.gc(); // Suggest to garbage collect
        long startMemory = runtime.totalMemory() - runtime.freeMemory();
        // Execute your method
        LinkedHashMap<Character, Boolean> satAssignment = bruteForceEarlyStopping(clauses);
        // Stop timer
        long endTime = System.nanoTime();
        // Get final memory usage
        long endMemory = runtime.totalMemory() - runtime.freeMemory();
        // Calculate time in seconds
        double durationInSeconds = (endTime - startTime) / 1_000_000_000.0;
        // Calculate memory used in MB
        double memoryUsedMB = (endMemory - startMemory) / (1024.0 * 1024.0);
        // Print results
        System.out.println("Execution time: " + durationInSeconds + " seconds");
        System.out.println("Memory used: " + memoryUsedMB + " MB");



        System.out.println();
        System.out.println("Brute force with early stopping:");
        System.out.println(satAssignment);
        System.out.println();

        // Brute force without early stopping
        HashSet<LinkedHashMap<Character, Boolean>> allSatAssignments = bruteForce(clauses);
        System.out.println();
        System.out.println("Brute force sat:");
        System.out.println(allSatAssignments);
        System.out.println();
    }
}
