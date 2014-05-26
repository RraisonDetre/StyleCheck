package assets.general;

import assets.gui.SCGUI;
import edu.stanford.nlp.tagger.maxent.MaxentTagger;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Class to handle integration with the Stanford part of speech
 * tagger, as well as conversions between Penn and CLAWS7 tagsets.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class POSTagger {

    // Threshold for a "too big" sentence
    private static final int MAX_SENTENCE_LENGTH = 512;

    // Relative path to the tagger model
    private static final String MODEL
            = "src/external/stanford-postagger-2014-01-04/models/english-bidirectional-distsim.tagger";

    // The tagset to store - convert to PENN for use with other
    // parts of the StyleCheck program, or keep additional detail with CLAWS7.
    public static final int PENN = 0;
    private static final int CLAWS7 = 1;

    private final MaxentTagger tagger;

    /**
     * Constructor initializes POS tagger with default model.
     */
    public POSTagger() {
        tagger = new MaxentTagger(MODEL);
    }

    /**
     * Return a sentence with POS tags.
     * @param sentence - the sentence to tag
     * @return the tagged sentence
     */
    String getTaggedSentence(String sentence) {
        String taggedSentence = "";
        try {
            taggedSentence = tagger.tagString(sentence);
        } catch (OutOfMemoryError e) {
            if (SCGUI.DEBUG) {
                System.out.println("Heap Space Exceeded: " + e.getMessage());
            }
        }
        return taggedSentence;
    }

    /**
     * Get the tagged words from an input sentence
     * @param sentence - the sentence to tag
     * @return a LinkedList of tagged words
     */
    LinkedList<String> getTaggedWords(String sentence) {
        return new LinkedList<>(Arrays.asList(getTaggedSentence(sentence).split(" ")));
    }

    /**
     * Parse the tag from a word and return an array
     * consisting of [word][tag]
     * @param word - the word to parse a tag from
     * @return the parsed word and tag
     */
    private String[] parseTag(String word) {
        return word.trim().split("_");
    }

    /**
     * The most useful method - input a sentence and get back a LinkedList of
     * arrays of length 2, where each word is accompanied by its tag.
     * @param sentence - the sentence to tag
     * @return the LinkedList of word and tag
     */
    public LinkedList<String[]> getSentenceWordsAndTags(String sentence) {
        Iterable<String> taggedWords = getTaggedWords(sentence);
        LinkedList<String[]> wordsAndTags = new LinkedList<>();
        for (String tagged : taggedWords) {
            String wordPlusTag[] = parseTag(tagged);

            // If this is a punctuation mark, currency symbol, number, etc., ignore it
            if (!wordPlusTag[0].isEmpty()) {
                if (wordPlusTag[0].length() > 1
                        || Character.isLetterOrDigit(wordPlusTag[0].charAt(0))) {
                    wordsAndTags.add(wordPlusTag);
                }
            }
        }
        return wordsAndTags;
    }

    /**
     * Test method
     */
    public static void main() {
        POSTagger tagger = new POSTagger();
        System.out.println(tagger.getTaggedSentence("This is a sentence."));
        System.out.println();

        Iterable<String[]> wordTagList = tagger.getSentenceWordsAndTags("This is a sentence.");
        for (String[] wordTag : wordTagList) {
            System.out.println("Word: " + wordTag[0] + "  ||  Penn Tag: " + wordTag[1]);
        }
    }

}
