package assets.gui;

import assets.freqanalysis.AuthorIdentifier;
import assets.freqanalysis.StatisticalAnalyzer;
import assets.freqanalysis.WorkProfile;
import assets.general.*;
import assets.spellcheck.EnhancedSpellCheck;
import org.clapper.util.misc.FileHashMap;
import org.clapper.util.misc.ObjectExistsException;
import org.clapper.util.misc.VersionMismatchException;
import org.languagetool.JLanguageTool;
import org.languagetool.language.AmericanEnglish;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class to handle the top-level GUI component
 * of StyleCheck and interface with other functionality.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
@SuppressWarnings("SameParameterValue")
public class SCGUI {

    // For global debugging purposes. Prints things if true
    public static final boolean DEBUG = true;
    public static final boolean DEBUG_FINE = false;

    // Dialog text
    private static final String APP_TITLE = "StyleCheck";

    // Default font for editor
    private static final int DEFAULT_EDITOR_FONT_SIZE = 14;
    private static final int DEFAULT_INFO_FONT_SIZE = 16;
    private static final Font DEFAULT_EDITOR_FONT
            = new Font(Font.SERIF, Font.PLAIN, DEFAULT_EDITOR_FONT_SIZE);
    private static final Font DEFAULT_INFO_FONT
            = new Font(Font.SERIF, Font.BOLD, DEFAULT_INFO_FONT_SIZE);

    // Recently opened files
    private static final String RECENTLY_OPENED_NAME_PREFIX
            = "src/files/recently_opened_name";
    private static final String RECENTLY_OPENED_TIME_PREFIX
            = "src/files/recently_opened_time";
    public static final int NUM_RECENT_FILES = 8;
    private FileHashMap<String, Long> recentlyOpenedByName;
    private FileHashMap<Long, String> recentlyOpenedByTime;

    // StyleCheck settings
    public static final int MAX_REPLACEMENTS = 3;      // Max number of total replacements

    // Essential GUI components
    private SCSplashScreen splashScreen;               // Splash screen
    private SCFrame frame;                             // Main window
    private JSplitPane splitPane;                      // The split pane component
    private JTextArea editorTextArea;                  // Text area for editor
    private JTextArea infoTextArea;                    // Text area for info panel
    private JOptionPane optionPane;                    // For creating dialog boxes

    // Split panel arrangements
    private static final double PANEL_WEIGHT = 0.72;

    // Colors for highlighting
    private static final int HIGHLIGHT_OPACITY = 160;
    private static final Color SPELLING_ERROR_COLOR
            = new Color(255, 0, 50, HIGHLIGHT_OPACITY);        // Red
    private static final Color GRAMMAR_ERROR_COLOR
            = new Color(0, 255, 50, HIGHLIGHT_OPACITY);        // Green
    private static final Color STYLE_ERROR_COLOR
            = new Color(0, 120, 255, HIGHLIGHT_OPACITY);       // Blue

    // GUI highlighting components
    private Highlighter editorHighlighter;
    private Highlighter infoHighlighter;
    private Highlighter.HighlightPainter spellingPainter;
    private Highlighter.HighlightPainter grammarPainter;
    private Highlighter.HighlightPainter stylePainter;
    private Highlighter.HighlightPainter infoRedPainter;
    private Highlighter.HighlightPainter infoGreenPainter;
    private Highlighter.HighlightPainter infoBluePainter;
    private boolean highlightsOn;

    // File management
    private SCFileManager fileManager;                 // File manager
    private File workFile;                             // Current work file

    // StyleCheck components
    private StatisticalAnalyzer analyzer;              // For statistical profiling
    private EnhancedSpellCheck spellCheck;             // Spell checking
    private AuthorIdentifier authorIdentifier;         // For finding similar authors
    private SynonymSearch synonymSearch;               // WordNet synonyms
    private WorkProfile documentProfile;               // This document's WorkProfile

    private Map<String, String> spellingIgnore;    // Words to ignore in spell check
    private HashMap<String, String> grammarIgnore;     // Grammar rules to ignore
    private Map<String, String> styleIgnore;       // Style rules to ignore

    // Error instances to ignore
    private Map<Integer, Integer> ignoredErrors;   // Individually ignored errors

    // Maps of errors to suggested replacements
    private HashMap<Integer, LinkedList<String>> spellingCorrections;
    private HashMap<Integer, LinkedList<String>> grammarCorrections;
    private HashMap<Integer, LinkedList<String>> styleCorrections;

    // Session information
    private String clipBoard;                          // Used for cut/copy/paste
    private boolean unsavedChanges;                    // Unsaved changes exist if true

    /**
     * Constructor initializes and starts GUI
     */
    private SCGUI() throws IOException, ObjectExistsException,
            VersionMismatchException, ClassNotFoundException {

        // Display a splash screen during loading
        showSplashScreen(SCSplashScreen.LOADING_TEXT);

        // Get recently opened files
        try {
            recentlyOpenedByName = new FileHashMap<>(RECENTLY_OPENED_NAME_PREFIX, 0);
            recentlyOpenedByTime = new FileHashMap<>(RECENTLY_OPENED_TIME_PREFIX, 0);
            recentlyOpenedByName.save();
            recentlyOpenedByTime.save();
        } catch (ObjectExistsException | ClassNotFoundException
                | VersionMismatchException | IOException e) {
            e.printStackTrace();
        }

        // Initialize all components
        initalizeGUIComponents();
        initializeStyleCheckComponents();

        // Make the GUI visible and destroy the splash screen
        splashScreen.closeSplashScreen();
        frame.setVisible(true);
    }

