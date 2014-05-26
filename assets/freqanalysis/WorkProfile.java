package assets.freqanalysis;

import assets.gui.SCPanelSuggestions;
import assets.parsing.AuthorParser;
import assets.parsing.CorporaParser;

import java.io.*;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to encapsulate all the information about a given work.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class WorkProfile {

    private static final String ENCODING = "utf-8";

    public final String title;
    public final String author;

    public double totalAdverbs;
    public double totalVerbs;
    public double totalAdjectives;
    public double totalNouns;

    public double adverbRatio;
    public double adjectiveRatio;
    public double nounRatio;
    public double verbRatio;

    public double adverbsPerSentence;
    public double adjectivesPerSentence;
    public double nounsPerSentence;
    public double verbsPerSentence;

    public double numSentences;
    public double totalSentences;
    public double avgSentences;
    public double ssSentences;
    private double varSentences;
    private double stdSentences;

    public double shortestSentence;
    public double longestSentence;

    public double shortestSentenceRatio;
    public double longestSentenceRatio;

    public double numWords;
    public double totalWords;
    public double avgWords;
    public double ssWords;
    private double varWords;
    private double stdWords;

    public double hapaxLegomena;
    public double hapaxRatio;
    public double disLegomena;
    public double disRatio;
    public double richnessRatio;

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

    private String workFile;

    /**
     * Constructor takes title and author
     * @param t - the title
     * @param a - the author
     */
    public WorkProfile(String t, String a, double info[][]) {
        title = t;
        author = a;
        workFile = null;
        unpackInfo(info);
    }

    /**
     * Build a WorkProfile from the saved file
     * @param filename - the WorkProfile file
     * @return the WorkProfile
     */
    public static WorkProfile buildFromFile(String filename) {
        if (!filename.contains(AuthorParser.PROFILE_EXT)) {
            return null;
        }
        List<String> fileLines = CorporaParser.getFileLineList(filename);

        double profileInfo[][] = new double[6][];
        profileInfo[0] = new double[4];
        profileInfo[1] = new double[4];
        profileInfo[2] = new double[4];
        profileInfo[3] = new double[10];
        profileInfo[4] = new double[11];
        profileInfo[5] = new double[16];

        for (int i = 0; i < profileInfo[0].length; i++) {
            profileInfo[0][i] = Double.parseDouble(fileLines.get(i + 2));
            profileInfo[1][i] = Double.parseDouble(fileLines.get(i + 6));
            profileInfo[2][i] = Double.parseDouble(fileLines.get(i + 10));
        }

        for (int j = 0; j < profileInfo[3].length; j++) {
            profileInfo[3][j] = Double.parseDouble(fileLines.get(j + 14));
        }

        for (int k = 0; k < profileInfo[4].length; k++) {
            profileInfo[4][k] = Double.parseDouble(fileLines.get(k + 24));
        }

        for (int l = 0; l < profileInfo[5].length; l++) {
            profileInfo[5][l] = Double.parseDouble(fileLines.get(l + 35));
        }

        WorkProfile profile = new WorkProfile(fileLines.get(0), fileLines.get(1), profileInfo);
        profile.setWorkFile(filename.replace(AuthorParser.PROFILE_EXT, AuthorParser.BOOK_EXT));
        return profile;
    }

    /**
     * Unpack the individual profiler into their respective locations
     * @param info - the StylometricProfiler information
     */
    void unpackInfo(double info[][]) {

        totalAdverbs = info[0][0];
        totalVerbs = info[0][1];
        totalAdjectives = info[0][2];
        totalNouns = info[0][3];

        adverbRatio = info[1][0];
        adjectiveRatio = info[1][1];
        nounRatio = info[1][2];
        verbRatio = info[1][3];

        adverbsPerSentence = info[2][0];
        adjectivesPerSentence = info[2][1];
        nounsPerSentence = info[2][2];
        verbsPerSentence = info[2][3];

        numSentences = info[3][0];
        totalSentences = info[3][1];
        avgSentences = info[3][2];
        ssSentences = info[3][3];
        varSentences = info[3][4];
        stdSentences = info[3][5];

        shortestSentence = info[3][6];
        longestSentence = info[3][7];

        shortestSentenceRatio = info[3][8];
        longestSentenceRatio = info[3][9];

        numWords = info[4][0];
        totalWords = info[4][1];
        avgWords = info[4][2];
        ssWords = info[4][3];
        varWords = info[4][4];
        stdWords = info[4][5];

        hapaxLegomena = info[4][6];
        hapaxRatio = info[4][7];
        disLegomena = info[4][8];
        disRatio = info[4][9];
        richnessRatio = info[4][10];

        ssAdverbs = info[5][0];
        ssVerbs = info[5][1];
        ssAdjectives = info[5][2];
        ssNouns = info[5][3];

        varAdverbs = info[5][4];
        varAdjectives = info[5][5];
        varVerbs = info[5][6];
        varNouns = info[5][7];

        stdAdverbs = info[5][8];
        stdAdjectives = info[5][9];
        stdVerbs = info[5][10];
        stdNouns = info[5][11];

        avgAdverbs = info[5][12];
        avgVerbs = info[5][13];
        avgAdjectives = info[5][14];
        avgNouns = info[5][15];
    }

    /**
     * Generates a complete SC panel string based
     * on the data in this profile. Must have profiled
     * and calculated stats before running.
     * @param wc - the document word count
     * @return the complete panel string
     */
    public String getSCPanelString(int wc) {
        return SCPanelSuggestions.getPanelStyleString(wc, richnessRatio,
                avgSentences, varSentences, adverbRatio, adverbsPerSentence,
                verbRatio, verbsPerSentence, adjectiveRatio, adjectivesPerSentence,
                nounRatio, nounsPerSentence);
    }

    /**
     * Calculates a distance score between two work profiles.
     * @param other - the work profile to calculate distance to
     * @return the distance
     */
    public double distanceTo(WorkProfile other) {
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
     * Optionally store the filename for later retrieval
     * @param filename - the filename
     */
    public void setWorkFile(String filename) { workFile = filename; }

    /**
     * Get the work by loading the file
     * @return the work
     */
    public String getWork() {
        if (workFile != null) {
            return CorporaParser.getFileAsString(workFile);
        } else { return null; }
    }

    /**
     * Get the String of the entire profile for writing to file
     * @return the profile string
     */
    String getProfileString() {
        String profileString = "";

        profileString += getProfileStringLine(title);
        profileString += getProfileStringLine(author);

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

        profileString += getProfileStringLine(avgAdverbs);
        profileString += getProfileStringLine(avgVerbs);
        profileString += getProfileStringLine(avgAdjectives);
        profileString += getProfileStringLine(avgNouns);

        profileString += getProfileStringLine(ssAdverbs);
        profileString += getProfileStringLine(ssVerbs);
        profileString += getProfileStringLine(ssAdjectives);
        profileString += getProfileStringLine(ssNouns);

        profileString += getProfileStringLine(varAdverbs);
        profileString += getProfileStringLine(varVerbs);
        profileString += getProfileStringLine(varAdjectives);
        profileString += getProfileStringLine(varNouns);

        profileString += getProfileStringLine(stdAdverbs);
        profileString += getProfileStringLine(stdVerbs);
        profileString += getProfileStringLine(stdAdjectives);
        profileString += getProfileStringLine(stdNouns);

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
     * Save this profile to a file (work filename must be set)
     */
    void saveToFile() {
        if (workFile == null) {
            throw new IllegalArgumentException();
        }

        String profileFilename = AuthorParser.getProfileFilename(workFile);
        BufferedWriter fileWriter = null;

        try {
            fileWriter = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(profileFilename), ENCODING));
            fileWriter.write(getProfileString());

        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                if (fileWriter != null) {
                    fileWriter.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fast hashing from title and author.
     * @return the hash code
     */
    @Override
    public int hashCode() {
        String combo = title + author;
        return combo.hashCode();
    }

    /**
     * Two WorkProfiles are considered equivalent if they have the same
     * title and author.
     * @param o - the other WorkProfile
     * @return true if the two WorkProfiles are identical
     */
    @SuppressWarnings("InstanceofInterfaces")
    @Override
    public boolean equals(Object o) {
        return o instanceof WorkProfile && title.equals(((WorkProfile) o).title)
                && author.equals(((WorkProfile) o).author);
    }

    /**
     * Build the work profiles
     * @param args
     */
    public static void main(String... args) throws IOException {

        System.out.println("BUILDING ALL WORK PROFILES...\n");

        File authorsDir = new File(AuthorParser.AUTHORS_DIR);
        long startTime = System.currentTimeMillis();
        StatisticalAnalyzer fc = new StatisticalAnalyzer();
        WorkProfile current;

        assert authorsDir.isDirectory();
        File authors[] = authorsDir.listFiles();
        assert authors != null;

        for (File author : authors) {
            if (author.isDirectory()) {

                File works[] = author.listFiles();
                assert works != null;
                for (File work : works) {

                    if (!work.getCanonicalPath().contains(AuthorParser.PROFILE_EXT)
                            && work.getCanonicalPath().contains(AuthorParser.BOOK_EXT)) {

                        assert work.isFile();
                        long currStartTime = System.currentTimeMillis();

                        String titleAuthor[] = AuthorParser.titleAndAuthor(work.getCanonicalPath());

                        System.out.println("\nProcessing Book: " + titleAuthor[0]);
                        current = fc.profiler.generateProfile(CorporaParser
                                        .getFileAsString(work.getAbsolutePath()
                                                .substring(work.getAbsolutePath().indexOf("src"),
                                                        work.getAbsolutePath().length())),
                                                        titleAuthor[0], titleAuthor[1]);
                        current.setWorkFile(work.getCanonicalPath());
                        current.saveToFile();

                        long currEndTime = System.currentTimeMillis();
                        long currRunTime = currEndTime - currStartTime;

                        System.out.println("Processed " + titleAuthor[0] + " in " + currRunTime + " ms.");
                    }
                }
            }
        }

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;

        System.out.println("\nLOADED ALL PROFILES IN " + runTime + " ms");
    }

}
