package assets.freqanalysis;

import assets.parsing.AuthorParser;
import assets.parsing.CorporaParser;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to have the word frequency functionality
 * interact with the user.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
class FrequencyClient {

    private static final String GREAT_EXPECTATIONS =
            "src/files/authors/Charles_Dickens/Great_Expectations.txt";

    private static final String BLEAK_HOUSE =
            "src/files/authors/Charles_Dickens/Bleak_House.txt";

    private static final String HUCKLEBERRY_FINN =
            "src/files/authors/Mark_Twain/Huckleberry_Finn.txt";

    private static final String TREASURE_ISLAND =
            "src/files/authors/Robert_Louis_Stevenson/Treasure_Island.txt";

    private static final String METAMORPHOSIS =
            "src/files/authors/Franz_Kafka/Metamorphosis.txt";

    /**
     * Main method to run frequency checker
     */
    public static void main() throws IOException {
        StatisticalAnalyzer fc = new StatisticalAnalyzer();

//        System.out.println("\nRUNNING ANALYSIS OF 'Great Expectations'...\n");
//        fc.analyzeFile(GREAT_EXPECTATIONS, true);
//
//        System.out.println("\nRUNNING ANALYSIS OF 'Huckleberry Finn'...\n");
//        fc.analyzeFile(HUCKLEBERRY_FINN, true);
//
//        System.out.println("\nRUNNING ANALYSIS OF 'Treasure Island'...\n");
//        fc.analyzeFile(TREASURE_ISLAND, true);

        String test[] = AuthorParser.titleAndAuthor(TREASURE_ISLAND);
        fc.identifier.analyzeSample(CorporaParser.getFileAsString(TREASURE_ISLAND), test[1]);
        System.out.println("Word Probability (and): " + fc.identifier.getWordProbability("and", test[1]));
        System.out.println("Log Word Probability (and): " + fc.identifier.getLogWordProbability("and", test[1]));
        double prob = fc.identifier.getUnigramSampleProbability(CorporaParser.getFileAsString(METAMORPHOSIS), test[1]);
        System.out.println("Probability of Kafka given Stevenson: " + prob);



        /////

        List<WorkProfile> profiles = new LinkedList<>();

        String titleAuthor[] = AuthorParser.titleAndAuthor(METAMORPHOSIS);
        WorkProfile metamorphosis = fc.profiler.generateProfile(CorporaParser
                .getFileAsString(METAMORPHOSIS), titleAuthor[0], titleAuthor[1]);
        metamorphosis.setWorkFile(METAMORPHOSIS);
        profiles.add(metamorphosis);
        fc.identifier.analyzeSample(CorporaParser.getFileAsString(METAMORPHOSIS), titleAuthor[1]);

        titleAuthor = AuthorParser.titleAndAuthor(GREAT_EXPECTATIONS);
        WorkProfile greatExpectations = fc.profiler.generateProfile(CorporaParser
                .getFileAsString(GREAT_EXPECTATIONS), titleAuthor[0], titleAuthor[1]);
        greatExpectations.setWorkFile(GREAT_EXPECTATIONS);
        profiles.add(greatExpectations);
        fc.identifier.analyzeSample(CorporaParser.getFileAsString(GREAT_EXPECTATIONS), titleAuthor[1]);

        titleAuthor = AuthorParser.titleAndAuthor(BLEAK_HOUSE);
        WorkProfile bleakHouse = fc.profiler.generateProfile(CorporaParser
                .getFileAsString(BLEAK_HOUSE), titleAuthor[0], titleAuthor[1]);
        bleakHouse.setWorkFile(BLEAK_HOUSE);
        profiles.add(bleakHouse);
        fc.identifier.analyzeSample(CorporaParser.getFileAsString(BLEAK_HOUSE), titleAuthor[1]);

        titleAuthor = AuthorParser.titleAndAuthor(HUCKLEBERRY_FINN);
        WorkProfile huckleberryFinn = fc.profiler.generateProfile(CorporaParser
                .getFileAsString(HUCKLEBERRY_FINN), titleAuthor[0], titleAuthor[1]);
        huckleberryFinn.setWorkFile(HUCKLEBERRY_FINN);
        profiles.add(huckleberryFinn);
        fc.identifier.analyzeSample(CorporaParser.getFileAsString(HUCKLEBERRY_FINN), titleAuthor[1]);

        titleAuthor = AuthorParser.titleAndAuthor(TREASURE_ISLAND);
        WorkProfile treasureIsland = fc.profiler.generateProfile(CorporaParser
                .getFileAsString(TREASURE_ISLAND), titleAuthor[0], titleAuthor[1]);
        treasureIsland.setWorkFile(TREASURE_ISLAND);
        profiles.add(treasureIsland);
        fc.identifier.analyzeSample(CorporaParser.getFileAsString(TREASURE_ISLAND), titleAuthor[1]);

        double profileDistance;
        double sampleProbability;
        for (WorkProfile profile1 : profiles) {
            for (WorkProfile profile2 : profiles) {
                if (!profile1.equals(profile2)) {

                    profileDistance = profile1.distanceTo(profile2);
                    System.out.println("\nDistance between " + profile1.title
                            + " and " + profile2.title + ": " + profileDistance);

                    sampleProbability = fc.identifier.getUnigramSampleProbability(profile2.getWork(), profile1.author);
                    System.out.println("Probability of " + profile2.title
                            + " using model of " + profile1.author + ": " + sampleProbability + "\n");
                }
            }
        }
    }

}
