package assets.parsing;

import assets.spellcheck.MisspelledWordMap;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Class to parse the Wikipedia corpus of commonly
 * misspelled words.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class WikipediaSpellingParser extends CorporaParser {

    private static final String WIKI_SPELLING = "src/files/corpora/MisspelledWordsWikipedia.txt";
    private static final String WIKI_SPELLING_DELIM = "(->)|(, )";

    /**
     * Create a MisspelledWordMap from the Wikipedia corpus with all of the
     * misspelled words.
     * @return the new MisspelledWordMap
     */
    public static MisspelledWordMap getWikipediaMap() {
        MisspelledWordMap map = new MisspelledWordMap();
        String wikiData[][] = splitFileOnDelimiter(getFileAsString(WIKI_SPELLING),
                WIKI_SPELLING_DELIM);

        for (String[] aWikiData : wikiData) {
            String corrections[] = Arrays.copyOfRange(aWikiData, 1, aWikiData.length);
            map.addWord(aWikiData[0], new LinkedList<>(Arrays.asList(corrections)));
        }

        return map;
    }

    /**
     * Test method
     */
    public static void main() {
        MisspelledWordMap testMap = getWikipediaMap();
        System.out.println(testMap.getMap().get("achive"));
    }

}
