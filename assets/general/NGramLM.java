package assets.general;

import assets.gui.SCGUI;
import edu.berkeley.nlp.lm.ArrayEncodedProbBackoffLm;
import edu.berkeley.nlp.lm.NgramLanguageModel;
import edu.berkeley.nlp.lm.io.LmReaders;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to encapsulate n-gram functionality using
 * the Berkeley LM
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class NGramLM {

    // N-Gram size parameters
    public static final int MAX_N_GRAM_SIZE = 3;
    public static final int MIN_N_GRAM_SIZE = 1;

    // Fraction of a word that constitutes the "base"
    public static final double BASE_PREFIX = 0.5;

    // The precomputed binary language model file path
    private static final String BINARY_FILE
            = "src/files/ngrams/google/google_ngrams.binary";

    // The BerkeleyLM model
    private ArrayEncodedProbBackoffLm<String> model;

    /**
     * Create a new NGramLM
     */
    public NGramLM() {
        loadModel();
    }

    /**
     * Load the n-gram model
     */
    @SuppressWarnings("CastToConcreteClass")
    private void loadModel() {
        NgramLanguageModel<String> ngramLanguageModel
                = LmReaders.readLmBinary(BINARY_FILE);
        model = (ArrayEncodedProbBackoffLm<String>) ngramLanguageModel;
    }

    /**
     * Essential top level method for word suggestions
     * @param window - the "window" in the sentence
     * @param errorIndex - the position of the error
     * @param replacements - the list of replacements to sort
     * @return the scored list of replacements
     */
    public LinkedList<ScoredWord> scorePossibleReplacements(
            LinkedList<String> window, int errorIndex,
            Collection<String> replacements) {

        if (errorIndex == -1) { return new LinkedList<>(); }
        String errorWord = window.get(errorIndex);

        // Sort by n-gram probability
        LinkedList<ScoredWord> evaluate = replacements.stream().map(
                replacement -> new ScoredWord(replacement,
                        getReplacementProbability(window, errorIndex, replacement)))
                .collect(Collectors.toCollection(LinkedList::new));
        sortSameBaseWordsByTense(evaluate, errorWord);

        // For debugging. Print the list and the scores
        if (SCGUI.DEBUG_FINE) {
            for (ScoredWord currWord : evaluate) {
                System.out.println("Phrase: " + SCStringFormat.wordListAsPhrase(window)
                        + " Error: " + errorWord + " Replacement: "
                        + currWord.word + " Score: " + currWord.score);
            }
        }

        return evaluate;
    }

    /**
     * Take the list of possible replacement words and evaluate the tense.
     * In simplest terms that means taking any words with a common base word
     * and setting the one with the same suffix as the error as the best scoring.
     * @param replacements - the list of possible replacements
     * @param error - the error word
     */
    private void sortSameBaseWordsByTense(Collection<ScoredWord> replacements,
                                         String error) {
        for (ScoredWord replacement1 : replacements) {
            replacements.stream().filter(replacement2 -> !replacement1.equals(replacement2)
                    && haveSameBase(replacement1, replacement2)).forEach(replacement2
                    -> sortSameBaseWordsByTense(replacement1, replacement2, error));
        }
    }

    /**
     * The single-unit version of above. Adjusts the score of two words with
     * the same base to reflect tense considerations.
     * @param replacement1 - the first word
     * @param replacement2 - the second word
     * @param error - the error word
     */
    private void sortSameBaseWordsByTense(ScoredWord replacement1,
                                          ScoredWord replacement2, CharSequence error) {

        // Get the largest common suffix
        int common1 = 0;
        int common2 = 0;
        int loc0 = error.length() - 1;
        int loc1 = replacement1.word.length() - 1;
        int loc2 = replacement2.word.length() - 1;
        while (loc1 >= 0 && loc0 >= 0
                && replacement1.word.charAt(loc1) == error.charAt(loc0)) {
            common1++;
            loc1--;
            loc0--;
        }
        loc0 = error.length() - 1;
        while (loc2 >= 0 && loc0 >= 0
                && replacement2.word.charAt(loc2) == error.charAt(loc0)) {
            common2++;
            loc2--;
            loc0--;
        }

        // If suffix lengths are the same, don't change score
        if (common1 == common2) { return; }

        // Assign the larger score to the larger suffix
        if (common1 > common2) {
            replacement1.score = Math.max(replacement1.score, replacement2.score);
            replacement2.score = Math.min(replacement1.score, replacement2.score);
        } else {
            replacement1.score = Math.min(replacement1.score, replacement2.score);
            replacement2.score = Math.max(replacement1.score, replacement2.score);
        }
    }

    /**
     * Helper method to check if two words have the same "base word"
     * @param word1 - the first word
     * @param word2 - the second word
     * @return true if two words have the same "base word"
     */
    public boolean haveSameBase(ScoredWord word1, ScoredWord word2) {

        // Check for nulls and empties
        if (word1 == null || word2 == null) {
            throw new IllegalArgumentException(
                    "One or both of words are null! First: "
                            + word1 + " Second: " + word2);
        }
        if (word1.word.isEmpty() || word2.word.isEmpty()) { return false; }

        // Get the two words and which is longer. Equal doesn't matter
        String longer, shorter;
        if (word1.word.length() > word2.word.length()) {
            longer = word1.word;
            shorter = word2.word;
        } else {
            longer = word2.word;
            shorter = word1.word;
        }

        // Get prefix length
        int prefixLength = (int) Math.ceil(longer.length() * BASE_PREFIX);
        if (prefixLength > shorter.length()) { return false; }

        // Check prefix and return
        return shorter.startsWith(longer.substring(0, prefixLength));
    }

    /**
     * Get the probability of this replacement in a given window
     * @param window - the window
     * @param errorIndex - the index of the error
     * @param replacement - the replacement for the error
     * @return the replacement probability
     */
    public double getReplacementProbability(LinkedList<String> window, int errorIndex,
                                            String replacement) {
        window.set(errorIndex, replacement);

        // If we don't need to evaluate multiple n-grams just return
        if (window.size() <= MAX_N_GRAM_SIZE) {
            return getLogNGramProbability(window, 0, window.size() - 1);
        }

        // Evaluate multiple n-grams
        double probability = 0.0;
        for (int i = 0; i <= window.size()
                - MAX_N_GRAM_SIZE; i++) {
            probability += getLogNGramProbability(window,
                    i, i + MAX_N_GRAM_SIZE);
        }
        return probability;
    }

    /**
     * Main functional method to get the log probability for a given
     * n-gram
     * @param window - the context surrounding the n-gram
     * @param startIndex - the start position of the n-gram
     * @param endIndex - the end position of the n-gram
     * @return the calculated score
     */
    public float getLogNGramProbability(AbstractList<String> window,
                                        int startIndex, int endIndex) {
        int length = endIndex - startIndex;
        if (length < MIN_N_GRAM_SIZE
                || length > MAX_N_GRAM_SIZE
                || startIndex < 0 || endIndex > window.size()) {
            return 0.0f;
        }

        // Look up word indices, calculate probability, return
        return model.getLogProb(getWordIndices(
                new LinkedList<>(window.subList(startIndex, endIndex))));
    }

    /**
     * Get an int array from a phrase for querying in the LM
     * @param words - the list of words
     * @return the int array
     */
    private int[] getWordIndices(AbstractSequentialList<String> words) {
        int indices[] = new int[words.size()];
        for (int i = 0; i < words.size(); i++) {
            indices[i] = getWordIndex(words.get(i));
        }
        return indices;
    }

    /**
     * Helper method: get the word index associated with a given word
     * @param word - the word to query
     * @return the index
     */
    private int getWordIndex(String word) {
        return model.getWordIndexer().getIndexPossiblyUnk(word);
    }

    /**
     * Test methd
     * @param args
     */
    public static void main(String... args) {
        NGramLM testLM = new NGramLM();
        String testPhrase = "the man stood up for";
        String garbagePhrase = "gym and violin rig band";
        float testProb = testLM.getLogNGramProbability(
                new LinkedList<>(Arrays.asList(testPhrase.split(" "))), 1, 4);
        float garbageProb = testLM.getLogNGramProbability(
                new LinkedList<>(Arrays.asList(garbagePhrase.split(" "))), 0, 3);
        System.out.println("Good Phrase Probability: " + testProb
                + " Garbage Phrase Probability: " + garbageProb);
    }

}
