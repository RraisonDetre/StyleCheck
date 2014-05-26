package assets.general;

import assets.gui.SCGUI;
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.SynsetType;
import edu.smu.tspell.wordnet.WordNetDatabase;
import edu.smu.tspell.wordnet.WordNetException;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to encapsulate Princeton WordNet
 * synonym features.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class SynonymSearch {

    // Path of Wordnet 3.0 SQL databases
    private static final String WN_DB_PATH
            = "/Users/alexpwelton/Documents/Dartmouth 2013 - 2014/Winter/" +
            "Thesis/StyleCheck/src/external/JAWS/WordNet-3.0/dict/";

    // Number of synsets to examine for synonyms
    private static final int MAX_SYNSETS = 100;

    // Delimiter for words in a phrase
    public static final String WORD_DELIM = " ";

    // The JAWS WordNet interface
    private final WordNetDatabase database;

    // Create a new SynonymSearch object with the correct dir
    public SynonymSearch() {
        System.setProperty("wordnet.database.dir", WN_DB_PATH);
        database = WordNetDatabase.getFileInstance();
    }

    /**
     * Return a list of synonymous words and phrases
     * for a given word and part of speech
     * @param taggedWord - a word/tag combination
     * @return a LinkedList of synonyms for ranking
     */
    public LinkedList<String> getSynonyms(String taggedWord[]) {
        if (taggedWord == null || taggedWord[0] == null || taggedWord[1] == null
                || taggedWord[0].isEmpty() || taggedWord[1].isEmpty()
                || !POSTagConverter.isAllowedType(taggedWord[1])) {
            return new LinkedList<>();
        }

        LinkedList<String> synonymList = new LinkedList<>();
        Iterable<Synset> synsets = getNBestSynsets(taggedWord[0],
                getWordSynsetType(taggedWord[1]));

        // Get all synonymous words and phrases
        for (Synset currSet : synsets) {
            for (String currSynonym : currSet.getWordForms()) {
                if (!synonymList.contains(currSynonym)
                        && !currSynonym.equals(taggedWord[0])) {
                    synonymList.add(currSynonym);
                }
            }
        }
        return synonymList;
    }

    /**
     * Get the N best Synsets, where N is defined by constant
     * @return the sorted list of best synsets
     */
    public LinkedList<Synset> getNBestSynsets(String word, SynsetType type) {
        long startTime = System.currentTimeMillis();

        List<Synset> allSynsets = Arrays.asList(database.getSynsets(word, type));
        try {
            Collections.sort(allSynsets, (o1, o2) -> -1
                    * (int) Math.signum(o1.getTagCount(word.toLowerCase().trim())
                    -  o2.getTagCount(word.toLowerCase().trim())));
        } catch (WordNetException e) {
            if (SCGUI.DEBUG) {
                System.out.println("Word \"" + word
                        + "\" DNE in Synset. " + e.getMessage());
            }
        }

        if (SCGUI.DEBUG_FINE) {
            System.out.println("Loaded " + allSynsets.size()
                    + " synsets for word: \"" + word + "\" of type: " + type
                    + " in " + (System.currentTimeMillis() - startTime) + " ms.");
        }

        // Limit synset list to fixed size
        return new LinkedList<>(allSynsets.subList(0,
                Math.min(MAX_SYNSETS, allSynsets.size())));
    }

    /**
     * Get the appropriate synset type for a given POS
     * @param pos - the word's POS
     * @return the appropriate SynsetType
     */
    private SynsetType getWordSynsetType(String pos) {
        if (POSTagConverter.isAdverb(pos)) { return SynsetType.ADVERB; }
        else if (POSTagConverter.isVerb(pos)) { return SynsetType.VERB; }
        else if (POSTagConverter.isAdjective(pos)) { return SynsetType.ADJECTIVE; }
        else if (POSTagConverter.isNoun(pos)) { return SynsetType.NOUN; }
        else { throw new IllegalArgumentException("POS is not of an allowed type!"); }
    }

    /**
     * Test method
     * @param args
     */
    public static void main(String... args) {

        SynonymSearch syn = new SynonymSearch();
        System.out.println("Synonyms for \"house\":");
        String house[] = { "house", "NN" };
        for (String synonym : syn.getSynonyms(house)) {
            System.out.println("\t" + synonym);
        }

    }

}
