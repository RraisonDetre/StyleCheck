package assets.general;

/**
 * Class to enclose constant style rules and their weights + descriptions
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class StyleRules {

    /////       RULE DESCRIPTIONS       /////

    // Formatting
    public static final String NUMBER_WORD = "Number Instead of Number Word";
    public static final String FORMATTING[] = {
        NUMBER_WORD };

    // Overused words
    public static final String OVER_ADV = "Overused Adverb";
    public static final String OVER_V = "Overused Verb";
    public static final String OVER_ADJ = "Overused Adjective";
    public static final String OVER_N = "Overused Noun";
    public static final String OVERUSED_WORDS[] = {
        OVER_ADV, OVER_V, OVER_ADJ, OVER_N };

    // Locally repeated words
    public static final String RPT_ADV = "Commonly Repeated Adverb in Surrounding Text";
    public static final String RPT_V = "Commonly Repeated Verb in Surrounding Text";
    public static final String RPT_ADJ = "Commonly Repeated Adjective in Surrounding Text";
    public static final String RPT_N = "Commonly Repeated Noun in Surrounding Text";
    public static final String REPEATED_WORDS[] = {
        RPT_ADV, RPT_V, RPT_ADJ, RPT_N };
    
    // 2D array of all rules
    public static final String ALL_RULES[][] = {
        FORMATTING, OVERUSED_WORDS, REPEATED_WORDS };

    /////       RULE WEIGHTS        /////

    /////       STATIC METHODS      /////

    /**
     * Returns true if the submitted string is a valid rule
     * @param test - the string to test
     * @return true if the submitted string is a valid rule
     */
    public static boolean isRule(String test) {
        for (String section[] : ALL_RULES) {
            if (arrayContains(section, test)) { return true;}
        }
        return false;
    }

    /**
     * Check if this style rule is one for which to generate synonyms
     * @param rule - the style rule
     * @return true if this style rule is one for which to generate synonyms
     */
    public static boolean requiresSynonyms(String rule) {
        return arrayContains(OVERUSED_WORDS, rule) || arrayContains(REPEATED_WORDS, rule);
    }

    /**
     * Helper method to check if a string array contains a string
     * @param array - the string array
     * @param text - the string
     * @return true if the string array contains the string
     */
    public static boolean arrayContains(String array[], String text) {
        for (String curr : array) {
            if (curr.equals(text)) {
                return true;
            }
        }
        return false;
    }

}
