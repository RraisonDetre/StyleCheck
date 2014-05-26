package assets.freqanalysis;

import assets.general.POSTagger;
import assets.parsing.AuthorParser;
import assets.parsing.CorporaParser;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Class to encapsulate author identification with
 * a unigram/bigram language model.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class LanguageModelIdentifier {

    private static final String UNKNOWN_UNI = "asdfg";  // Gibberish word for unknown words
    private LinkedList<String> UNKNOWN_BI;              // Gibberish bigram for unknown

    // Weights for unigrams versus bigrams
    private static final double UNIGRAM_WEIGHT = 0.2;

    // For writing to and reading from file
    private static final String FIELD_DELIM = ":";
    private static final String ENCODING = "utf-8";

    private Map<String, HashMap<String, Double>> unigramModels;
    private Map<String, HashMap<LinkedList<String>, Double>> bigramModels;
    private HashMap<String, Double> unigramCounts;
    private HashMap<String, Double> bigramCounts;

    private final JLanguageTool langTool;
    private final POSTagger tagger;

    /**
     * Constructor to initialize
     */
    private LanguageModelIdentifier() throws IOException {
        this(new POSTagger(), new JLanguageTool(new AmericanEnglish()));
    }

    /**
     * Overridden constructor to save on multiple resource copies
     * @param t - the tagger
     * @param lt - the language tool
     */
    public LanguageModelIdentifier(POSTagger t, JLanguageTool lt) throws IOException {
        tagger = t;
        langTool = lt;
        langTool.activateDefaultPatternRules();
        clear();
    }

    /**
     * Reset profiler for new analysis
     */
    void clear() {
        UNKNOWN_BI = new LinkedList<>();
        UNKNOWN_BI.add(UNKNOWN_UNI);
        UNKNOWN_BI.add(UNKNOWN_UNI);

        unigramModels = new HashMap<>();
        bigramModels = new HashMap<>();
        unigramCounts = new HashMap<>();
        bigramCounts = new HashMap<>();
    }

    /**
     * Analyze a sample of text
     * @param sample - the text
     */
    public void analyzeSample(String sample, String author) {
        DecimalFormat df = new DecimalFormat("##.##");
        double i = 0.0;

        if (!unigramModels.containsKey(author)) {
            unigramModels.put(author, new HashMap<>());
            unigramModels.get(author).put(UNKNOWN_UNI, 1.0);

            bigramModels.put(author, new HashMap<>());
            bigramModels.get(author).put(UNKNOWN_BI, 1.0);

            unigramCounts.put(author, 0.0);
            bigramCounts.put(author, 0.0);
        }

        List<String> sentences = langTool.sentenceTokenize(sample);
        for (String sentence : sentences) {
            if (!sentence.trim().equals("")) {
                AbstractSequentialList<String[]> wordsAndTags =
                        tagger.getSentenceWordsAndTags(sentence);

                for (int j = 0; j < wordsAndTags.size() - 1; j++) {
                    if (wordsAndTags.get(j).length > 1) {
                        countWord(wordsAndTags.get(j)[0], author);
                        countBigram(wordsAndTags.get(j)[0],
                                wordsAndTags.get(j + 1)[0], author);
                    }
                }
            }

            if (i % 1000 == 0) {
                double percent = 100 * (i / sentences.size());
                System.out.println(df.format(percent) + "% processed...");
            }
            i++;
        }
    }

    /**
     * Register a bigram in the map as having occurred
     * @param word1 - the first word
     * @param word2 - the second word
     * @param author - the author
     */
    void countBigram(String word1, String word2, String author) {
        LinkedList<String> bigram = new LinkedList<>();
        bigram.add(word1.toLowerCase());
        bigram.add(word2.toLowerCase());

        if (bigramModels.get(author).containsKey(bigram)) {
            bigramModels.get(author).replace(bigram, bigramModels.get(author).get(bigram) + 1.0);
        } else {
            bigramModels.get(author).put(bigram, 1.0);
        }
        bigramCounts.replace(author, bigramCounts.get(author) + 1.0);
    }

    /**
     * Register a word in the map as having occurred
     * @param word - the word
     */
    void countWord(String word, String author) {
        String realWord = word.toLowerCase();
        if (unigramModels.get(author).containsKey(realWord)) {
            unigramModels.get(author).replace(realWord, unigramModels.get(author).get(realWord) + 1.0);
        } else {
            unigramModels.get(author).put(realWord, 1.0);
        }
        unigramCounts.replace(author, unigramCounts.get(author) + 1.0);
    }

    /**
     * Query a sample against the model and retrieve that sample's score
     * using the unigram model.
     * @param sample - the text sample
     * @return the probability of this sample's existence
     */
    public double getUnigramSampleProbability(String sample, String authorModel) {
        DecimalFormat df = new DecimalFormat("##.##");
        double i = 0.0;
        double probability = 0.0;

        System.out.println("Calculating sample probability (unigrams)...");

        List<String> sentences = langTool.sentenceTokenize(sample);
        for (String sentence : sentences) {
            if (!sentence.trim().equals("")) {
                Iterable<String[]> wordsAndTags =
                        tagger.getSentenceWordsAndTags(sentence);

                for (String word[] : wordsAndTags) {
                    if (word.length > 1) {
                        probability += getLogWordProbability(word[0], authorModel);
                    }
                }
            }

            if (i % 500 == 0) {
                double percent = 100 * (i / sentences.size());
                System.out.println(df.format(percent) + "% processed...");
            }
            i++;
        }

        return probability;
    }

    /**
     * Save a single model to file
     * @param author - the author to save
     */
    void saveModelToFile(String author) {
        BufferedWriter fileWriter = null;

        try {
            fileWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(
                            AuthorParser.getModelFilename(author)), ENCODING));

            fileWriter.write(author + "\n");
            fileWriter.write(unigramCounts.get(author).toString() + "\n");

            for (String word : unigramModels.get(author).keySet()) {
                fileWriter.write(word + FIELD_DELIM
                        + unigramModels.get(author).get(word).toString() + "\n");
            }
            for (LinkedList<String> bigram : bigramModels.get(author).keySet()) {
                fileWriter.write(bigram.get(0) + FIELD_DELIM + bigram.get(1) + FIELD_DELIM
                        + bigramModels.get(author).get(bigram).toString() + "\n");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fileWriter != null) {
                try {
                    fileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Get the negative log probability of a word appearing
     * based on the model by a given author
     * @param word - the word to check
     * @param author - the author
     * @return the negative log probability
     */
    public double getLogWordProbability(String word, String author) {
        return -1.0 * Math.log(getWordProbability(word, author));
    }

    /**
     * Get the probability of a given word appearing according
     * to the model by a given author
     * @param word - the word to check
     * @return the probability
     */
    public double getWordProbability(String word, String author) {
        if (unigramModels.get(author).containsKey(word)) {
            return unigramModels.get(author).get(word) / unigramCounts.get(author);
        } else {
            return unigramModels.get(author).get(UNKNOWN_UNI) / unigramCounts.get(author);
        }
    }

    /**
     * Get the probability of the second word in a bigram using the first
     * @param bigram - the bigram to check
     * @param author - the author
     * @return the probability
     */
    double getBigramProbability(LinkedList<String> bigram, String author) {
        if (unigramModels.get(author).containsKey(bigram.get(0))) {
            if (bigramModels.get(author).containsKey(bigram)) {
                return bigramModels.get(author).get(bigram)
                        / unigramModels.get(author).get(bigram.get(0));
            }
        }
        return bigramModels.get(author).get(UNKNOWN_BI) / bigramCounts.get(author);
    }

    /**
     * Build and test the LanguageModelIdentifier
     * @param args
     */
    public static void main(String... args) throws IOException {

        System.out.println("BUILDING ALL MODELS...\n");

        File authorsDir = new File(AuthorParser.AUTHORS_DIR);
        long startTime = System.currentTimeMillis();
        LanguageModelIdentifier ngi = new LanguageModelIdentifier();
        String titleAuthor[];

        assert authorsDir.isDirectory();
        File authors[] = authorsDir.listFiles();
        assert authors != null;

        for (File author : authors) {
            if (author.isDirectory() && !author.getName().equals("test")) {

                File works[] = author.listFiles();
                assert works != null;
                for (File work : works) {

                    if (!work.getCanonicalPath().contains(AuthorParser.PROFILE_EXT)
                            && work.getCanonicalPath().contains(AuthorParser.BOOK_EXT)) {

                        assert work.isFile();
                        long currStartTime = System.currentTimeMillis();

                        titleAuthor = AuthorParser.titleAndAuthor(work.getCanonicalPath());

                        System.out.println("\nProcessing Book: " + titleAuthor[0]);
                        ngi.analyzeSample(CorporaParser.getFileAsString(work.getAbsolutePath()
                                .substring(work.getAbsolutePath().indexOf("src"),
                                        work.getAbsolutePath().length())), titleAuthor[1]);


                        long currEndTime = System.currentTimeMillis();
                        long currRunTime = currEndTime - currStartTime;

                        System.out.println("Processed " + titleAuthor[0] + " in " + currRunTime + " ms.");
                        ngi.saveModelToFile(titleAuthor[1]);
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;

        System.out.println("\nLOADED ALL MODELS IN " + runTime + " ms");

    }

}
