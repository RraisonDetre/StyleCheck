package assets.freqanalysis;

import assets.general.POSTagConverter;
import assets.general.POSTagger;
import assets.general.SCStringFormat;
import assets.general.ScoredWord;
import org.languagetool.JLanguageTool;

import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Counts the occurrences of each word in a given file
 * and reports if a given word is significantly above
 * average for a given POS. Also tracks sentence lengths.
 * Maintains detailed statistics on all of these.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class StylometricProfiler {

    // Number of standard deviations above average by POS
    private final static double ADV_THRESHOLD = 2.0;
    private final static double V_THRESHOLD = 2.0;
    private final static double ADJ_THRESHOLD = 2.0;
    private final static double N_THRESHOLD = 2.0;

    private HashMap<String, Integer> adverbs;
    private HashMap<String, Integer> verbs;
    private HashMap<String, Integer> adjectives;
    private HashMap<String, Integer> nouns;

    private LinkedList<String> numberWords;

    private double totalAdverbs;
    private double totalVerbs;
    private double totalAdjectives;
    private double totalNouns;

    private double adverbRatio;
    private double adjectiveRatio;
    private double nounRatio;
    private double verbRatio;

    private double adverbsPerSentence;
    private double adjectivesPerSentence;
    private double nounsPerSentence;
    private double verbsPerSentence;

    private double numSentences;
    private double totalSentences;
    private double avgSentences;
    private double ssSentences;
    private double varSentences;
    private double stdSentences;

    private double shortestSentence;
    private double longestSentence;

    private double shortestSentenceRatio;
    private double longestSentenceRatio;

    private double numWords;
    private double totalWords;
    private double avgWords;
    private double ssWords;
    private double varWords;
    private double stdWords;

    private double hapaxLegomena;
    private double hapaxRatio;
    private double disLegomena;
    private double disRatio;
    private double richnessRatio;

    private double ssAdverbs;
    private double ssVerbs;
    private double ssAdjectives;
    private double ssNouns;

    private double varAdverbs;
    private double varVerbs;
    private double varAdjectives;
    private double varNouns;

    private double stdAdverbs;
    private double stdVerbs;
    private double stdAdjectives;
    private double stdNouns;

    private double avgAdverbs;
    private double avgVerbs;
    private double avgAdjectives;
    private double avgNouns;

    private final JLanguageTool langTool;
    private final POSTagger tagger;

    /**
     * Constructor takes tagger and language tool as arguments,
     * initializes everything else to 0.
     */
    public StylometricProfiler(JLanguageTool lt, POSTagger pt) {
        langTool = lt;
        tagger = pt;
        clear();
    }

    /**
     * Set all statistics to 0 and initialize.
     */
    public void clear() {
        adverbs = new HashMap<>();
        verbs = new HashMap<>();
        adjectives = new HashMap<>();
        nouns = new HashMap<>();

        numberWords = new LinkedList<>();

        totalAdverbs = 0.0;
        totalVerbs = 0.0;
        totalAdjectives = 0.0;
        totalNouns = 0.0;

        adverbRatio = 0.0;
        adjectiveRatio = 0.0;
        nounRatio = 0.0;
        verbRatio = 0.0;

        adverbsPerSentence = 0.0;
        adjectivesPerSentence = 0.0;
        nounsPerSentence = 0.0;
        verbsPerSentence = 0.0;

        numSentences = 0.0;
        totalSentences = 0.0;
        avgSentences = 0.0;
        ssSentences = 0.0;
        varSentences = 0.0;
        stdSentences = 0.0;

        shortestSentence = Double.MAX_VALUE;
        longestSentence = Double.MIN_VALUE;

        shortestSentenceRatio = 0.0;
        longestSentenceRatio = 0.0;

        numWords = 0.0;
        totalWords = 0.0;
        avgWords = 0.0;
        ssWords = 0.0;
        varWords = 0.0;
        stdWords = 0.0;

        hapaxLegomena = 0.0;
        hapaxRatio = 0.0;
        disLegomena = 0.0;
        disRatio = 0.0;
        richnessRatio = 0.0;

        ssAdverbs = 0.0;
        ssVerbs = 0.0;
        ssAdjectives = 0.0;
        ssNouns = 0.0;

        varAdverbs = 0.0;
        varAdjectives = 0.0;
        varVerbs = 0.0;
        varNouns = 0.0;

        stdAdverbs = 0.0;
        stdAdjectives = 0.0;
        stdVerbs = 0.0;
        stdNouns = 0.0;

        avgAdverbs = 0.0;
        avgVerbs = 0.0;
        avgAdjectives = 0.0;
        avgNouns = 0.0;
    }

    /**
     * Analyze a text sample
     * @param sample - the text to analyze
     */
    public void analyzeSample(String sample) {
        DecimalFormat df = new DecimalFormat("##.##");
        double i = 0.0;

        System.out.println("Processing sample...");

        List<String> sentences = langTool.sentenceTokenize(sample);
        for (String sentence : sentences) {
            countSentence(sentence);

            if (!sentence.trim().equals("")) {
                Collection<String[]> wordsAndTags =
                        tagger.getSentenceWordsAndTags(sentence);

                wordsAndTags.stream().filter(word -> word.length > 1)
                        .forEach(word -> countWord(word[0], word[1]));
            }

            if (i % 500 == 0) {
                double percent = 100 * (i / sentences.size());
                System.out.println(df.format(percent) + "% processed...");
            }
            i++;
        }
        calculateStats();
    }

    /**
     * Add a sentence to the counter
     * @param sentence - the sentence to add
     */
    void countSentence(CharSequence sentence) {
        totalSentences += sentence.length();
        ssSentences += Math.pow(sentence.length(), 2);

        if (sentence.length() < shortestSentence) {
            shortestSentence = sentence.length();
        }
        if (sentence.length() > longestSentence) {
            longestSentence = sentence.length();
        }

        numSentences++;
    }

    /**
     * Add a word (with POS) to the counter
     * @param word - the word to add
     * @param pos - the word's part of speech
     */
    void countWord(String word, String pos) {
        if (SCStringFormat.isNumericalString(word, false)
                && !numberWords.contains(word)) {
            numberWords.add(word);
        }

        if (word.length() < 2 ||
                (word.length() == 1 && !(word.charAt(0) == 'I'
                        || word.charAt(0) == 'a' || word.charAt(0) == 'A'))) {
            return;
        }

        String realWord = word.toLowerCase();
        HashMap<String, Integer> map;
        if (POSTagConverter.isAdverb(pos)) {
            map = adverbs;
            totalAdverbs++;
        } else if (POSTagConverter.isVerb(pos)) {
            map = verbs;
            totalVerbs++;
        } else if (POSTagConverter.isAdjective(pos)) {
            map = adjectives;
            totalAdjectives++;
        } else if (POSTagConverter.isNoun(pos)) {
            map = nouns;
            totalNouns++;
        } else {
            return;
        }

        if (!map.containsKey(realWord)) {
            map.put(realWord, 0);
        } else {
            int score = map.get(realWord);
            map.replace(realWord, score + 1);
        }

        numWords++;
        totalWords += realWord.length();
        ssWords += Math.pow(realWord.length(), 2);
    }

    /**
     * Print the results of the analysis.
     */
    public void printAnalysis() {
        DecimalFormat df = new DecimalFormat("#.###");

        System.out.println("\nPART OF SPEECH FREQUENCY ANALYSIS:\n");

        System.out.println("\tTotal Number of Adverbs: " + df.format(totalAdverbs));
        System.out.println("\tTotal Number of Adjectives: " + df.format(totalAdjectives));
        System.out.println("\tTotal Number of Nouns: " + df.format(totalNouns));
        System.out.println("\tTotal Number of Verbs: " + df.format(totalVerbs) + "\n");

        System.out.println("\tRatio of Words that are Adverbs: " + df.format(adverbRatio));
        System.out.println("\tRatio of Words that are Adjectives: " + df.format(adjectiveRatio));
        System.out.println("\tRatio of Words that are Nouns: " + df.format(nounRatio));
        System.out.println("\tRatio of Words that are Verbs: " + df.format(verbRatio) + "\n");

        System.out.println("\tAverage Number of Adverbs Per Sentence: " + df.format(adverbsPerSentence));
        System.out.println("\tAverage Number of Adjectives Per Sentence: " + df.format(adjectivesPerSentence));
        System.out.println("\tAverage Number of Nouns Per Sentence: " + df.format(nounsPerSentence));
        System.out.println("\tAverage Number of Verbs Per Sentence: " + df.format(verbsPerSentence) + "\n");

        System.out.println("\tAdverb Variance: " + df.format(varAdverbs));
        System.out.println("\tAdjective Variance: " + df.format(varAdjectives));
        System.out.println("\tNoun Variance: " + df.format(varNouns));
        System.out.println("\tVerb Variance: " + df.format(varVerbs) + "\n");

        System.out.println("\tAdverb Standard Deviation: " + df.format(stdAdverbs));
        System.out.println("\tAdjective Standard Deviation: " + df.format(stdAdjectives));
        System.out.println("\tNoun Standard Deviation: " + df.format(stdNouns));
        System.out.println("\tVerb Standard Deviation: " + df.format(stdVerbs) + "\n");

        System.out.println("WORD FREQUENCY ANALYSIS:\n");

        System.out.println("ADVERBS:");
        for (String adv : getOverusedAdverbs()) {
            System.out.println("\t" + adv + " is used " + adverbs.get(adv)
                    + " times. Average: " + df.format(avgAdverbs));
        }
        System.out.println();

        System.out.println("VERBS:");
        for (String v : getOverusedVerbs()) {
            System.out.println("\t" + v + " is used " + verbs.get(v)
                    + " times. Average: " + df.format(avgVerbs));
        }
        System.out.println();

        System.out.println("ADJECTIVES:");
        for (String adj : getOverusedAdjectives()) {
            System.out.println("\t" + adj + " is used " + adjectives.get(adj)
                    + " times. Average: " + df.format(avgAdjectives));
        }
        System.out.println();

        System.out.println("NOUNS:");
        for (String adv : getOverusedNouns()) {
            System.out.println("\t" + adv + " is used " + nouns.get(adv)
                    + " times. Average: " + df.format(avgNouns));
        }

        System.out.println("\nSENTENCE LENGTH ANALYSIS:\n");
        System.out.println("\tNumber of Sentences: " + df.format(numSentences));
        System.out.println("\tAverage Sentence Length: " + df.format(avgSentences) + "\n");

        System.out.println("\tShortest Sentence Length: " + df.format(shortestSentence));
        System.out.println("\tRatio of Shortest Length to Average: "
                + df.format(shortestSentenceRatio) + "\n");

        System.out.println("\tLongest Sentence Length: " + df.format(longestSentence));
        System.out.println("\tRatio of Longest Length to Average: "
                + df.format(longestSentenceRatio) + "\n");

        System.out.println("\tSentence Length Variance: " + df.format(varSentences));
        System.out.println("\tStandard Deviation: " + df.format(stdSentences));

        System.out.println("\nWORD LENGTH ANALYSIS:\n");
        System.out.println("\tNumber of Words: " + df.format(numWords));
        System.out.println("\tAverage Word Length: " + df.format(avgWords));
        System.out.println("\tWord Length Variance: " + df.format(varWords));
        System.out.println("\tWord Length Standard Deviation: " + df.format(stdWords));

        System.out.println("\nVOCABULARY ANALYSIS:\n");
        System.out.println("\tNumber of Hapax Legomena: " + df.format(hapaxLegomena));
        System.out.println("\tRatio of Hapax Legomena: " + df.format(hapaxRatio));
        System.out.println("\tNumber of Dis Legomena: " + df.format(disLegomena));
        System.out.println("\tRatio of Dis Legomena: " + df.format(disRatio));
        System.out.println("\tVocabulary Richness Score: " + df.format(richnessRatio));

        System.out.println();
    }

    /**
     * Get a list of all adverbs used above average.
     * @return the list of adverbs
     */
    public Iterable<String> getOverusedAdverbs() {
        List<ScoredWord> advList = new LinkedList<>();
        LinkedList<String> finalList = new LinkedList<>();

        advList.addAll(adverbs.keySet().stream().filter(adv -> adverbs.get(adv) >
                (ADV_THRESHOLD * avgAdverbs))
                .map(adv -> new ScoredWord(adv, adverbs.get(adv)))
                .collect(Collectors.toList()));

        Collections.sort(advList, Collections.reverseOrder());
        finalList.addAll(advList.stream().map(sw -> sw.word).collect(Collectors.toList()));

        return finalList;
    }

    /**
     * Get a list of all verbs used above average.
     * @return the list of verbs
     */
    public Iterable<String> getOverusedVerbs() {
        List<ScoredWord> vList = new LinkedList<>();
        LinkedList<String> finalList = new LinkedList<>();

        vList.addAll(verbs.keySet().stream().filter(v -> verbs.get(v) >
                (V_THRESHOLD * avgVerbs)).map(v
                -> new ScoredWord(v, verbs.get(v)))
                .collect(Collectors.toList()));

        Collections.sort(vList, Collections.reverseOrder());
        finalList.addAll(vList.stream().map(sw -> sw.word).collect(Collectors.toList()));

        return finalList;
    }

    /**
     * Get a list of all adjectives used above average.
     * @return the list of adjectives
     */
    public Iterable<String> getOverusedAdjectives() {
        List<ScoredWord> adjList = new LinkedList<>();
        LinkedList<String> finalList = new LinkedList<>();

        adjList.addAll(adjectives.keySet().stream().filter(adj -> adjectives.get(adj) >
                (ADJ_THRESHOLD * avgAdjectives)).map(adj
                -> new ScoredWord(adj, adjectives.get(adj)))
                .collect(Collectors.toList()));

        Collections.sort(adjList, Collections.reverseOrder());
        finalList.addAll(adjList.stream().map(sw -> sw.word).collect(Collectors.toList()));

        return finalList;
    }

    /**
     * Get a list of all nouns used above average.
     * @return the list of nouns
     */
    public Iterable<String> getOverusedNouns() {
        List<ScoredWord> nounList = new LinkedList<>();
        LinkedList<String> finalList = new LinkedList<>();

        nounList.addAll(nouns.keySet().stream().filter(noun -> nouns.get(noun)
                > (N_THRESHOLD * avgNouns)).map(noun
                -> new ScoredWord(noun, nouns.get(noun)))
                .collect(Collectors.toList()));

        Collections.sort(nounList, Collections.reverseOrder());
        finalList.addAll(nounList.stream().map(sw -> sw.word).collect(Collectors.toList()));

        return finalList;
    }

    /**
     * Getter for number words list
     * @return the list of number words
     */
    public Iterable<String> getNumberWords() { return numberWords; }

    /**
     * Calculate the various statistics.
     */
    void calculateStats() {
        avgAdverbs = totalAdverbs / ((double) adverbs.size());
        if (Double.isInfinite(avgAdverbs)) { avgAdverbs = 0.0; }

        avgVerbs = totalVerbs / ((double) verbs.size());
        if (Double.isInfinite(avgVerbs)) { avgVerbs = 0.0; }

        avgAdjectives = totalAdjectives / ((double) adjectives.size());
        if (Double.isInfinite(avgAdjectives)) { avgAdjectives = 0.0; }

        avgNouns = totalNouns / ((double) nouns.size());
        if (Double.isInfinite(avgNouns)) { avgNouns = 0.0; }

        avgSentences = totalSentences / numSentences;
        if (Double.isInfinite(avgSentences)) { avgSentences = 0.0; }

        avgWords = totalWords / numWords;
        if (Double.isInfinite(avgWords)) { avgWords = 0.0; }

        hapaxLegomena = 0.0;
        disLegomena = 0.0;

        ssAdverbs = 0.0;
        Set<String> adverbSet = adverbs.keySet();
        for (String adv : adverbSet) {
            ssAdverbs += Math.pow(adverbs.get(adv), 2);
            if (adverbs.get(adv) == 1) {
                hapaxLegomena++;
            } else if (adverbs.get(adv) == 2) {
                disLegomena++;
            }
        }

        ssVerbs = 0.0;
        Set<String> verbSet = verbs.keySet();
        for (String v : verbSet) {
            ssVerbs += Math.pow(verbs.get(v), 2);
            if (verbs.get(v) == 1) {
                hapaxLegomena++;
            } else if (verbs.get(v) == 2) {
                disLegomena++;
            }
        }

        ssAdjectives = 0.0;
        Set<String> adjSet = adjectives.keySet();
        for (String adj : adjSet) {
            ssAdjectives += Math.pow(adjectives.get(adj), 2);
            if (adjectives.get(adj) == 1) {
                hapaxLegomena++;
            } else if (adjectives.get(adj) == 2) {
                disLegomena++;
            }
        }

        ssNouns = 0.0;
        Set<String> nounSet = nouns.keySet();
        for (String noun : nounSet) {
            ssNouns += Math.pow(nouns.get(noun), 2);
            if (nouns.get(noun) == 1) {
                hapaxLegomena++;
            } else if (nouns.get(noun) == 2) {
                disLegomena++;
            }
        }

        hapaxRatio = hapaxLegomena / numWords;
        disRatio = disLegomena / numWords;
        richnessRatio = hapaxLegomena / disLegomena;
        if (Double.isInfinite(hapaxRatio)) { hapaxRatio = 0.0; }
        if (Double.isInfinite(disRatio)) { disRatio = 0.0; }
        if (Double.isInfinite(richnessRatio)) { richnessRatio = 0.0; }

        shortestSentenceRatio = shortestSentence / avgSentences;
        longestSentenceRatio = longestSentence / avgSentences;
        if (Double.isInfinite(shortestSentenceRatio)) { shortestSentenceRatio = 0.0; }
        if (Double.isInfinite(longestSentenceRatio)) { longestSentenceRatio = 0.0; }

        adverbRatio = totalAdverbs / numWords;
        adverbsPerSentence = totalAdverbs / numSentences;
        if (Double.isInfinite(adverbRatio)) { adverbRatio = 0.0; }
        if (Double.isInfinite(adverbsPerSentence)) { adverbsPerSentence = 0.0; }

        adjectiveRatio = totalAdjectives / numWords;
        adjectivesPerSentence = totalAdjectives / numSentences;
        if (Double.isInfinite(adjectiveRatio)) { adjectiveRatio = 0.0; }
        if (Double.isInfinite(adjectivesPerSentence)) { adjectivesPerSentence = 0.0; }

        nounRatio = totalNouns / numWords;
        nounsPerSentence = totalNouns / numSentences;
        if (Double.isInfinite(nounRatio)) { nounRatio = 0.0; }
        if (Double.isInfinite(nounsPerSentence)) { nounsPerSentence = 0.0; }

        verbRatio = totalVerbs / numWords;
        verbsPerSentence = totalVerbs / numSentences;
        if (Double.isInfinite(verbRatio)) { verbRatio = 0.0; }
        if (Double.isInfinite(verbsPerSentence)) { verbsPerSentence = 0.0; }

        varAdverbs = (ssAdverbs / (double) adverbs.size())
                - Math.pow(avgAdverbs, 2);
        varAdjectives = (ssAdjectives / (double) adjectives.size())
                - Math.pow(avgAdjectives, 2);
        varVerbs = (ssVerbs / (double) verbs.size())
                - Math.pow(avgVerbs, 2);
        varNouns = (ssNouns / (double) nouns.size())
                - Math.pow(avgNouns, 2);
        varSentences = (ssSentences / numSentences)
                - Math.pow(avgSentences, 2);
        varWords = (ssWords / numWords)
                - Math.pow(avgWords, 2);
        if (Double.isInfinite(varAdverbs)) { varAdverbs = 0.0; }
        if (Double.isInfinite(varAdjectives)) { varAdjectives = 0.0; }
        if (Double.isInfinite(varVerbs)) { varVerbs = 0.0; }
        if (Double.isInfinite(varNouns)) { varNouns = 0.0; }
        if (Double.isInfinite(varSentences)) { varSentences = 0.0; }
        if (Double.isInfinite(varWords)) { varWords = 0.0; }

        stdAdverbs = Math.sqrt(Math.abs(varAdverbs));
        stdVerbs = Math.sqrt(Math.abs(varVerbs));
        stdAdjectives = Math.sqrt(Math.abs(varAdjectives));
        stdNouns = Math.sqrt(Math.abs(varNouns));
        stdSentences = Math.sqrt(Math.abs(varSentences));
        stdWords = Math.sqrt(Math.abs(varWords));
    }

    /**
     * Generate a profile without re-analyzing statistics
     * @return the new profile
     */
    public WorkProfile generateProfile() {
        double POSTotals[] = {totalAdverbs, totalVerbs, totalAdjectives, totalNouns};

        double POSRatios[] = {adverbRatio, adjectiveRatio, nounRatio, verbRatio};

        double POSPerSentence[] = {adverbsPerSentence, adjectivesPerSentence,
                nounsPerSentence, verbsPerSentence};

        double sentenceInfo[] = {numSentences, totalSentences, avgSentences, ssSentences,
                varSentences, stdSentences, shortestSentence, longestSentence,
                shortestSentenceRatio, longestSentenceRatio};

        double wordInfo[] = {numWords, totalWords, avgWords, ssWords, varWords, stdWords,
                hapaxLegomena, hapaxRatio, disLegomena, disRatio, richnessRatio};

        double statsInfo[] = {ssAdverbs, ssVerbs, ssAdjectives, ssNouns, varAdverbs,
                varAdjectives, varVerbs, varNouns, stdAdverbs, stdAdjectives, stdVerbs,
                stdNouns, avgAdverbs, avgVerbs, avgAdjectives, avgNouns};

        double info[][] = {POSTotals, POSRatios, POSPerSentence,
                sentenceInfo, wordInfo, statsInfo};

        return new WorkProfile("My Document", "Current User", info);
    }

    /**
     * Generate and return a WorkProfile object from the statistics.
     * @param title - the title of the work
     * @param author - the author of the work
     * @return a new WorkProfile object
     */
    public WorkProfile generateProfile(String sample, String title, String author) {

        clear();
        analyzeSample(sample);
        calculateStats();

        double POSTotals[] = {totalAdverbs, totalVerbs, totalAdjectives, totalNouns};

        double POSRatios[] = {adverbRatio, adjectiveRatio, nounRatio, verbRatio};

        double POSPerSentence[] = {adverbsPerSentence, adjectivesPerSentence,
                nounsPerSentence, verbsPerSentence};

        double sentenceInfo[] = {numSentences, totalSentences, avgSentences, ssSentences,
                varSentences, stdSentences, shortestSentence, longestSentence,
                shortestSentenceRatio, longestSentenceRatio};

        double wordInfo[] = {numWords, totalWords, avgWords, ssWords, varWords, stdWords,
                hapaxLegomena, hapaxRatio, disLegomena, disRatio, richnessRatio};

        double statsInfo[] = {ssAdverbs, ssVerbs, ssAdjectives, ssNouns, varAdverbs,
                varAdjectives, varVerbs, varNouns, stdAdverbs, stdAdjectives, stdVerbs,
                stdNouns, avgAdverbs, avgVerbs, avgAdjectives, avgNouns};

        double info[][] = {POSTotals, POSRatios, POSPerSentence,
                sentenceInfo, wordInfo, statsInfo};

        return new WorkProfile(title, author, info);
    }

}
