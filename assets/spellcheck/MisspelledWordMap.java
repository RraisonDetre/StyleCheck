package assets.spellcheck;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Class to hold commonly misspelled words and
 * their corrections.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class MisspelledWordMap {

    private final HashMap<String, LinkedList<String>> map;

    /**
     * Constructor - takes no arguments.
     */
    public MisspelledWordMap() {
        map = new HashMap<>();
    }

    /**
     * Add a word to the map
     * @param word - the misspelled word
     * @param corrections - the corrections
     */
    public void addWord(String word, LinkedList<String> corrections) {
        map.put(word, corrections);
    }

    /**
     * Getter for the central HashMap
     * @return the map
     */
    public Map<String, LinkedList<String>> getMap() {
        return map;
    }

    /**
     * Get the corrections list for a word.
     * @param word - the word to retrieve a list for
     * @return the corrections list
     */
    public LinkedList<String> getCorrections(String word) {
        if (!map.containsKey(word)) {
            return null;
        }
        return map.get(word);
    }

}
