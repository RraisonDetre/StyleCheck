package assets.spellcheck;

import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;
import org.languagetool.rules.RuleMatch;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to interface with JLanguageTool
 * and provide basic spelling/grammar check
 * functionality.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
@SuppressWarnings("SameParameterValue")
class SpellGrammarCheck {

    // JLanguageTool object that will do the processing
    private final JLanguageTool langTool;

    /**
     * Constructor - for now, just activates as standard rules
     * for American English.
     * @throws IOException
     */
    SpellGrammarCheck() throws IOException {
        this(new JLanguageTool(new AmericanEnglish()));
    }

    /**
     * Overridden for preexistent languagetool
     * @param lt - the language tool
     */
    SpellGrammarCheck(JLanguageTool lt) throws IOException {
        langTool = lt;
        langTool.activateDefaultPatternRules();
    }

    /**
     * Produces a list of Strings corresponding to the mistakes
     * in the input string, as specified by the LanguageTool's rules.
     * @param input - the string to check
     * @return a list of RuleMatch objects for that string
     * @throws IOException
     */
    List<RuleMatch> checkString(String input) throws IOException {
        return langTool.check(input);
    }

    /**
     * Get a LinkedList of Strings of errors discovered in this sentence
     * @param input - the string input to check
     * @return the LinkedList of errors
     * @throws IOException
     */
    LinkedList<String> getSpellingErrors(String input) throws IOException {
        List<RuleMatch> matchList = checkString(input);
        LinkedList<String> errorList = new LinkedList<>();
        matchList.stream().filter(match -> match.getRule().isSpellingRule()).forEach(match -> {
            errorList.add(input.substring(match.getFromPos(), match.getToPos()));
            if (errorList.peekLast().trim().length() < 2
                    || isProperNoun(errorList.peekLast(), match.getFromPos())) {
                errorList.removeLast();
            }
        });
        return errorList;
    }

    /**
     * Return true if a given word is a proper noun. If a proper noun
     * is the first word in a sentence this method will return false.
     * @param word - the word to test
     * @param startPos - the start position of the word
     * @return true if the word is a proper noun
     */
    boolean isProperNoun(CharSequence word, int startPos) {
        return Character.isUpperCase(word.charAt(0)) && startPos > 0;
    }

    /**
     * Return a LinkedList of suggested replacements for a RuleMatch
     * @param match - the RuleMatch object
     * @return the LinkedList of replacements
     */
    List<String> getCorrections(RuleMatch match) {
        return new LinkedList<>(match.getSuggestedReplacements());
    }

    /**
     * Takes a section of text and returns a list of sentences as Strings.
     * @param text - the text to parse
     * @return a list of sentences as Strings
     */
    List<String> parseToSentences(String text) {
        List<String> sentenceList = langTool.sentenceTokenize(text);
        return sentenceList.stream().map(String::trim)
                .collect(Collectors.toCollection(LinkedList::new));
    }

    // Main testing method
    public static void main() throws IOException {
        SpellGrammarCheck spc = new SpellGrammarCheck();
        List<RuleMatch> result = spc.checkString("This strng has some problemns.");
        for (RuleMatch m : result) {
            System.out.println("Uh oh! Probable error at line: " + m.getLine()
                + " column: " + m.getColumn() + " : " + m.getMessage());
            System.out.println("LanguageTool suggested replacement: "
                + spc.getCorrections(m));
        }

        System.out.println();
        List<String> errorList = spc.getSpellingErrors("This strng has some problemns.");
        System.out.println(errorList.toString());
        System.out.println();

        System.out.println();
        System.out.println(spc.parseToSentences("This is a sentence. This is another sentence. " +
                "This is a third sentence"));
        System.out.println(spc.parseToSentences("This is a sentence. This is another sentence. " +
                "This is a third sentence.").get(0) + "!");
        System.out.println();
    }

}
