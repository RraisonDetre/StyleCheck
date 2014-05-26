package assets.gui;

/**
 * Simple wrapper class to encapsulate a highlighted
 * word in the GUI using offsets
 */
class SCHighlightedWord {

    private final String word;
    private final int position;
    private final String styleRule;

    /**
     * Main constructor called by other variants
     * @param w - the word
     * @param pos - the position
     * @param style - the style error code, or NO_ERROR
     */
    SCHighlightedWord(String w, int pos, String style) {
        word = w;
        position = pos;
        styleRule = style;
    }

    /**
     * Simple getter for word
     * @return the word
     */
    public String getWord() { return word; }

    /**
     * Get the start position of the word in the document
     * @return the start position
     */
    public int getStartPosition() { return position; }

    /**
     * Getter for style rule
     * @return the style rule
     */
    public String getStyleRule() { return styleRule; }

}