    /**
     * Create the splash screen for display while application loads
     */
    private void showSplashScreen(String message) {
        splashScreen = new SCSplashScreen(message);
    }

    /**
     * Close the recently opened files map before exiting
     */
    public void closeMaps() {
        try {
            recentlyOpenedByName.close();
            recentlyOpenedByTime.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Initialize the GUI components
     */
    private void initalizeGUIComponents() {
        frame = new SCFrame();                          // The main window
        SCMenu menu = new SCMenu(this);

        fileManager = new SCFileManager(this);          // File manager

        editorTextArea = initializeEditorTextArea();
        infoTextArea = initializeInfoTextArea();

        JScrollPane editorPane = initializeEditorPane(editorTextArea);
        JScrollPane infoPane = initializeInfoPane(infoTextArea);

        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editorPane, infoPane);
        splitPane.setResizeWeight(PANEL_WEIGHT);

        initializeHighlighters();                        // The editorHighlighter
        optionPane = new JOptionPane(APP_TITLE);        // For dialog boxes

        // Attach components together and to GUI
        frame.setJMenuBar(menu.getMenuBar());           // Menu bar
        frame.getContentPane().add(splitPane);          // Scroll pane

        // Initialize non-GUI session components
        clipBoard = "";
        unsavedChanges = false;
        workFile = null;
    }

    /**
     * Initialize the StyleCheck resources and components
     */
    private void initializeStyleCheckComponents() throws IOException, ObjectExistsException,
            VersionMismatchException, ClassNotFoundException {

        // Initialize sub-components tagger and language tool
        documentProfile = null;
        POSTagger tagger = new POSTagger();
        JLanguageTool languageTool = new JLanguageTool(new AmericanEnglish());
        languageTool.activateDefaultPatternRules();

        // Initialize ignore lists for errors
        ignoredErrors = new HashMap<>();
        spellingIgnore = new HashMap<>();
        grammarIgnore = new HashMap<>();
        styleIgnore = new HashMap<>();

        // Initialize spell check and multiple dictionaries
        spellCheck = new EnhancedSpellCheck(tagger, languageTool);

        // Initialize statistical analysis features
        analyzer = new StatisticalAnalyzer(tagger, languageTool);
        authorIdentifier = new AuthorIdentifier();
        synonymSearch = new SynonymSearch();

        // Set up error suggestion maps
        spellingCorrections = new HashMap<>();
        grammarCorrections = new HashMap<>();
        styleCorrections = new HashMap<>();
    }

    /**
     * Do the initial setup for the editor JScrollPane
     * @return the new scrollpane
     */
    private JScrollPane initializeEditorPane(JTextArea area) {
        JScrollPane sp = new JScrollPane(area,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
                JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        sp.setWheelScrollingEnabled(true);
        return sp;
    }

    /**
     * Do the initial setup for the JScrollPane
     * @return the new scrollpane
     */
    private JScrollPane initializeInfoPane(JTextArea area) {
        JScrollPane sp = new JScrollPane(area,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        sp.setWheelScrollingEnabled(true);
        return sp;
    }

    /**
     * Initialize the highlighting features
     */
    private void initializeHighlighters() {
        editorHighlighter = editorTextArea.getHighlighter();
        spellingPainter = new DefaultHighlighter
                .DefaultHighlightPainter(SPELLING_ERROR_COLOR);
        grammarPainter = new DefaultHighlighter
                .DefaultHighlightPainter(GRAMMAR_ERROR_COLOR);
        stylePainter = new DefaultHighlighter
                .DefaultHighlightPainter(STYLE_ERROR_COLOR);

        infoHighlighter = infoTextArea.getHighlighter();
        infoRedPainter = new DefaultHighlighter
                .DefaultHighlightPainter(SPELLING_ERROR_COLOR);
        infoGreenPainter = new DefaultHighlighter
                .DefaultHighlightPainter(GRAMMAR_ERROR_COLOR);
        infoBluePainter = new DefaultHighlighter
                .DefaultHighlightPainter(STYLE_ERROR_COLOR);

        highlightsOn = false;
    }

    /**
     * Create the non-editable text area for the side panel
     * @return the text area
     */
    private JTextArea initializeInfoTextArea() {
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(DEFAULT_INFO_FONT);
        area.setText(SCPanelSuggestions.TOO_SHORT_BLURB);
        return area;
    }

    /**
     * Do the initial setup for the JTextArea
     * @return the new JTextArea object
     */
    private JTextArea initializeEditorTextArea() {

        // Initialize and set attributes
        JTextArea area = new JTextArea();
        area.setEditable(true);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        area.setFont(DEFAULT_EDITOR_FONT);

        // Add a document listener to fire on text changes
        area.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                unsavedChanges = true;
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                unsavedChanges = true;
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                unsavedChanges = true;
            }
        });

        // Add a caret listener to fire on caret changes
        area.addCaretListener(e -> {
            if (highlightsOn && isCaretInError()) {
                String word = getSurroundingWord(editorTextArea.getCaretPosition());
                int errorPos = getStartOfWord(editorTextArea.getCaretPosition());

                // Clicked on a spelling error
                if (isCaretInSpellingError()) {
                    showReplacementDialog(word, spellingCorrections.get(errorPos), errorPos, false);

                // Clicked on a grammar error
                } else if (isCaretInGrammarError()) {
                    showReplacementDialog(word, grammarCorrections.get(errorPos), errorPos, false);

                // Clicked on a style error
                } else if (isCaretInStyleError()) {
                    showReplacementDialog(word, styleCorrections.get(errorPos), errorPos, true);

                } else {
                    throw new IllegalComponentStateException();
                }
            }
        });

        return area;
    }

