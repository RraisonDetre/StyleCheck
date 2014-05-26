package assets.general;

import assets.gui.SCGUI;

import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

/**
 * Class to assist with various formatting tasks
 * for outputting data to users. Static methods only.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public final class SCStringFormat {

    // For fail values
    public static final String NO_STRING = "[NONE FOUND]";

    /**
     * Get the position adjustment for a replacement
     * @param original - the original word or phrase
     * @param replacement - the replacement word or phrase
     * @return the length difference from original to replacement
     */
    public static int getPositionAdjustment(String original, String replacement) {
        return original.trim().length() - replacement.trim().length();
    }

    /**
     * Return true if the word is the first in a sentence
     * @param text - the text to search
     * @param position - the word start position
     * @return true if the word is the first in a sentence
     */
    public static boolean isFirstWordInSentence(CharSequence text, int position) {
        if (position < 0 || position >= text.length()) {
            throw new IllegalArgumentException("Position out of range!");
        }
        if (position == 0) { return true; }
        int p = position - 1;
        while (p >= 0 && !Character.isLetterOrDigit(text.charAt(p))) {
            if (text.charAt(p) == '.') { return true; }
            p--;
        }
        return p < 0;
    }

    /**
     * Return a list of capitalized words
     * @param words - the words to capitalize
     * @return the list of capitalized words
     */
    public static LinkedList<String> capitalizeWords(Collection<String> words) {
        return words.stream().map(word -> word.toUpperCase().charAt(0)
                + word.substring(1)).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the individual words as strings
     * @param text - the text to search
     * @return the list of words
     */
    public static LinkedList<String> getAllWords(String text, int skip) {
        LinkedList<ScoredWord> wordList = getAllWordsAndPositions(text);
        if (skip + 1 >= wordList.size()) { return new LinkedList<>(); }
        return wordList.subList(skip + 1, wordList.size()).stream().map(wp -> wp.word)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Utility method to get all of the individual words from a text
     * along with their start positions
     * @param text - the text to search
     * @return the list of words
     */
    public static LinkedList<ScoredWord> getAllWordsAndPositions(String text) {
        LinkedList<ScoredWord> wordList = new LinkedList<>();

        // Get start position of first word
        int pos = getNextWordStart(text, -1);

        if (SCGUI.DEBUG_FINE) {
            System.out.println("Starting word position: " + pos);
        }

        // Walk through and add the words to the list
        int currentEnd;
        while (pos < text.length()) {
            currentEnd = getEndOfCurrentWord(text, pos);
            if (currentEnd < 0) { break; }
            wordList.add(new ScoredWord(text.substring(pos, currentEnd), pos));
            pos = getNextWordStart(text, currentEnd);
        }

        if (SCGUI.DEBUG_FINE) {
            System.out.println("Number of words in document: " + wordList.size());
            System.out.print("Words: ");
            for (ScoredWord word : wordList) {
                System.out.print("\"" + word.word + "\":" + word.score + ", ");
            }
            System.out.print("\n");
        }

        return wordList;
    }

    /**
     * Find the start position of the next word
     * @param text - the text to search
     * @param position - the current position
     * @return the start position of the next word
     */
    public static int getNextWordStart(CharSequence text, int position) {
        if (position < -1 || position + 1 >= text.length()) {
            throw new IllegalArgumentException("[SC] Position out of range in text!");
        }

        // Walk until word char found
        int startPos = position + 1;
        while (startPos < text.length()
                && !Character.isLetterOrDigit(text.charAt(startPos))
                && text.charAt(startPos) != '\''
                && text.charAt(startPos) != '-') {
            startPos++;
        }
        return startPos;
    }

    /**
     * Get the end position (exclusive) of the current word
     * @param text - the text to search
     * @param position - the current position
     * @return the end position of the word
     */
    public static int getEndOfCurrentWord(CharSequence text, int position) {
        if (position < -1 || position + 1 >= text.length()) {
            return -1;
        }

        // Walk through text until non-word char found (space, punctuation, etc.)
        int endPos = position + 1;
        while (endPos < text.length()
                && (Character.isLetterOrDigit(text.charAt(endPos))
                || text.charAt(endPos) == '\'')
                && text.charAt(endPos) != '?'
                && text.charAt(endPos) != '!'
                && text.charAt(endPos) != '"'
                && text.charAt(endPos) != '.'
                && text.charAt(endPos) != ','
                && text.charAt(endPos) != ';') {
            endPos++;
        }
        return endPos;
    }

    /**
     * Return true if a string is entirely numeric
     * @param str - the string to check
     * @param punctuation - if true, allow decimal points/commas etc.
     * @return true if the string is numeric and of a valid format
     */
    public static boolean isNumericalString(String str, boolean punctuation) {
        for (char c : str.toCharArray()) {
            if (!Character.isDigit(c)) {
                if (!punctuation
                        || !((c == '.' && countOccurrences(str, '.', false) == 1)
                        || c == ',')) {
                        return false;
                }
            }
        }
        return true;
    }

    /**
     * Count the occurrences of a character in a string
     * @param str - the string
     * @param c - the character
     * @param caseSensitive - if true, a letter match will
     *                      only count if they are the same case
     * @return the number of occurrences
     */
    public static int countOccurrences(String str, char c,
                                       boolean caseSensitive) {
        int occ = 0;
        for (char d : str.toCharArray()) {
            if (c == d || (!caseSensitive && Character.isLetter(c)
                    && Character.toLowerCase(c) == Character.toLowerCase(d))) {
                occ++;
            }
        }
        return occ;
    }

    /**
     * Utility function to create a grammatically correct "or" string
     * of the list of strings (using Oxford commas).
     * @param stringList - the list
     * @return the "or" string
     */
    public static String getOrString(LinkedList<String> stringList) {
        if (stringList == null || stringList.isEmpty()) {
            return NO_STRING;
        } else if (stringList.size() == 1) {
            return stringList.getFirst();
        } else if (stringList.size() == 2) {
            return stringList.get(0) + " or " + stringList.get(1);
        } else {
            String oxford = "";
            String last = stringList.pollLast();
            for (String word : stringList) {
                oxford += word + ", ";
            }
            oxford += "or " + last;
            return oxford;
        }
    }

    /**
     * Utility function to generate a quoted version of a given String
     * @param input - the String to quote
     * @return the quoted String
     */
    public static String getQuotedString(String input) {
        return "\"" + input + "\"";
    }

    /**
     * Reverse a string and return it
     * @param str - string input
     * @return the reversed string
     */
    public static String reverseString(CharSequence str) {
        String rev = "";
        for (int i = str.length() - 1; i >= 0; i--) {
            rev += str.charAt(i);
        }
        return rev;
    }

    /**
     * Format a number using proper grouping etc. Number is assumed
     * to be only digits and up to one decimal point.
     * @param numberString - the String of the number
     * @return the cleaned number String
     */
    public static String formatNumberString(String numberString) {
        if (numberString.equals(getOnlyDigits(numberString))) { return ""; }
        if (!isNumericalString(numberString, false)
                && countOccurrences(numberString, '.', false) != 1) {
            if (SCGUI.DEBUG) { System.out.println("INCORRECT NUMBER FORMAT: " + numberString); }
            return getOnlyDigits(numberString);
        }
        String formatted;

        // Deal with decimal point
        if (numberString.contains(".")
                && hasUselessDecimalPointOrPeriod(numberString)) {
            formatted = numberString.substring(0, numberString.indexOf('.'));
        } else {
            formatted = numberString;
        }

        // Group digits and return
        return groupDigits(formatted);
    }

    /**
     * A last-resort method to get only digits
     * @param numberString - the string
     * @return the string without any non-digit characters
     */
    public static String getOnlyDigits(String numberString) {
        String digits = "";
        for (char c : numberString.toCharArray()) {
            if (Character.isDigit(c)) {
                digits += c;
            }
        }
        if (digits.isEmpty()) { return NO_STRING; }
        return digits;
    }

    /**
     * Group the digits with a group separator. Input String is
     * assumed to have only digits and up to 1 decimal point.
     * @param numberString - the number string
     * @return the grouped number string
     */
    public static String groupDigits(String numberString) {
        String left;
        String right = "";
        String grouped = "";

        // If this has a decimal point, only group before it
        if (numberString.contains(".")) {
            left = numberString.substring(0, numberString.indexOf('.'));
            right = numberString.substring(numberString.indexOf('.'));
        } else {
            left = numberString;
        }

        // Group digits starting from rightmost (last in array)
        byte groupCount = 0;
        for (int i = left.length() - 1; i >= 0; i--) {
            if (groupCount == 3) {
                groupCount = 0;
                grouped += ",";
            }
            grouped += left.charAt(i);
            groupCount++;
        }

        // The left portion is reversed
        return reverseString(grouped) + right;
    }

    /**
     * Very niche helper method for number formatting. String is assumed
     * to contain only digits and a single decimal point
     * @param numberString - the String of the number
     * @return true if the number String's decimal point is useless
     */
    private static boolean hasUselessDecimalPointOrPeriod(String numberString) {

        // If the decimal point is first or last it is useless
        int pointPos = numberString.indexOf('.');
        if (pointPos == 0 || pointPos == numberString.length() - 1) {
            return true;
        }

        // If only zeroes follow the decimal point it is useless
        boolean zeroes = true;
        for (int i = pointPos + 1; i < numberString.length(); i++) {
            if (numberString.charAt(i) != '0') { zeroes = false; }
        }
        return zeroes;
    }

    /**
     * Print a LinkedList of words as a contiguous phrase
     * @param wordList - the list of words
     */
    public static String wordListAsPhrase(Iterable<String> wordList) {
        String printMe = "";
        for (String word : wordList) {
            printMe += (word + " ");
        }
        return "\"" + printMe.trim() + "\"";
    }

    /**
     * Return true if the string contains letters or digits
     * @param str - the string
     * @return true if the string contains letters or digits
     */
    public static boolean containsLettersOrDigits(String str) {
        for (char c : str.toCharArray()) {
            if (Character.isLetterOrDigit(c)) { return true; }
        }
        return false;
    }

    /**
     * Return true if the string contains non-letters at other
     * positions than the first or last.
     * @param str - the string
     * @return true if the above condition is met
     */
    public static boolean containsNonLettersInside(String str) {
        if (str.isEmpty()) { return false; }
        if (str.length() == 1) {
            return !Character.isLetterOrDigit(str.charAt(0));
        }

        int start = 1;
        while (start < str.length() - 1) {
            if (!Character.isLetterOrDigit(str.charAt(start))
                    && str.charAt(start) != '-'
                    && str.charAt(start) != '\'') {
                return true;
            }
            start++;
        }
        return false;
    }

    /**
     * Check whether a given word can possibly be a legitimate by format.
     * @param word - the word to check
     * @return true if the word's format is valid
     */
    public static boolean isPossibleWord(String word) {
        return !word.isEmpty() && !(word.length() == 1
                && !Character.isLetterOrDigit(word.charAt(0)))
                && containsLettersOrDigits(word)
                && !containsNonLettersInside(word);
    }

}
