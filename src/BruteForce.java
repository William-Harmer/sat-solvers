import java.io.*;
import java.util.*;

public class BruteForce {
    private static Map<Character, Boolean> literalValues; // A hashmap that stores the literal and then the potential values it could be, true or false. Top level as I like keeping the state of the hashmap across multiple methods if needed.

    public static void main(String[] args) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader("input.txt")); // Open file for reading
        String line;

        while ((line = reader.readLine()) != null) { // Read each line of the file
            line = line.trim(); // Remove any whitespace there is from that line (Which is being stored in the string 'line')
            if (line.isEmpty() || line.startsWith("#")) { // If the line is empty or is a comment
                continue; // Break that iteration of the loop and go onto the next line
            }

            System.out.println("Formula: " + line);
            long startTime = System.nanoTime(); // Start timer
            solve(line); // Solve that line
            long endTime = System.nanoTime(); // End timer
            long duration = (endTime - startTime) / 1_000; // Calculate the time taken in microseconds
            System.out.println("Time taken: " + duration + " µs\n");
        }
        reader.close(); // Close file reader after going through all lines
    }

    // Solve a boolean formula given as a string
    private static void solve(String formula) {
        literalValues = new HashMap<>(); // Reinitialize the map (This is so the map resets every time the function is called)

        List<Character> literals = extractLiterals(formula); // Call method that extracts the literals (a-u) from the formula and returns them in a list

        int numLiterals = literals.size(); // Get the number of literals in the formula

        // A binary shift
        // E.g. If numLiterals = 3, then 1 << 3 results in 8, which is 1000 in binary.
        // Integer literal 1 is implicitly treated as a binary number when you apply a bitwise operation
        int totalCombinations = 1 << numLiterals; // Calculate the total number of truth value combinations (2^numLiterals)

        boolean satisfiable = false; // A flag to track if the formula is satisfiable

        for (int i = 0; i < totalCombinations; i++) { // Loop through every single combination

            for (int j = 0; j < numLiterals; j++) { // For each literal
                literalValues.put(literals.get(j), (i & (1 << j)) != 0); // In obsidian notes
            }

            // Now use the assigned values we have in the hash map to work out the formula

            try {
                if (evaluateFormula(formula)) { // If called function to evaluate the formula returns true
                    satisfiable = true; // State true so it skips the next if statement
                    System.out.println("Satisfiable with: " + literalValues);
                }
            } catch (Exception e) {
                System.out.println("Error evaluating formula: " + e.getMessage());
            }
        }

        if (!satisfiable) { // If it is not satisfiable
            System.out.println("Formula is unsatisfiable.");
        }
    }

    // Extract all the literals (a-u) from a string
    private static List<Character> extractLiterals(String formula) {
        Set<Character> literalsSet = new HashSet<>(); // Use a Set to ensure each literal appears only once

        for (char ch : formula.toCharArray()) { // Iterate through each character in the formula
            if (ch >= 'a' && ch <= 'u') { // If the character is a literal (a-u)
                literalsSet.add(ch); // Add it to the set
            }
        }

        return new ArrayList<>(literalsSet); // Convert the set to a list and return it, as we need order
    }

    // Given the hashmap will now have true false values assigned, evaluate what the formula output will be
    private static boolean evaluateFormula(String formula) throws Exception {
        Stack<Boolean> values = new Stack<>(); // Stack to hold boolean values of literals
        Stack<Character> operators = new Stack<>(); // Stack to hold operators '^', 'v', or '('

        for (int i = 0; i < formula.length(); i++) { // Loop through each character in the formula
            char ch = formula.charAt(i); // Get the character at i position
            if (ch == ' ') continue; // Skip whitespace (Precaution)


            if (ch >= 'a' && ch <= 'u') { // If the character is a literal
                values.push(literalValues.get(ch)); // Push the literals value (True / false) onto the stack
            }
            else if (ch == '-') { // If the character is a negation operator ('-'), negate the next literal
                i++; // Move to the next character, which will be a literal
                char nextLiteral = formula.charAt(i); // Store the next literal in a variable
                values.push(!literalValues.get(nextLiteral)); // Push onto the stack the literal value but notted
            }

            else if (ch == '(') { // If the character is an opening parenthesis
                operators.push(ch); // Push it onto the operator stack
            }

            else if (ch == '^' || ch == 'v') { // If the character is the operator AND or OR

                while (!operators.isEmpty() && precedence(operators.peek()) >= precedence(ch)) { // While the operators stack is not empty and the precedence of the operator currently at the top of the stack is greater than or equal to the precedence of the operator currently being looked at
                    applyOperator(values, operators.pop()); // Apply top stack operator (Not the one being looked at) to the top two values on the stack and then push value back onto the operator stack
                }
                operators.push(ch); // Now that your operator has higher precedence than the one on the top of the stock (Or the stack is empty), push being looked at operator onto the stack
            }

            else if (ch == ')') { // If the character is a closing parenthesis
                // Apply operators inside the parentheses
                while (!operators.isEmpty() && operators.peek() != '(') { // While the operator stack is not empty and the operator at the top of the stack is not '('
                    applyOperator(values, operators.pop()); // Apply top stack operator to the top two values on the stack and then push value back onto the operator stack
                }
                operators.pop(); // Remove the opening parenthesis
            }

            else {
                throw new Exception("Invalid character in formula: " + ch);
            }
        }

        // Apply any remaining operators after the formula has been parsed
        while (!operators.isEmpty()) {
            applyOperator(values, operators.pop());
        }

        // The final result of the evaluation is the last value on the stack
        return values.pop();
    }


    // Apply operator to the top two values on the stack and then push value back onto stack
    private static void applyOperator(Stack<Boolean> values, char operator) {
        boolean b = values.pop(); // Pop the top two boolean values from the stack
        boolean a = values.pop();

        switch (operator) { // Apply the appropriate operator
            case '^': // AND operator
                values.push(a && b); // Push that value back onto the value stack
                break;
            case 'v': // OR operator
                values.push(a || b); // Push that value back onto the value stack
                break;
        }
    }

    private static int precedence(char operator) { // Determine precedence of operators, higher value = higher precedence
        if (operator == '^') return 2; // AND ('^') has higher precedence than OR ('v')
        if (operator == 'v') return 1;
        return 0;
    }
}
