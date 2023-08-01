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

    public static String getRelativeFailedMsg(String rel) {
        switch (rel) {
            case format_id:
                return "Failed: Message must be in format \"id\".";

            case format_id_name:
                return "Failed: Message must be in format \"id name\".";

            default:
                return "Failed: Unknown format.";
        }
    }
}
