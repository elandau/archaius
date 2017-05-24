package com.netflix.archaius.internal;

public class Preconditions {
    public static void checkArgument(boolean isValid, String message) {
        if (!isValid) {
            throw new IllegalArgumentException(String.valueOf(message));
        }
    }
    
    public static void checkState(boolean isValid, String message) {
        if (!isValid) {
            throw new IllegalArgumentException(String.valueOf(message));
        }
    }
}