    /**
     * Generate a replacement dialog with the appropriate listeners
     * and display it
     * @param word - the word to change
     * @param replacements - the replacements
     * @param errorPos - the error position
     * @param style - true if this dialog is for a style suggestion
     */
    private void showReplacementDialog(String word,
                                         Collection<String> replacements,
                                         int errorPos, boolean style) {

        String message;
        if (style) {
            message = "Changing "
                    + SCStringFormat.getQuotedString(word)
                    + " to one of the following may improve this sentence:";
        } else {
            message = "The text " + SCStringFormat.getQuotedString(word)
                    + " may contain an error. "
                    + "Please consider one of the following replacements.";
        }

        JDialog dialog = new JDialog(frame, APP_TITLE);
        JPanel dialogPane = new JPanel(new FlowLayout());
        dialog.setSize(650, 125);
        dialog.setLocation(editorTextArea.getCaret().getMagicCaretPosition());
        dialogPane.add(new JLabel(message));

        // Replacement buttons
        if (replacements != null && !replacements.isEmpty()) {
            for (String replacement : replacements) {
                JButton replaceButton = new JButton(SCStringFormat.getQuotedString(replacement));
                replaceButton.addActionListener(e -> {
                    doReplacement(word, replacement, errorPos);
                    rehighlight();
                    dialog.setVisible(false);
                });
                replaceButton.setVisible(true);
                dialogPane.add(replaceButton);
            }
        }

        // Ignore specific error button
        JButton ignoreButton = new JButton("Ignore This Error");
        ignoreButton.addActionListener(e -> {
            ignoredErrors.put(errorPos, errorPos);
            rehighlight();
            dialog.setVisible(false);
        });
        ignoreButton.setVisible(true);
        dialogPane.add(ignoreButton);

        // Ignore general error button
        JButton generalIgnore;
        if (style) {
            generalIgnore = new JButton("Ignore Style Rule");
            generalIgnore.addActionListener(e -> {
                ignoredErrors.put(errorPos, errorPos);
                ignoreStyleRule("TEMP");
                rehighlight();
                dialog.setVisible(false);
            });
        } else {
            if (spellingCorrections.containsKey(errorPos)) {
                generalIgnore = new JButton("Add to Dictionary");
                generalIgnore.addActionListener(e -> {
                    ignoredErrors.put(errorPos, errorPos);
                    ignoreSpellingOfWord(word);
                    rehighlight();
                    dialog.setVisible(false);
                });
            } else {
                generalIgnore = new JButton("Ignore Grammar Rule");
                generalIgnore.addActionListener(e -> {
                    ignoredErrors.put(errorPos, errorPos);
                    ignoreGrammarRule("TEMP");
                    rehighlight();
                    dialog.setVisible(false);
                });
            }
        }
        generalIgnore.setVisible(true);
        dialogPane.add(generalIgnore);

        // Set visible to user
        dialog.add(dialogPane);
        dialog.setVisible(true);
    }

    /**
     * Refresh the highlights
     */
    private void rehighlight() {
        unHighlightSpellingErrors();
        unHighlightGrammarErrors();
        unHighlightStyleErrors();
        highlightSpellingErrors();
        highlightGrammarErrors();
        highlightStyleErrors();
    }

    /**
     * Count the number of words in the document and display to the user
     * @return the word count
     */
    public int wordCountDocument(boolean print) {
        int wc = SCStringFormat.getAllWordsAndPositions(editorTextArea.getText()).size();

        if (print) {
            optionPane.setMessage("Word Count: " + wc);
            JDialog dialog = optionPane.createDialog(splitPane, APP_TITLE);
            dialog.setVisible(true);
        }

        return wc;
    }

    /**
     * Perform a replacement
     * @param word - the original word
     * @param replacement - the replacement word
     * @param errorPos - the error position
     */
    private void doReplacement(String word, String replacement, int errorPos) {
        unHighlightSpellingErrors();
        unHighlightGrammarErrors();
        unHighlightStyleErrors();

        // Remove from error lists
        if (spellingCorrections.containsKey(errorPos)) {
            spellingCorrections.remove(errorPos);
        }
        if (grammarCorrections.containsKey(errorPos)) {
            grammarCorrections.remove(errorPos);
        }
        if (styleCorrections.containsKey(errorPos)) {
            styleCorrections.remove(errorPos);
        }
        if (ignoredErrors.containsKey(errorPos)) {
            ignoredErrors.remove(errorPos);
        }

        // Adjust highlight and error positions
        adjustForReplacement(word, replacement, errorPos);

        // Replace the text
        editorTextArea.setText(editorTextArea.getText().substring(0, errorPos)
                + replacement + editorTextArea.getText().substring(errorPos
                + word.length(), editorTextArea.getText().length()));

        // Re-highlight other errors
        highlightSpellingErrors();
        highlightGrammarErrors();
        highlightStyleErrors();
    }

