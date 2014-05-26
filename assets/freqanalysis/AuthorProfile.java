package assets.freqanalysis;

import assets.parsing.CorporaParser;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to encapsulate an author's profile and accompanying methods.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class AuthorProfile {

    private static final String AUTHOR_DIR = "src/files/authors/";
    private static final String PROFILE_EXT = "_PROFILE.txt";

    public final String author;
    private final HashSet<WorkProfile> works;

    public double totalAdverbs;
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

    /**
     * Constructor initializes to empty
     */
    private AuthorProfile(String a) {
        works = new HashSet<>();
        author = a;
        clear();
    }

    /**
     * Generate a complete author profile from its profile file
     * @param authorName - the author name
     * @return the completed profile
     */
    public static AuthorProfile buildFromFile(String authorName) {
        String filename = AUTHOR_DIR + authorName;
        List<String> fileLines = CorporaParser.getFileLineList(filename);
        assert !fileLines.isEmpty();

        AuthorProfile newProfile = new AuthorProfile(fileLines.get(0));

        newProfile.totalAdverbs = Double.parseDouble(fileLines.get(1));
        newProfile.totalVerbs = Double.parseDouble(fileLines.get(2));
        newProfile.totalAdjectives = Double.parseDouble(fileLines.get(3));
        newProfile.totalNouns = Double.parseDouble(fileLines.get(4));

        newProfile.adverbRatio = Double.parseDouble(fileLines.get(5));
        newProfile.verbRatio = Double.parseDouble(fileLines.get(6));
        newProfile.adjectiveRatio = Double.parseDouble(fileLines.get(7));
        newProfile.nounRatio = Double.parseDouble(fileLines.get(8));

        newProfile.adverbsPerSentence = Double.parseDouble(fileLines.get(9));
        newProfile.verbsPerSentence = Double.parseDouble(fileLines.get(10));
        newProfile.adjectivesPerSentence = Double.parseDouble(fileLines.get(11));
        newProfile.nounsPerSentence = Double.parseDouble(fileLines.get(12));

        newProfile.totalSentences = Double.parseDouble(fileLines.get(13));
        newProfile.numSentences = Double.parseDouble(fileLines.get(14));
        newProfile.avgSentences = Double.parseDouble(fileLines.get(15));
        newProfile.ssSentences = Double.parseDouble(fileLines.get(16));
        newProfile.varSentences = Double.parseDouble(fileLines.get(17));
        newProfile.stdSentences = Double.parseDouble(fileLines.get(18));

        newProfile.shortestSentence = Double.parseDouble(fileLines.get(19));
        newProfile.longestSentence = Double.parseDouble(fileLines.get(20));

        newProfile.shortestSentenceRatio = Double.parseDouble(fileLines.get(21));
        newProfile.longestSentenceRatio = Double.parseDouble(fileLines.get(22));

        newProfile.totalWords = Double.parseDouble(fileLines.get(23));
        newProfile.numWords = Double.parseDouble(fileLines.get(24));
        newProfile.avgWords = Double.parseDouble(fileLines.get(25));
        newProfile.ssWords = Double.parseDouble(fileLines.get(26));
        newProfile.varWords = Double.parseDouble(fileLines.get(27));
        newProfile.stdWords = Double.parseDouble(fileLines.get(28));

        newProfile.hapaxLegomena = Double.parseDouble(fileLines.get(29));
        newProfile.hapaxRatio = Double.parseDouble(fileLines.get(30));
        newProfile.disLegomena = Double.parseDouble(fileLines.get(31));
        newProfile.disRatio = Double.parseDouble(fileLines.get(32));
        newProfile.richnessRatio = Double.parseDouble(fileLines.get(33));

        return newProfile;
    }

    /**
     * Build from a given author's directory
     * @param author - the author
     * @return the new author profile
     */
    private static AuthorProfile buildFromAuthorDir(String author) throws IOException {
        AuthorProfile newProfile = new AuthorProfile(author);
        File authorDir = new File(AUTHOR_DIR + author);
        assert authorDir.isDirectory();

        File workFiles[] = authorDir.listFiles();
        assert workFiles != null;

        for (File workFile : workFiles) {
            if (workFile.isFile()
                    && workFile.getCanonicalPath().contains(PROFILE_EXT)) {
                WorkProfile currProfile = WorkProfile.buildFromFile(workFile
                        .getCanonicalPath().substring(workFile.getCanonicalPath()
                                .indexOf(AUTHOR_DIR)));
                assert currProfile != null;

                newProfile.addProfile(currProfile);
            }
        }

        newProfile.calculateStats();
        return newProfile;
    }

    /**
     * Reset profiler
     */
    void clear() {
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
    }

    /**
     * Update profiler from a single new work profile
     * @param work - the new work profile
     */
    private void updateStatsFromWork(WorkProfile work) {
        totalAdverbs += work.totalAdverbs;
        totalVerbs += work.totalVerbs;
        totalAdjectives += work.totalAdjectives;
        totalNouns += work.totalNouns;

        numSentences += work.numSentences;
        totalSentences += work.totalSentences;
        ssSentences += work.ssSentences;

        if (work.shortestSentence < shortestSentence) {
            shortestSentence = work.shortestSentence;
        }
        if (work.longestSentence > longestSentence) {
            longestSentence = work.longestSentence;
        }

        numWords += work.numWords;
        totalWords += work.totalWords;
        ssWords += work.ssWords;

        hapaxLegomena += work.hapaxLegomena;
        disLegomena += work.disLegomena;
    }

    /**
     * Calculate ratios and statistical properties
     */
    void calculateStats() {
        hapaxRatio = hapaxLegomena / numWords;
        disRatio = disLegomena / numWords;
        richnessRatio = hapaxLegomena / disLegomena;

        adverbRatio = totalAdverbs / numWords;
        adverbsPerSentence = totalAdverbs / numSentences;

        adjectiveRatio = totalAdjectives / numWords;
        adjectivesPerSentence = totalAdjectives / numSentences;

        nounRatio = totalNouns / numWords;
        nounsPerSentence = totalNouns / numSentences;

        verbRatio = totalVerbs / numWords;
        verbsPerSentence = totalVerbs / numSentences;

        avgSentences = totalSentences / numSentences;
        avgWords = totalWords / numWords;

        varSentences = (ssSentences / numSentences)
                - Math.pow(avgSentences, 2);
        varWords = (ssWords / numWords)
                - Math.pow(avgWords, 2);

        stdSentences = Math.sqrt(varSentences);
        stdWords = Math.sqrt(varWords);

        shortestSentenceRatio = shortestSentence / avgSentences;
        longestSentenceRatio = longestSentence / avgSentences;
    }

    /**
     * Add a work to the author profile
     * @param w - the profile to add
     */
    void addProfile(WorkProfile w) {
        works.add(w);
        updateStatsFromWork(w);
        calculateStats();
    }

    /**
     * Getter for list of WorkProfiles
     * @return the LinkedList of profiles
     */
    public HashSet<WorkProfile> getWorks() { return works; }

    /**
     * Calculates a distance score between this author and a work profile
     * @param other - the work profile to calculate distance to
     * @return the distance
     */
    public double distanceToWork(WorkProfile other) {
        double squared = 0.0;

        squared += Math.pow(other.adverbRatio - adverbRatio, 2);
        squared += Math.pow(other.adjectiveRatio - adjectiveRatio, 2);
        squared += Math.pow(other.nounRatio - nounRatio, 2);
        squared += Math.pow(other.verbRatio - verbRatio, 2);

        squared += Math.pow(other.adverbsPerSentence - adverbsPerSentence, 2);
        squared += Math.pow(other.adjectivesPerSentence - adjectivesPerSentence, 2);
        squared += Math.pow(other.nounsPerSentence - nounsPerSentence, 2);
        squared += Math.pow(other.verbsPerSentence - verbsPerSentence, 2);

//        squared += Math.pow(other.avgSentences - avgSentences, 2);
//        System.out.println("Current squared distance: " + squared);
//        squared += Math.pow(other.avgWords - avgWords, 2);
//        System.out.println("Current squared distance: " + squared);
//
//        squared += Math.pow(other.shortestSentenceRatio - shortestSentenceRatio, 2);
//        System.out.println("Current squared distance: " + squared);
//        squared += Math.pow(other.longestSentenceRatio - longestSentenceRatio, 2);
//        System.out.println("Current squared distance: " + squared);

//        squared += Math.pow(other.hapaxRatio - hapaxRatio, 2);
//        System.out.println("Current squared distance: " + squared);
//        squared += Math.pow(other.disRatio - disRatio, 2);
//        System.out.println("Current squared distance: " + squared);
//        squared += Math.pow(other.richnessRatio - richnessRatio, 2);
//        System.out.println("Current squared distance: " + squared);

        return Math.sqrt(squared);
    }

    /**
     * Calculates the distance between this author profile
     * and another
     * @param other - the other author profile
     * @return the distance
     */
    public double distanceToAuthor(AuthorProfile other) {
        double squared = 0.0;

        squared += Math.pow(other.adverbRatio - adverbRatio, 2);
        squared += Math.pow(other.adjectiveRatio - adjectiveRatio, 2);
        squared += Math.pow(other.nounRatio - nounRatio, 2);
        squared += Math.pow(other.verbRatio - verbRatio, 2);

        squared += Math.pow(other.adverbsPerSentence - adverbsPerSentence, 2);
        squared += Math.pow(other.adjectivesPerSentence - adjectivesPerSentence, 2);
        squared += Math.pow(other.nounsPerSentence - nounsPerSentence, 2);
        squared += Math.pow(other.verbsPerSentence - verbsPerSentence, 2);

        squared += Math.pow(other.avgSentences - avgSentences, 2);
        squared += Math.pow(other.avgWords - avgWords, 2);

        squared += Math.pow(other.shortestSentenceRatio - shortestSentenceRatio, 2);
        squared += Math.pow(other.longestSentenceRatio - longestSentenceRatio, 2);

        squared += Math.pow(other.hapaxRatio - hapaxRatio, 2);
        squared += Math.pow(other.disRatio - disRatio, 2);
        squared += Math.pow(other.richnessRatio - richnessRatio, 2);

        return Math.sqrt(squared);
    }

    /**
     * Save this profile to file
     */
    void saveToFile() {
        String filename = AUTHOR_DIR + author + PROFILE_EXT;
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(new File(filename)));
            writer.write(getProfileString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the String of the entire profile for writing to file
     * @return the profile string
     */
    String getProfileString() {
        String profileString = "";

        profileString += getProfileStringLine(author.replaceFirst("_", " "));

        profileString += getProfileStringLine(totalAdverbs);
        profileString += getProfileStringLine(totalVerbs);
        profileString += getProfileStringLine(totalAdjectives);
        profileString += getProfileStringLine(totalNouns);

        profileString += getProfileStringLine(adverbRatio);
        profileString += getProfileStringLine(verbRatio);
        profileString += getProfileStringLine(adjectiveRatio);
        profileString += getProfileStringLine(nounRatio);

        profileString += getProfileStringLine(adverbsPerSentence);
        profileString += getProfileStringLine(verbsPerSentence);
        profileString += getProfileStringLine(adjectivesPerSentence);
        profileString += getProfileStringLine(nounsPerSentence);

        profileString += getProfileStringLine(totalSentences);
        profileString += getProfileStringLine(numSentences);
        profileString += getProfileStringLine(avgSentences);
        profileString += getProfileStringLine(ssSentences);
        profileString += getProfileStringLine(varSentences);
        profileString += getProfileStringLine(stdSentences);

        profileString += getProfileStringLine(shortestSentence);
        profileString += getProfileStringLine(longestSentence);

        profileString += getProfileStringLine(shortestSentenceRatio);
        profileString += getProfileStringLine(longestSentenceRatio);

        profileString += getProfileStringLine(totalWords);
        profileString += getProfileStringLine(numWords);
        profileString += getProfileStringLine(avgWords);
        profileString += getProfileStringLine(ssWords);
        profileString += getProfileStringLine(varWords);
        profileString += getProfileStringLine(stdWords);

        profileString += getProfileStringLine(hapaxLegomena);
        profileString += getProfileStringLine(hapaxRatio);
        profileString += getProfileStringLine(disLegomena);
        profileString += getProfileStringLine(disRatio);
        profileString += getProfileStringLine(richnessRatio);

        return profileString;
    }

    /**
     * Simple utility method for building the profile string
     * @param in - the input
     * @return the profile string line
     */
    private String getProfileStringLine(String in) { return in + "\n"; }

    /**
     * Simple utility method for building the profile string
     * @param in - the input
     * @return the profile string line
     */
    private String getProfileStringLine(double in) { return in + "\n"; }

    /**
     * Simple hashcode of the author's name
     * @return the hashcode
     */
    @Override
    public int hashCode() { return author.hashCode(); }

    /**
     * Two author profiles are equal if they have the same author
     * @param o - the author profile to compare to
     * @return true if the profiles are equal
     */
    @SuppressWarnings("InstanceofInterfaces")
    @Override
    public boolean equals(Object o) {
        return o instanceof AuthorProfile
                && author.equals(((AuthorProfile) o).author);
    }

    /**
     * Build authorprofile files
     * @param args
     */
    public static void main(String... args) throws IOException {

        File authorsDir = new File(AUTHOR_DIR);
        assert authorsDir.isDirectory();

        File authorFiles[] = authorsDir.listFiles();
        assert authorFiles != null;

        for (File authorFile : authorFiles) {
            if (authorFile.isDirectory()
                    && !authorFile.getCanonicalPath().contains("test")) {
                long start = System.currentTimeMillis();

                AuthorProfile currProfile = AuthorProfile
                        .buildFromAuthorDir(authorFile.getName());
                assert currProfile != null;

                currProfile.saveToFile();

                long end = System.currentTimeMillis();
                long run = end - start;
                System.out.println("Loaded Author: " + currProfile.author
                        + " in " + run + " ms");
            }
        }

    }

}
