package com.github.williamharmer.utilities;

// Utility class for flipping the polarity of a literal represented by a char.
// Convention:
// - Uppercase letters denote positive literals (e.g., 'A').
// - Lowercase letters denote negated literals (e.g., 'a' means ¬A).
// The oppositePolarity method toggles between these two forms:
//   'A' -> 'a', 'a' -> 'A'.
// Non-letter chars will be flipped by case rules as well, but typical usage
// assumes alphabetic CNF literals.

public class OppositePolarity {
    // Returns the same letter with its case flipped:
    // - Uppercase input -> lowercase output
    // - Lowercase input -> uppercase output
    public static char oppositePolarity(char literal) {
        if (Character.isUpperCase(literal)) {
            return Character.toLowerCase(literal);
        } else {
            return Character.toUpperCase(literal);
        }
    }
}