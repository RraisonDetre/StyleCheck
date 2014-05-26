package assets.parsing;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 * Class to parse the Wikipedia list of common contractions.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class ContractionsParser extends CorporaParser {

    public static final String WIKI_CONTRACTIONS_PATH
            = "src/files/corpora/ContractionsWikipedia.txt";
    public static final String WIKI_CONTRACTIONS_DELIM = "/";

    /**
     * Build the dictionary of contractions from file
     * @return the dictionary of contractions
     */
    public static HashMap<String, LinkedList<String>> getContractionDictionary() {
        HashMap<String, LinkedList<String>> contractions = new HashMap<>();
        List<String> fileLines = getFileLineList(WIKI_CONTRACTIONS_PATH);
        fileLines.stream().filter(line -> !line.isEmpty()
                && !line.contains(WIKI_CONTRACTIONS_DELIM))
                .forEach(line -> contractions.put(line.toLowerCase().trim(),
                getAlternateForms(fileLines.get(fileLines.indexOf(line)))));
        return contractions;
    }

    /**
     * Helper method for parsing the separate "true" forms
     * @param formLine - the line from the corpus
     * @return the list of "true" forms
     */
    private static LinkedList<String> getAlternateForms(String formLine) {
        LinkedList<String> forms = new LinkedList<>();
        for (String part : formLine.split(WIKI_CONTRACTIONS_DELIM)) {
            forms.add(part.toLowerCase().trim());
        }
        return forms;
    }

}
