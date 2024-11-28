import java.io.*;
import java.util.*;

public class UPAndBF {
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
            UnitPropagation.unitPropagation(clauses);
            System.out.println("The 2D arraylist after unit propagation: " + clauses);
            LinkedHashMap<Character, Boolean> satAssignment = BruteForce.bruteForceEarlyStopping(clauses);
            System.out.println();
            System.out.println("Brute force with early stopping:");
            System.out.println(satAssignment);
            System.out.println();

        }
    }
}

