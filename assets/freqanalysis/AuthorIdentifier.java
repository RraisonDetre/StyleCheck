package assets.freqanalysis;

import assets.gui.SCGUI;
import assets.parsing.AuthorParser;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to encapsulate author identification.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class AuthorIdentifier {

    public ProfileMap profiles;

    /**
     * Initialize a new ProfileMap
     * @throws IOException
     */
    public AuthorIdentifier() throws IOException {
        profiles = ProfileMap.buildMapFromFiles();
        if (profiles.getMap().isEmpty()) {
            System.out.println("Error: failed to load profiles!");
        } else {
            profiles.getMap().values().stream()
                    .filter(profile -> profile.totalAdverbs == 0.0)
                    .forEach(profile -> System.out.println("Error, profile is empty!"));
        }
    }

    /**
     * Get the distance between two author profiles that are
     * already in the map
     * @param author1 - the first author
     * @param author2 - the second author
     * @return the distance
     */
    public double getAuthorDistance(String author1, String author2) {

        // Ensure that both authors are in the map
        if (profiles.containsNoAuthor(author1)
                || profiles.containsNoAuthor(author2)) {
            throw new IllegalArgumentException();
        }

        // Retrieve the stored profiles
        AuthorProfile first = profiles.getMap().get(author1);
        AuthorProfile second = profiles.getMap().get(author2);
        assert first != null && second != null;

        // Return the heuristic distance
        return first.distanceToAuthor(second);
    }

    /**
     * Return the author with the closest writing style to
     * this piece
     * @param w - the WorkProfile to check
     * @return the closest author
     */
    public String getClosestAuthor(WorkProfile w) {
        String closest = null;
        double closestDist = Double.MAX_VALUE;
        double currentDist;

        for (String author : profiles.getMap().keySet()) {
            currentDist = profiles.getMap().get(author).distanceToWork(w);

            if (SCGUI.DEBUG_FINE) {
                System.out.println("Distance from " + w.title
                        + " to " + author + ": " + currentDist);
            }

            if (currentDist < closestDist) {
                closestDist = currentDist;
                closest = author;
            }
        }

        return closest;
    }

    /**
     * Test script for author identification
     * @param args
     */
    public static void main(String... args) {

        System.out.println("\nTESTING AUTHOR IDENTIFICATION:\n");
        long startTime = System.currentTimeMillis();

        System.out.println("LOADING AUTHOR PROFILES...");
        long mStartTime = System.currentTimeMillis();

        AuthorIdentifier identifier = null;
        try {
            identifier = new AuthorIdentifier();
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert identifier != null;

        long mEndTime = System.currentTimeMillis();
        long mRunTime = mEndTime - mStartTime;
        System.out.println("Loaded Author Profiles in " + mRunTime + " ms\n");

        List<WorkProfile> testProfiles = new LinkedList<>();

        File testDir = new File(AuthorParser.TEST_DIR);
        assert testDir.isDirectory();

        File testFiles[] = testDir.listFiles();
        assert testFiles != null;

        System.out.println("LOADING TEST PROFILES...");
        long pStartTime = System.currentTimeMillis();

        for (File testFile : testFiles) {
            try {
                if (testFile.isFile()
                        && testFile.getCanonicalPath().contains(AuthorParser.PROFILE_EXT)) {

                    testProfiles.add(WorkProfile.buildFromFile(testFile.getCanonicalPath()));
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        long pEndTime = System.currentTimeMillis();
        long pRunTime = pEndTime - pStartTime;
        System.out.println("Loaded Test Profiles in " + pRunTime + " ms\n");

        System.out.println("IDENTIFYING AUTHORS...");
        long iStartTime = System.currentTimeMillis();

        for (WorkProfile testProfile : testProfiles) {
            System.out.println("Closest author to " + testProfile.title
                    + " by " + testProfile.author + ": " + identifier.getClosestAuthor(testProfile));
        }

        long iEndTime = System.currentTimeMillis();
        long iRunTime = iEndTime - iStartTime;
        System.out.println("Identified Closest Authors in " + iRunTime + " ms\n");

        for (String author : identifier.profiles.getMap().keySet()) {
            System.out.println("Number of training works for " + author + ": "
                    + identifier.profiles.getMap().get(author).getWorks().size());
        }

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;
        System.out.println("\nTotal Run Time: " + runTime + " ms");
        System.out.println("\nALL TESTS COMPLETE");
    }

}
