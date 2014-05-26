package assets.freqanalysis;

import assets.general.POSTagger;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;

import java.io.IOException;

/**
 * Class to enclose various counters and interface
 * with entire files of text.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class StatisticalAnalyzer {

    public StylometricProfiler profiler;
    public LanguageModelIdentifier identifier;

    /**
     * Constructor to initialize components
     * @throws IOException
     */
    public StatisticalAnalyzer() throws IOException {
        this(new POSTagger(), new JLanguageTool(new AmericanEnglish()));
    }

    /**
     * Overridden constructor accepting the tagger
     * @param t - the tagger
     */
    public StatisticalAnalyzer(POSTagger t, JLanguageTool lt) throws IOException {
        lt.activateDefaultPatternRules();

        profiler = new StylometricProfiler(lt, t);
        identifier = new LanguageModelIdentifier(t, lt);
    }

}
