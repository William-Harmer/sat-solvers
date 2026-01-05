package com.github.williamharmer.utilities;

public class OppositePolarity {
    public static char oppositePolarity(char literal) {
        if (Character.isUpperCase(literal)) {
            return Character.toLowerCase(literal);
        } else {
            return Character.toUpperCase(literal);
        }
    }
}
