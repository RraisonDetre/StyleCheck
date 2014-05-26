package assets.general;

import assets.parsing.ContractionsParser;
import assets.parsing.DictionaryWordsParser;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to allow searching of an English dictionary corpus.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
@SuppressWarnings("SameParameterValue")
public class EnglishDictionary {

    // Max number of dictionary replacements to consider when ranking
    public static final int NUM_CLOSE_WORDS = 30;

    private Map<String, String> dictionary;         // Recognized English words
    private Map<String, String> userDictionary;     // Any user-added words

    // List of contractions (logical equivalents)
    private Map<String, LinkedList<String>> contractions;

    /**
     * Retrieves resources and builds a new searchable dictionary.
     */
    public EnglishDictionary() {
        initialize();
    }

    /**
     * Initializes components.
     */
    private void initialize() {
        DictionaryWordsParser parser = new DictionaryWordsParser();
        dictionary = parser.getDictionary();
        userDictionary = new HashMap<>();
        contractions = ContractionsParser.getContractionDictionary();
    }

    /**
     * Add a word to the dictionary
     * @param word - the word to add
     */
    void addToDictionary(String word) {
        String lowerWord = word.toLowerCase();
        if (!dictionary.containsKey(lowerWord)) {
            dictionary.put(lowerWord, lowerWord);
        }
    }

    /**
     * Add another dictionary to this dictionary
     * @param otherDict - the other dictionary
     */
    public void addAllToDictionary(Map<String, String> otherDict) {
        otherDict.keySet().stream().filter(word
                -> !dictionary.containsKey(word))
                .forEach(this::addToDictionary);
    }

    /**
     * Return all the full forms of a given contraction
     * @return the list of full forms
     */
    public Collection<String> getFullForms(String contraction) {
        if (!isContraction(contraction)) {
            return new LinkedList<>();
        } else {
            return contractions.get(contraction);
        }
    }

    /**
     * Check if a word is a known contraction
     * @param word - the word
     * @return true if the word is a contraction
     */
    public boolean isContraction(String word) {
        return contractions.containsKey(word.toLowerCase().trim());
    }

    /**
     * Wrapper for default N
     * @param testWord - the word
     * @return the N closest words
     */
    public LinkedList<String> getNClosestWords(String testWord) {
        return getNClosestWords(testWord, NUM_CLOSE_WORDS);
    }

    /**
     * Return a list of the n closest words by String distance.
     * @param testWord - the word to compare to
     * @return the LinkedList of closest words from best to worst
     */
    public LinkedList<String> getNClosestWords(String testWord, int n) {
        List<ScoredWord> distanceList = new LinkedList<>();

        // If this is already a word we only want that word (regardless of n)
        if (isDictionaryWord(testWord)) {
            return new LinkedList<>(Arrays.asList(testWord));
        }
        Set<String> wordSet = dictionary.keySet();      // Get dictionary words
        wordSet.addAll(userDictionary.keySet());        // Get user words
        int currentScore;

        // Go through dictionary and retrieve n best scores
        for (String word : wordSet) {
            if (Math.abs(word.length() - testWord.length()) < 3) {
                currentScore = stringDistance(testWord, word);
                distanceList.add(new ScoredWord(word, currentScore));
            }
        }

        // Sort from min to max distance and truncate list
        Collections.sort(distanceList);

        // Add multi-word suggestions, if any, to the front
        LinkedList<String> multi = generateMultiWordSuggestions(testWord);
        multi.addAll(distanceList.subList(0, Math.min(n, distanceList.size()))
                .stream().map(close -> close.word).collect(Collectors.toList()));

        // Add contractions, if any, to the front
        if (isContraction(testWord)) {
            multi.addAll(0, getFullForms(testWord));
        }

        // Return full list
        return multi;
    }

    /**
     * Generate a list of suggestions that could arise from accidental
     * joining of two words
     * @param testWord - the word
     * @return the list of suggestions, which may be empty
     */
    LinkedList<String> generateMultiWordSuggestions(String testWord) {
        LinkedList<String> suggestionList = new LinkedList<>();
        String word1, word2;
        for (int i = 1; i < testWord.length() - 1; i++) {
            word1 = testWord.substring(0, i);
            word2 = testWord.substring(i);
            if (isValidWord(word1) && isValidWord(word2)) {
                suggestionList.add(word1 + " " + word2);
            }
        }
        return suggestionList;
    }

    /**
     * Checks whether a given word is a valid word
     * @param test - the word to search
     * @return true if the word is in either dictionary
     */
    public boolean isValidWord(String test) {
        return isDictionaryWord(test) || isUserWord(test);
    }

    /**
     * Returns true if the word is in the dictionary.
     * @param test - the word to search
     * @return true if the word is in the dictionary
     */
    boolean isDictionaryWord(String test) {
        return dictionary.containsKey(test);
    }

    /**
     * Return true if the word is in the user dictionary
     * @param test - the word to search
     * @return true if the word is in the user dictionary
     */
    public boolean isUserWord(String test) {
        return userDictionary.containsKey(test);
    }

    /**
     * Assumes that dictionary word is in/from dictionary
     * @param testWord - the word to get a distance for
     * @param dictWord - the word in the dictionary
     * @return the Levenshtein distance between the two words
     */
    int stringDistance(String testWord, String dictWord) {
        return StringDistance.levenshtein(testWord, dictWord);
    }

}