    /**
     * Adjust all error positions for a given replacement happening
     * @param word - the word to replace
     * @param replacement - the replacement word
     * @param errorPos - the position of the error in the document
     */
    private void adjustForReplacement(String word, String replacement, int errorPos) {
        adjustErrorPositions(SCStringFormat
                .getPositionAdjustment(word, replacement), errorPos);
    }

    /**
     * Adjust the positions of all errors after a given error
     * @param offset - the amount to adjust by (may be negative)
     * @param errorPos - the position of the error causing adjustment
     */
    private void adjustErrorPositions(int offset, int errorPos) {

        // Because these can't be modified concurrently
        Collection<Integer> adjustments = new LinkedList<>();
        LinkedList<String> currCorrections;

        // Spelling corrections
        adjustments.addAll(spellingCorrections.keySet().stream()
                .filter(pos -> pos > errorPos).collect(Collectors.toList()));
        for (Integer pos : adjustments) {
            currCorrections = spellingCorrections.get(pos);
            spellingCorrections.remove(pos);
            spellingCorrections.put(pos - offset, currCorrections);
        }

        // Grammar corrections
        adjustments.clear();
        adjustments.addAll(grammarCorrections.keySet().stream()
                .filter(pos -> pos > errorPos).collect(Collectors.toList()));
        for (Integer pos : adjustments) {
            currCorrections = grammarCorrections.get(pos);
            grammarCorrections.remove(pos);
            grammarCorrections.put(pos - offset, currCorrections);
        }

        // Style corrections
        adjustments.clear();
        adjustments.addAll(styleCorrections.keySet().stream()
                .filter(pos -> pos > errorPos).collect(Collectors.toList()));
        for (Integer pos : adjustments) {
            currCorrections = styleCorrections.get(pos);
            styleCorrections.remove(pos);
            styleCorrections.put(pos - offset, currCorrections);
        }

        // Ignored errors
        adjustments.clear();
        adjustments.addAll(ignoredErrors.keySet().stream()
                .filter(pos -> pos > errorPos).collect(Collectors.toList()));
        for (Integer pos : adjustments) {
            ignoredErrors.remove(pos);
            ignoredErrors.put(pos - offset, pos + offset);
        }
    }

    /**
     * Get the word surrounding a given position
     * @param position - the position
     * @return the word
     */
    String getSurroundingWord(int position) {
        if (position < 0 || position >= editorTextArea.getText().length()) {
            throw new IllegalArgumentException("Position out of range.");
        }

        int startPos = getStartOfWord(position);
        int endPos = SCStringFormat.getEndOfCurrentWord(
                editorTextArea.getText(), position);
        if (endPos <= startPos || endPos < 0 || startPos < 0) { return ""; }
        return editorTextArea.getText().substring(startPos, endPos).trim();
    }

    /**
     * Get the start position of a word from the middle
     * @param position - somewhere in the middle of a word
     * @return the start position
     */
    int getStartOfWord(int position) {
        if (SCGUI.DEBUG_FINE) {
            System.out.println("Got word at position: " + (Math.max(editorTextArea.getText()
                            .lastIndexOf(EnhancedSpellCheck.WORD_DELIM, position),
                            editorTextArea.getText().lastIndexOf(
                            EnhancedSpellCheck.SENTENCE_DELIM, position)) + 1));
        }
        int lastDelimPos = Math.max(editorTextArea.getText()
                        .lastIndexOf(EnhancedSpellCheck.WORD_DELIM, position),
                        editorTextArea.getText().lastIndexOf(
                        EnhancedSpellCheck.SENTENCE_DELIM, position));
        while (!Character.isLetterOrDigit(editorTextArea.getText().charAt(lastDelimPos))
                && lastDelimPos < editorTextArea.getText().length() - 1) {
            lastDelimPos++;
        }
        return lastDelimPos;
    }

    /**
     * Fill the text area with the given text
     * @param text - the text to populate the editor with
     */
    void fillEditor(String text) {
        editorTextArea.setText(text);
    }

    /**
     * Spell check the document
     */
    public void spellCheckDocument(boolean spellingOnly) throws IOException {
        spellingCorrections.clear();
        unHighlightSpellingErrors();
        grammarCorrections.clear();
        unHighlightGrammarErrors();

        spellingCorrections = spellCheck.checkSpelling(editorTextArea.getText());
        grammarCorrections = spellCheck.checkGrammar(
                editorTextArea.getText(), grammarIgnore);

        Iterable<Integer> overlap = grammarCorrections.keySet().stream()
                .filter(spellingCorrections::containsKey)
                .collect(Collectors.toCollection(LinkedList::new));
        overlap.forEach(grammarCorrections::remove);

        highlightSpellingErrors();
        highlightGrammarErrors();

        if (spellingOnly) {
            infoTextArea.setText(SCPanelSuggestions.getPanelSpellingGrammarString(
                    spellingCorrections.size(), grammarCorrections.size()));
        }
        highlightInfoPanel();
    }

    /**
     * Reset all error info
     */
    void resetErrorInfo() {
        editorHighlighter.removeAllHighlights();
        unhighlightInfoPanel();
        highlightsOn = false;

        spellingCorrections.clear();
        grammarCorrections.clear();
        styleCorrections.clear();

        spellingIgnore.clear();
        grammarIgnore.clear();
        styleIgnore.clear();

        infoTextArea.setText(SCPanelSuggestions.TOO_SHORT_BLURB);
        documentProfile = null;
    }

