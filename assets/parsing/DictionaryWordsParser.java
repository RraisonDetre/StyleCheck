package assets.parsing;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

/**
 * Class to parse the contents of the UNIX words corpora.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class DictionaryWordsParser extends CorporaParser {

    // Path of UNIX words file
    private static final String WORDS_FILE = "src/files/corpora/English.words";

    // Each word mapped to itself for fast membership testing.
    private final HashMap<String, String> dictionary;

    /**
     * Constructor initializes and builds dictionary.
     */
    public DictionaryWordsParser() {
        dictionary = buildDictionary(WORDS_FILE);
    }

    /**
     * Build the dictionary from the file
     * @return the new dictionary
     */
    private static HashMap<String, String> buildDictionary(String filename) {
        HashMap<String, String> newDict = new HashMap<>();
        Collection<String> words = getFileLineList(filename);
        words.stream().filter(word -> word.length() > 0
                && !newDict.containsKey(word.toLowerCase())
                && !hasMoreThanOneCap(word.toLowerCase()))
                .forEach(word -> newDict.put(word.toLowerCase(),
                        word.toLowerCase()));
        return newDict;
    }

    /**
     * Check if a word is invalid because it's an acronym
     * @param word - the word
     * @return the
     */
    private static boolean hasMoreThanOneCap(String word) {
        int count = 0;
        for (char c : word.toCharArray()) {
            if (Character.isUpperCase(c)) {
                count++;
            }
        }
        return count >= 2;
    }

    /**
     * Return the completed dictionary.
     * @return dictionary - the Unix word list
     */
    public HashMap<String, String> getDictionary() { return dictionary; }

}
