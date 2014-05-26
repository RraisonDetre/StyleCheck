package assets.parsing;

import assets.general.NonNativeEssay;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Class to parse and randomly sample from the selection
 * of training essays available to it.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
@SuppressWarnings("SameParameterValue")
class NonNativeEssayParser extends CorporaParser {

    private static final String NON_NATIVE_INDEX = "training/index-training.csv";
    private static final String NON_NATIVE_INDEX_DELIM = ",";

    private static final String NON_NATIVE_ESSAY_DIR = "training/nonnative";

    /**
     * Return a random sample of essays of variable length
     * @param length - the number of essays to return
     * @return an ArrayList of NonNativeEssay objects
     */
    private static ArrayList<NonNativeEssay> getRandomEssaySample(int length) {
        Random rand = new Random();
        File essayDir = new File(NON_NATIVE_ESSAY_DIR);
        File essays[] = essayDir.listFiles();
        ArrayList<NonNativeEssay> essayList = new ArrayList<>();

        assert essays != null;
        if (length > essays.length) { throw new IllegalArgumentException(); }
        for (int i = 0; i < length; i++) {
            int essayNum = rand.nextInt(essays.length);
            essayList.add(getEssay(essayNum));
        }
        return essayList;
    }

    /**
     * Returns a NonNativeEssay object, given an essay number in the directory.
     * @param essayNum - - the number of the essay to retrieve
     * @return a new NonNativeEssay object
     */
    private static NonNativeEssay getEssay(int essayNum) {
        String index[][] = splitFileOnDelimiter(
                getFileAsString(NON_NATIVE_INDEX),
                NON_NATIVE_INDEX_DELIM);
        int pNum = Character.getNumericValue(index[essayNum][1].charAt(1));
        return new NonNativeEssay(
        );
    }

    /**
     * Return the essay requested by number (where the number refers to the
     * # file in the directory, not the number contained in the filename).
     * @param essayNum - the number of the essay to retrieve
     * @return the essay as a string
     */
    private static String getEssayString(int essayNum) {
        File essayDir = new File(NON_NATIVE_ESSAY_DIR);
        File essays[] = essayDir.listFiles();
        assert essays != null;
        if (essayNum > essays.length) { throw new IllegalArgumentException(); }
        return getFileAsString(essays[essayNum].getName());
    }

    /**
     * Test method
     */
    public static void main() {
        List<NonNativeEssay> essayList = getRandomEssaySample(3);
        System.out.println(essayList.get(2));
    }

}