    /**
     * Simple check for whether a style rule has been ignored
     * @param ruleID - the rule identifier
     * @return true if the rule is ignored
     */
    boolean isStyleRuleIgnored(String ruleID) {
        return styleIgnore.containsKey(ruleID);
    }

    /**
     * Add the highlights to the info panel
     */
    public void highlightInfoPanel() {
        unhighlightInfoPanel();
        int locations[] = SCPanelSuggestions
                .getHighlightPositions(infoTextArea.getText());

        // Red
        if (locations[0] >= 0) {
            try {
                infoHighlighter.addHighlight(locations[0],
                        locations[0] + SCPanelSuggestions.RED_HIGHLIGHT.length(),
                        infoRedPainter);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        // Green
        if (locations[1] >= 0) {
            try {
                infoHighlighter.addHighlight(locations[1],
                        locations[1] + SCPanelSuggestions.GREEN_HIGHLIGHT.length(),
                        infoGreenPainter);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }

        // Blue
        if (locations[2] >= 0) {
            try {
                infoHighlighter.addHighlight(locations[2],
                        locations[2] + SCPanelSuggestions.BLUE_HIGHLIGHT.length(),
                        infoBluePainter);
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Remove the highlights from the info panel
     */
    void unhighlightInfoPanel() {
        infoHighlighter.removeAllHighlights();
    }

    /**
     * Highlight the given spelling errors
     */
    void highlightSpellingErrors() {
        unHighlightSpellingErrors();
        highlightsOn = true;
        for (Integer spellingPos : spellingCorrections.keySet()) {
            int endPos = SCStringFormat.getEndOfCurrentWord(
                    editorTextArea.getText(), spellingPos);
            if (endPos == -1) { continue; }
            String word = editorTextArea.getText().substring(spellingPos, endPos);
            if (!spellCheck.getDict().isUserWord(word)
                    && !spellingIgnore.containsKey(word)
                    && !ignoredErrors.containsKey(spellingPos)) {
                try {
                    editorHighlighter.addHighlight(spellingPos,
                            endPos, spellingPainter);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Highlight the given grammar errors
     */
    void highlightGrammarErrors() {
        unHighlightGrammarErrors();
        highlightsOn = true;
        for (Integer grammarPos : grammarCorrections.keySet()) {
            if (!ignoredErrors.containsKey(grammarPos)) {
                int endPos = editorTextArea.getText().indexOf(
                        EnhancedSpellCheck.WORD_DELIM, grammarPos);
                try {
                    editorHighlighter.addHighlight(grammarPos,
                            endPos + 1, grammarPainter);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Highlight the given style errors
     */
    void highlightStyleErrors() {
        unHighlightStyleErrors();
        highlightsOn = true;
        for (Integer stylePos : styleCorrections.keySet()) {
            if (!ignoredErrors.containsKey(stylePos)) {
                int endPos = editorTextArea.getText().indexOf(
                        EnhancedSpellCheck.WORD_DELIM, stylePos);
                try {
                    editorHighlighter.addHighlight(stylePos,
                            endPos, stylePainter);
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Remove the highlights from spelling errors
     */
    void unHighlightSpellingErrors() {
        for (Highlighter.Highlight highlight : editorHighlighter.getHighlights()) {
            if (((DefaultHighlighter.DefaultHighlightPainter) highlight
                    .getPainter()).getColor().equals(SPELLING_ERROR_COLOR)) {
                editorHighlighter.removeHighlight(highlight);
            }
        }
        if (editorHighlighter.getHighlights().length == 0) {
            highlightsOn = false;
        }
    }

    /**
     * Remove the highlights from grammar errors
     */
    void unHighlightGrammarErrors() {
        for (Highlighter.Highlight highlight : editorHighlighter.getHighlights()) {
            if (((DefaultHighlighter.DefaultHighlightPainter) highlight
                    .getPainter()).getColor().equals(GRAMMAR_ERROR_COLOR)) {
                editorHighlighter.removeHighlight(highlight);
            }
        }
        if (editorHighlighter.getHighlights().length == 0) {
            highlightsOn = false;
        }
    }

    /**
     * Remove the highlights from style errors
     */
    void unHighlightStyleErrors() {
        for (Highlighter.Highlight highlight : editorHighlighter.getHighlights()) {
            if (((DefaultHighlighter.DefaultHighlightPainter) highlight
                    .getPainter()).getColor().equals(STYLE_ERROR_COLOR)) {
                editorHighlighter.removeHighlight(highlight);
            }
        }
        if (editorHighlighter.getHighlights().length == 0) {
            highlightsOn = false;
        }
    }

    /**
     * Check if caret is under any error
     * @return true if the caret is currently positioned in an error
     */
    boolean isCaretInError() {
        return isCaretInSpellingError()
                || isCaretInGrammarError()
                || isCaretInStyleError();
    }

    /**
     * Check if caret is under a spelling error
     * @return true if so
     */
    boolean isCaretInSpellingError() {
        for (Integer wordPos : spellingCorrections.keySet()) {
            if (editorTextArea.getCaretPosition() >= wordPos && editorTextArea.getCaretPosition()
                    < Math.min(editorTextArea.getText().indexOf(EnhancedSpellCheck.WORD_DELIM, wordPos),
                    editorTextArea.getText().indexOf(EnhancedSpellCheck.SENTENCE_DELIM, wordPos))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if caret is under a grammar error
     * @return true if so
     */
    boolean isCaretInGrammarError() {
        for (Integer wordPos : grammarCorrections.keySet()) {
            if (editorTextArea.getCaretPosition() >= wordPos && editorTextArea.getCaretPosition()
                    <= Math.min(editorTextArea.getText().indexOf(EnhancedSpellCheck.WORD_DELIM, wordPos),
                    editorTextArea.getText().indexOf(EnhancedSpellCheck.SENTENCE_DELIM, wordPos))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check if caret is under a style error
     * @return true if so
     */
    boolean isCaretInStyleError() {
        for (Integer wordPos : styleCorrections.keySet()) {
            if (editorTextArea.getCaretPosition() >= wordPos && editorTextArea.getCaretPosition()
                    < Math.min(editorTextArea.getText().indexOf(EnhancedSpellCheck.WORD_DELIM, wordPos),
                    editorTextArea.getText().indexOf(EnhancedSpellCheck.SENTENCE_DELIM, wordPos))) {
                return true;
            }
        }
        return false;
    }

    /**
     * Ignore the spelling of this word
     * @param word - the word to ignore
     */
    public void ignoreSpellingOfWord(String word) {
        if (!spellingIgnore.containsKey(word)) {
            spellingIgnore.put(word, word);
        }
    }

    /**
     * Ignore this grammar rule
     * @param ruleID - the rule ID to ignore
     */
    public void ignoreGrammarRule(String ruleID) {
        if (!grammarIgnore.containsKey(ruleID)) {
            grammarIgnore.put(ruleID, ruleID);
        }
    }

    /**
     * Ignore this style rule
     * @param rule - the word to ignore
     */
    public void ignoreStyleRule(String rule) {
        if (StyleRules.isRule(rule) &&
                !styleIgnore.containsKey(rule)) {
            styleIgnore.put(rule, rule);
        }
    }

    /**
     * Stylecheck the document and highlight errors
     */
    public void styleCheckDocument(boolean styleOnly) {
        analyzer.profiler.clear();
        analyzer.profiler.analyzeSample(editorTextArea.getText());

        if (DEBUG) {
            analyzer.profiler.printAnalysis();
        }

        LinkedList<SCHighlightedWord> styleErrors = new LinkedList<>();
        if (DEBUG) { System.out.println("Done Analyzing! Getting synonyms..."); }

        // Overused adverbs
        LinkedList<SCHighlightedWord> overAdv = new LinkedList<>();
        for (String adv : analyzer.profiler.getOverusedAdverbs()) {
            overAdv.addAll(getOccurrencePositions(editorTextArea.getText(), adv)
                    .stream().map(loc -> new SCHighlightedWord(
                            adv, loc, StyleRules.OVER_ADV)).collect(Collectors.toList()));
        }
        styleErrors.addAll(overAdv);

        if (DEBUG) { System.out.println("Got " + overAdv.size() + " adverbs!"); }

        // Overused verbs
        LinkedList<SCHighlightedWord> overV = new LinkedList<>();
        for (String v : analyzer.profiler.getOverusedVerbs()) {
            overAdv.addAll(getOccurrencePositions(editorTextArea.getText(), v)
                    .stream().map(loc -> new SCHighlightedWord(
                            v, loc, StyleRules.OVER_V)).collect(Collectors.toList()));
        }
        styleErrors.addAll(overV);

        if (DEBUG) { System.out.println("Got " + overV.size() + " verbs!"); }

        // Overused adjectives
        LinkedList<SCHighlightedWord> overAdj = new LinkedList<>();
        for (String adj : analyzer.profiler.getOverusedAdjectives()) {
            overAdv.addAll(getOccurrencePositions(editorTextArea.getText(), adj)
                    .stream().map(loc -> new SCHighlightedWord(
                            adj, loc, StyleRules.OVER_ADJ)).collect(Collectors.toList()));
        }
        styleErrors.addAll(overAdj);

        if (DEBUG) { System.out.println("Got " + overAdj.size() + " adjectives!"); }

        // Overused nouns
        LinkedList<SCHighlightedWord> overN = new LinkedList<>();
        for (String n : analyzer.profiler.getOverusedNouns()) {
            overAdv.addAll(getOccurrencePositions(editorTextArea.getText(), n)
                    .stream().map(loc -> new SCHighlightedWord(
                            n, loc, StyleRules.OVER_N)).collect(Collectors.toList()));
        }
        styleErrors.addAll(overN);

        if (DEBUG) { System.out.println("Got " + overN.size() + " nouns!"); }

        // Number words
        for (String numberWord : analyzer.profiler.getNumberWords()) {
            styleErrors.addAll(getOccurrencePositions(editorTextArea.getText(), numberWord)
                    .stream().map(pos -> new SCHighlightedWord(numberWord, pos, StyleRules.NUMBER_WORD))
                    .collect(Collectors.toList()));
        }

        if (DEBUG_FINE) { System.out.println("Eliminating ignored rules..."); }

        // Eliminate any ignored errors and place in suggestion dictionary
        styleErrors.stream().filter(word -> !StyleRules.isRule(word.getStyleRule())
                || styleIgnore.containsKey(word.getStyleRule())).forEach(styleErrors::remove);

        styleCorrections.clear();
        styleCorrections.putAll(getSynonymsForStyleErrors(styleErrors));
        styleCorrections.putAll(getNumberWordReplacements(styleErrors));

        // Eliminate any with no corrections
        Iterable<Integer> remove = styleCorrections.keySet().stream()
                .filter(pos -> styleCorrections.get(pos).isEmpty())
                .collect(Collectors.toCollection(LinkedList::new));
        remove.forEach(styleCorrections::remove);

        if (DEBUG) { System.out.println("Got replacements, now highlighting!"); }

        highlightStyleErrors();

        documentProfile = analyzer.profiler.generateProfile();

        if (styleOnly) {
            infoTextArea.setText(documentProfile.getSCPanelString(wordCountDocument(false)));
        } else {
            infoTextArea.setText(SCPanelSuggestions.getPanelSpellingGrammarString(
                    spellingCorrections.size(), grammarCorrections.size())
                    + documentProfile.getSCPanelString(wordCountDocument(false)));
        }
        highlightInfoPanel();
    }

    /**
     * Get all the positions in text where word occurs
     * @param text - the text
     * @param word - the word
     * @return the list of positions
     */
    Collection<Integer> getOccurrencePositions(String text, String word) {
        LinkedList<Integer> posList = new LinkedList<>();
        int currPos = 0;
        int nextPos;

        // Get all occurrences
        while (currPos + word.length() < text.length()) {
            nextPos = text.indexOf(word, currPos);
            if (nextPos == -1) { break; }

            posList.add(nextPos);
            currPos = nextPos + word.length();
        }

        if (DEBUG_FINE) {
            System.out.print("Found positions: ");
            for (Integer pos : posList) {
                System.out.print(pos);
                if (!posList.getLast().equals(pos)) {
                    System.out.print(", ");
                }
            }
            System.out.print("\n");
        }

        return posList;
    }

    /**
     * Get the phrase replacement for number words
     * @param styleErrors - the style error list
     * @return the map of words to replacements
     */
    Map<Integer, LinkedList<String>> getNumberWordReplacements(
            Collection<SCHighlightedWord> styleErrors) {
        HashMap<Integer, LinkedList<String>> numberMap = new HashMap<>();

        // Get all the style errors and generate replacements for numbers
        styleErrors.stream().filter(word -> word.getStyleRule().equals(StyleRules.NUMBER_WORD))
                .forEach(word -> numberMap.put(word.getStartPosition(),
                new LinkedList<>(Arrays.asList(EnglishNumberToWords
                        .convert(Long.valueOf(word.getWord())),
                        SCStringFormat.formatNumberString(word.getWord())))));

        // Remove any suggested formats that are the same as the original
        numberMap.keySet().stream().filter(pos -> numberMap.get(pos).size() == 2).forEach(pos -> {
            if (numberMap.get(pos).get(1).isEmpty()) {
                numberMap.get(pos).removeLast();
            }
        });
        return numberMap;
    }

    /**
     * Get all of the synonym suggestions for style errors
     * @param styleErrors - the style error list
     * @return the hashmap of error positions to suggestions
     */
    Map<Integer, LinkedList<String>> getSynonymsForStyleErrors(
            Collection<SCHighlightedWord> styleErrors) {

        HashMap<Integer, LinkedList<String>> styleErrorMap = new HashMap<>();

        // Iterate over style errors and filter for synonym errors
        styleErrors.stream().filter(styleError -> !isStyleRuleIgnored(styleError.getStyleRule())
                && StyleRules.requiresSynonyms(styleError.getStyleRule())
                && !styleErrorMap.containsKey(styleError.getStartPosition()))
                .forEach(styleError -> styleErrorMap.put(styleError.getStartPosition(),
                        getSynonymsForStyleError(styleError)));

        return styleErrorMap;
    }

    /**
     * Get the synonym suggestions for a single style error
     * @param styleError - the style error
     * @return the list of synonym suggestions
     */
    LinkedList<String> getSynonymsForStyleError(SCHighlightedWord styleError) {
        LinkedList<String> window = spellCheck.getWindow(editorTextArea.getText(),
                styleError.getWord());
        if (window.indexOf(styleError.getWord()) == -1) {
            if (DEBUG_FINE) { System.out.println("Word not in window!"); }
            return new LinkedList<>();
        }

        // Get POS information and locate correct word-tag pair
        LinkedList<String[]> taggedSentence = spellCheck.getTagger()
                .getSentenceWordsAndTags(spellCheck.getCurrentSentence(
                        editorTextArea.getText(), styleError.getStartPosition()));
        int foundIndex = 0;
        for (String tagged[] : taggedSentence) {
            if (tagged[0].equals(styleError.getWord())) {
                foundIndex = taggedSentence.indexOf(tagged);
            }
        }

        // Find valid replacements using the list generated from synonyms
        if (taggedSentence.isEmpty()) { return new LinkedList<>(); }

        // If the first word in a sentence
        if (SCStringFormat.isFirstWordInSentence(editorTextArea.getText(),
                styleError.getStartPosition())) {
            return SCStringFormat.capitalizeWords(spellCheck
                    .getValidReplacements(window, styleError.getWord(),
                            synonymSearch.getSynonyms(taggedSentence.get(foundIndex))));
        }

        // If not the first word
        return spellCheck.getValidReplacements(window, styleError.getWord(),
                synonymSearch.getSynonyms(taggedSentence.get(foundIndex)));
    }

    /**
     * Build a language model of the document
     */
    public void modelDocument() {
        analyzer.identifier.analyzeSample(editorTextArea.getText(), "User");
    }

    /**
     * Identify the closest author to the style
     */
    public void findClosestAuthor() {
        if (documentProfile == null || unsavedChanges) {
            saveDocument();
            profileDocument();
        }
        String author = authorIdentifier.getClosestAuthor(documentProfile);
        optionPane.setMessage(author + " has the most similar style to yours.");
        JDialog dialog = optionPane.createDialog(editorTextArea, APP_TITLE);
        dialog.setVisible(true);
    }

    /**
     * Profile the document
     */
    public void profileDocument() {
        analyzer.profiler.clear();
        analyzer.profiler.analyzeSample(editorTextArea.getText());
        documentProfile = analyzer.profiler.generateProfile();
    }

    /**
     * Set the editor to a new document for writing
     */
    public void newDocument() {
        resetErrorInfo();
        workFile = null;
        fillEditor("");
    }

    /**
     * Save a document in the editor
     */
    public void saveDocument() {
        if (unsavedChanges) {
            workFile = fileManager.saveToFile();
        }
    }

    /**
     * Open a document in the editor
     */
    public void openDocument() {
        File openFile = fileManager.openFromFile();
        resetErrorInfo();
        if (openFile != null) {
            try (BufferedReader reader = new BufferedReader(new FileReader(openFile))) {
                editorTextArea.read(reader, openFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Add to recently opened files
        addFileToMostRecentList(openFile);
        workFile = openFile;
    }

    /**
     * Add a file to the most recently opened list
     * @param openFile - the file to add
     */
    public void addFileToMostRecentList(File openFile) {
        if (openFile != null) {
            String path = openFile.getPath();
            long time = System.currentTimeMillis();
            if (!recentlyOpenedByName.containsKey(openFile.getPath())) {
                recentlyOpenedByName.put(path, time);
                recentlyOpenedByTime.put(time, path);
            } else {
                recentlyOpenedByName.replace(path, time);
                recentlyOpenedByTime.replace(time, path);
            }
            try {
                recentlyOpenedByName.save();
                recentlyOpenedByTime.save();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get the N most recently opened files
     * @param n - the number to get
     * @return a LinkedList of recently open file paths
     */
    public LinkedList<String> getNMostRecentlyOpened(int n) {
        List<Long> timeStamps = new LinkedList<>(recentlyOpenedByTime.keySet());
        Collections.sort(timeStamps, Collections.reverseOrder());

        LinkedList<String> recentFiles = new LinkedList<>();
        for (Long timeStamp : timeStamps.subList(0, Math.min(recentlyOpenedByTime.size(), n))) {
            if (recentlyOpenedByTime.containsKey(timeStamp)) {
                recentFiles.add(recentlyOpenedByTime.get(timeStamp));
            }
        }
        return recentFiles;
    }

    /**
     * Get the N mostly recently opened filenames
     * @param n - the number to get
     * @return the list of recently opened names
     */
    public LinkedList<String> getNMostRecentlyOpenedNames(int n) {
        Collection<String> recentPaths = getNMostRecentlyOpened(n);
        return recentPaths.stream()
                .map(recent -> recent.substring(recent.lastIndexOf('/')))
                .collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Open a document in the editor using the file path
     * @param path - the file path
     */
    public void openFromPath(String path) {
        resetErrorInfo();
        File openFile;
        try {
            openFile = new File(path);
            BufferedReader reader = new BufferedReader(new FileReader(openFile));
            editorTextArea.read(reader, openFile);
            addFileToMostRecentList(openFile);
            workFile = openFile;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Getter for recently opened files
     * @return the recently opened file map
     */
    public FileHashMap<String, Long> getRecentlyOpenedByName() { return recentlyOpenedByName; }

    /**
     * Setter for unsaved changes
     * @param val - the boolean value
     */
    public void setUnsavedChanges(boolean val) {
        unsavedChanges = val;
    }

    /**
     * Make the editor display font larger
     */
    public void makeFontLarger() {
        editorTextArea.setFont(new Font(Font.SERIF, Font.BOLD,
                editorTextArea.getFont().getSize() + 2));
    }

    /**
     * Make the editor display font smaller
     */
    public void makeFontSmaller() {
        editorTextArea.setFont(new Font(Font.SERIF, Font.BOLD,
                editorTextArea.getFont().getSize() - 2));
    }

    /**
     * Select all document text
     */
    public void selectAll() { editorTextArea.selectAll(); }

    /**
     * Cut selected text
     */
    public void cut() {
        copy();
        editorTextArea.replaceRange("", editorTextArea.getSelectionStart(),
                editorTextArea.getSelectionEnd());
    }

    /**
     * Copy selected text
     */
    public void copy() {
        clipBoard = editorTextArea.getSelectedText();
    }

    /**
     * Paste from clipboard at current position
     */
    public void paste() {
        editorTextArea.insert(clipBoard, editorTextArea.getCaretPosition());
    }

    /**
     * Get the work file
     * @return the work file
     */
    public File getWorkFile() { return workFile; }

    /**
     * Getter for the frame
     * @return the SCFrame object
     */
    public SCFrame getFrame() { return frame; }

    /**
     * Get the text currently held in the editor
     * @return the text
     */
    public String getEditorText() { return editorTextArea.getText(); }

    /**
     * Test method
     * @param args
     */
    public static void main(String... args) throws ClassNotFoundException,
            VersionMismatchException, ObjectExistsException, IOException {

        SCGUI gui = new SCGUI();
    }

}
