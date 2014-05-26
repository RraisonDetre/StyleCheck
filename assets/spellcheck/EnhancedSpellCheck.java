package assets.spellcheck;

import assets.general.*;
import assets.gui.SCGUI;
import assets.parsing.CorporaParser;
import assets.parsing.WikipediaSpellingParser;
import org.clapper.util.misc.ObjectExistsException;
import org.clapper.util.misc.VersionMismatchException;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to provide a more nuanced spell checker for the StyleCheck
 * program. Involves checking against n-grams, corpora, and POS.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class EnhancedSpellCheck extends SpellGrammarCheck {

    // Levenshtein distance weight for ranking
    private static final double LEVENSHTEIN_WEIGHT = 2.0;

    private MisspelledWordMap wikiMap;    // Map of Wikipedia misspelled words
    private POSTagger tagger;             // Stanford POS tagger
    private NGramLM nGramSearch;          // Fast n-gram probability lookup
    private EnglishDictionary dict;       // UNIX words list

    public static final char SENTENCE_DELIM = '.';
    public static final char WORD_DELIM = ' ';

    private static final String VALID_FILE_EXT          // For suggested replacements
            = CorporaParser.TXT_EXT;                    // Valid extension for input file

    private int previousWord = 0;

    /**
     * Constructor - for now, just activates as standard rules
     * for American English, with Wikipedia misspelled word corpus
     * check and n-gram/POS check.
     * @throws java.io.IOException
     */
    public EnhancedSpellCheck() throws IOException, ObjectExistsException,
            VersionMismatchException, ClassNotFoundException {

        this(new POSTagger(), new JLanguageTool(new AmericanEnglish()));
    }

    /**
     * Overridden constructor that can take a preloaded tagger
     * @param t - the tagger
     * @param lt - the JLanguageTool instance
     */
    public EnhancedSpellCheck(POSTagger t, JLanguageTool lt) throws IOException {
        super(lt);
        tagger = t;
        loadComponents();
    }

    /**
     * Initialize the various subprograms required to run.
     */
    private void loadComponents() {
        System.out.println("Loading components...");
        long startTime = System.currentTimeMillis();

        loadWikiMap();
        loadNGramMap();
        loadDictionary();
        if (tagger == null) {
            tagger = new POSTagger();
        }

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;

        System.out.println("Loaded components in a total of " + runTime + " ms.");
        System.out.println("Done loading components!\n");
    }

    /**
     * Loads the Wikipedia map with some user feedback.
     */
    private void loadWikiMap() {
        System.out.println("Loading Wikipedia Spelling Corpus...");
        wikiMap = WikipediaSpellingParser.getWikipediaMap();
        System.out.println("Done!");
    }

    /**
     * Loads the n-gram map with some user feedback.
     */
    private void loadNGramMap() {
        System.out.println("Loading N-Gram Corpus...");
        nGramSearch = new NGramLM();
        System.out.println("Done!");
    }

    /**
     * Load the English dictionary.
     */
    private void loadDictionary() {
        System.out.println("Loading English Dictionary...");
        dict = new EnglishDictionary();
        System.out.println("Done!");
    }

    /**
     * Wrapper method for main spell check method that takes filename.
     * @param filename - the text file to analyze
     * @throws IOException
     */
    public void checkFileSpelling(String filename) throws IOException {
        if (!CorporaParser.validateExtension(filename, VALID_FILE_EXT)) {
            throw new IOException();
        }
        String fileText = CorporaParser.getFileAsString(filename);
        checkSpelling(fileText);
    }

    /**
     * Main spell check loop for text
     * @param text - the text to check
     * @return the suggested replacements indexed by error position
     * @throws IOException
     */
    public HashMap<Integer, LinkedList<String>> checkSpelling(String text)
            throws IOException {

        previousWord = 0;
        Iterable<ScoredWord> errorList
                = getErrorsFromWordList(SCStringFormat.getAllWordsAndPositions(text));
        HashMap<Integer, LinkedList<String>> replacementMap = new HashMap<>();

        // Add all the positions and replacements to the map, capitalizing first words
        for (ScoredWord error : errorList) {
            if (SCStringFormat.isFirstWordInSentence(text, (int) error.score)) {
                replacementMap.put((int) error.score, SCStringFormat.capitalizeWords(
                        getValidReplacements(getWindow(text, error.word), error.word)));
            } else {
                replacementMap.put((int) error.score,
                        getValidReplacements(getWindow(text, error.word), error.word));
            }
        }

        // Check for hyphenated words and british words
        List<Integer> removeMe = new LinkedList<>();
        for (Integer pos : replacementMap.keySet()) {

            // Get the end of the word
            int endOfWord = SCStringFormat.getEndOfCurrentWord(text, pos);
            if (endOfWord < 0) { continue; }

            // Check the word for valid hyphenation
            String word = text.substring(pos, endOfWord);
            int hyphens = SCStringFormat.countOccurrences(text, '-', false);
            if (hyphens == 1 || hyphens == 2) {
                removeMe.add(pos);
            }

            // Proper nouns
            if (Character.isUpperCase(word.charAt(0))
                    && !SCStringFormat.isFirstWordInSentence(text, pos)) {
                removeMe.add(pos);
            }

            // Check for British spellings
            if (isWord(cleanWord(word.replace("er", "re")))
                    || isWord(cleanWord(word.replace("o", "ou")))
                    || isWord(cleanWord(word.replace("ize", "ise")))
                    || isWord(cleanWord(word.replace("ization", "isation")))
                    || isWord(cleanWord(word.replace("ized", "ised")))
                    || isWord(cleanWord(word.replace("se", "ce")))
                    || isWord(cleanWord(word.replace("ction", "xion")))
                    || isWord(cleanWord(word.replace("yze", "yse")))
                    || isWord(cleanWord(word.replace("og", "ogue")))) {
                removeMe.add(pos);
            }
        }
        removeMe.forEach(replacementMap::remove);

        System.out.println("Spelling Errors Found: " + replacementMap.size());

        return replacementMap;
    }

    /**
     * Get a list of misspelled words from the list of all words
     * @param wordList - the list of all words
     * @return the list of misspelled words and positions
     */
    private LinkedList<ScoredWord> getErrorsFromWordList(Collection<ScoredWord> wordList) {
        return wordList.stream().filter(word -> !isWord(cleanWord(word.word))
                && SCStringFormat.isPossibleWord(word.word))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Main grammar check loop for text
     * @param text - the text to check
     * @param ignored - ignored grammar rules
     * @return the suggested replacements index by error position
     */
    public HashMap<Integer, LinkedList<String>> checkGrammar(String text,
                                                Map<String, String> ignored)
                                                throws IOException {

        List<RuleMatch> errorList = checkString(text);
        HashMap<Integer, LinkedList<String>> replacementMap = new HashMap<>();

        if (SCGUI.DEBUG) {
            for (RuleMatch error : errorList) {
                System.out.println("Grammar error at pos "
                        + error.getFromPos() + error.getShortMessage());
            }
        }

        // Filter to only grammar errors
        errorList.stream().filter(error -> !error.getRule().isSpellingRule()
                && !ignored.containsKey(error.getRule().getId())
                && error.getShortMessage() != null
                && !error.getShortMessage().equals("null")
                && !error.getShortMessage().equals("Redundant phrase")
                && !error.getShortMessage().equals("Three successive sentences begin with the same word.")
                && !error.getShortMessage().equals("Use smart quotes")
                && !error.getShortMessage().equals("Commonly confused word")
                && !error.getShortMessage().equals("Two consecutive dots")
                && !error.getShortMessage().equals("Grammatical problem"))
                .forEach(error -> replacementMap.put(error.getFromPos(),
                        new LinkedList<>(error.getSuggestedReplacements())));


        return replacementMap;
    }

    /**
     * Get the relevant "window" for n-gram processing
     * @param text - the text
     * @param word - the word
     * @return the tagged window
     */
    public LinkedList<String> getWindow(String text, String word) {
        LinkedList<String> sentenceWords
                = SCStringFormat.getAllWords(text, previousWord);
        int wordPosition = sentenceWords.indexOf(word);
        previousWord = wordPosition + previousWord;

        int firstWord = wordPosition - (NGramLM.MAX_N_GRAM_SIZE - 1);
        int lastWord = wordPosition + (NGramLM.MAX_N_GRAM_SIZE - 1);

        // Check if the window starts and/or ends the sentence
        if (firstWord < 0) {
            firstWord = 0;
        }
        if (lastWord >= sentenceWords.size()) {
            lastWord = sentenceWords.size() - 1;
        }
        int offset = firstWord;

        // Create window
        LinkedList<String> window = cleanWindow(new LinkedList<>(
                sentenceWords.subList(firstWord, lastWord + 1)));

        if (SCGUI.DEBUG_FINE) {
            System.out.println("Cleaned Window: "
                    + SCStringFormat.wordListAsPhrase(window));
        }

        // Check if another spelling error/s exists inside the window
        Iterable<Integer> otherErrors = window.stream().filter(
                windowWord -> !dict.isValidWord(windowWord.toLowerCase())
                && !word.equals(windowWord)).map(sentenceWords::indexOf)
                .collect(Collectors.toCollection(LinkedList::new));

        // Adjust window bounds
        wordPosition = window.indexOf(word);
        for (Integer error : otherErrors) {
            if (error < wordPosition && error >= firstWord) {
                firstWord = error + 1;
            } else if (error > wordPosition && error <= lastWord) {
                lastWord = error - 1;
            }
        }

        if (SCGUI.DEBUG_FINE) {
            System.out.println("First: " + firstWord + ", Last: "
                    + lastWord + ", Offset: " + offset + ", Window: "
                    + SCStringFormat.wordListAsPhrase(window));
        }
        if (lastWord - offset + 1 <= firstWord - offset) {
            lastWord = Math.min(firstWord + 2, window.size() - 1);
            if (lastWord - offset + 1 < 0) {
                return new LinkedList<>();
            }
        }

        // Now that we have definite boundaries return the window
        return new LinkedList<>(window.subList(firstWord - offset,
                lastWord - offset + 1));
    }

    /**
     * Remove any non-words (i.e. punctuation), trim whitespace, etc.
     * Generally clean up input for the n-gram ranking to do its job.
     * @param window - the sentence context word list
     * @return the new window
     */
    private LinkedList<String> cleanWindow(LinkedList<String> window) {
        if (SCGUI.DEBUG_FINE) {
            System.out.println("Window before cleaning: "
                    + SCStringFormat.wordListAsPhrase(window));
        }
        return window.stream().map(this::cleanWord)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Trim whitespace, punctuation, etc. from a word
     * @param word - the word to operate on
     * @return the new word
     */
    private String cleanWord(String word) {
        int start = 0;
        int end = word.length() - 1;
        while (start < word.length() - 1) {
            if (!Character.isLetterOrDigit(word.charAt(start))
                    && word.charAt(start) != '\''
                    && word.charAt(start) != '-') {
                start++;
            } else { break; }
        }
        while (end >= 0 && end > start) {
            if (!Character.isLetterOrDigit(word.charAt(end))
                    && word.charAt(end) != '\''
                    && word.charAt(end) != '-') {
                end--;
            } else { break; }
        }
        return word.substring(start, end + 1).toLowerCase();
    }

    /**
     * Check whether a given word is a valid entry for n-gram processing.
     * @param word - the word to check
     * @return true if the word is valid
     */
    private boolean isWord(String word) {
        return dict.isValidWord(word)
                || SCStringFormat.isNumericalString(word, true)
                || isSingleLetterWord(word);
    }

    /**
     * Returns true if the word is a valid single letter word
     * @param word - the word
     * @return true if the word is valid
     */
    private boolean isSingleLetterWord(String word) {
        String low = word.toLowerCase();
        return word.length() == 1
                && (low.charAt(0) == 'i' || low.charAt(0) == 'a'
                || low.charAt(0) == 'v' || low.charAt(0) == 'x');
    }

    /**
     * Get the current sentence being operated on so that POS tagging works
     * @param text - the text
     * @param errorPos - the error position
     * @return the sentence containing this word
     */
    public String getCurrentSentence(String text, int errorPos) {
        if (errorPos < 0 || errorPos >= text.length()) {
            throw new IllegalArgumentException("Error index out of range.");
        }
        return text.substring(text.lastIndexOf(SENTENCE_DELIM, errorPos) + 1,
                text.indexOf(SENTENCE_DELIM, errorPos)).trim();
    }

    /**
     * Simpler version for spell check only
     * @param window - the window
     * @param word - the word to replace
     * @return the ranked list of replacements
     */
    public LinkedList<String> getValidReplacements(LinkedList<String> window, String word) {
        return getValidReplacements(window, word, getPossibleReplacements(word));
    }

    /**
     * Get all possible replacements for a word then filter by validity.
     * @param window - the sentence context
     * @param word - the word to query
     * @param possible - the list of possible replacements
     * @return the list of valid replacements
     */
    public LinkedList<String> getValidReplacements(LinkedList<String> window, String word,
                                            LinkedList<String> possible) {

        // Get pure n-gram scored replacements (unsorted)
        LinkedList<ScoredWord> replacements = nGramSearch.scorePossibleReplacements(
                window, window.indexOf(word), possible);

        // Append weighted Levenshtein distance score and sort
        for (ScoredWord replace : replacements) {
            replace.score -= LEVENSHTEIN_WEIGHT
                    * StringDistance.levenshtein(word, replace.word);
        }
        Collections.sort(replacements, Collections.reverseOrder());

        if (SCGUI.DEBUG_FINE) {
            System.out.println("Retrieved " + replacements.size()
                    + " replacements for \"" + word + "\"");
        }

        // After sorting retrieve plain words and return
        return replacements.subList(0, Math.min(replacements.size(), SCGUI.MAX_REPLACEMENTS))
                .stream().map(replace -> replace.word)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all possible replacements for a word (with no duplicates), using
     * all sources for possible replacements.
     * @param word - the word to query
     * @return the list of possible replacement words
     */
    LinkedList<String> getPossibleReplacements(String word) {
        LinkedList<String> replacementList = new LinkedList<>();

        // Query the Wikipedia corpus of common misspellings
        Collection<String> wikiList = getWikipediaWords(word);
        if (wikiList != null) {
            wikiList.stream().filter(wikiWord -> !replacementList.contains(wikiWord)).forEach(replacementList::add);
        }

        // Query the English dictionary by edit distance
        Collection<String> editList = dict.getNClosestWords(word, EnglishDictionary.NUM_CLOSE_WORDS);
        if (editList != null) {
            editList.stream().filter(editWord -> !replacementList.contains(editWord)).forEach(replacementList::add);
        }

        return replacementList;
    }

    /**
     * Get the list of corrections for a word from the Wikipedia
     * corpus database.
     * @param word - the word to query
     * @return - a LinkedList of possible corrections
     */
    LinkedList<String> getWikipediaWords(String word) {
        return wikiMap.getCorrections(word);
    }

    /**
     * Getter for the dictionary
     * @return the EnglishDictionary object
     */
    public EnglishDictionary getDict() { return dict; }

    /**
     * Getter for the tagger
     * @return the POSTagger object
     */
    public POSTagger getTagger() { return tagger; }

}
