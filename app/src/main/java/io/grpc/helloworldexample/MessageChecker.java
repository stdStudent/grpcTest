package io.grpc.helloworldexample;

public class MessageChecker {
    public static final String format_id = "^(\\s*\\d+\\s*)$";
    public static final String format_id_name = "\\s*(\\d+)\\s*([\\s\\S]+)\\s*";

    /**
     * Check if a string is of required format
     * @param formatToCheck Must be regex.
     */
    public static boolean isFormat(String input, String formatToCheck) {
        return input.matches(formatToCheck);
    }
}
