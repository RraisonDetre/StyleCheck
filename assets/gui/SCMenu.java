package assets.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * Class to implement the menus for the GUI.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
class SCMenu {

    // Menu titles for the menu bar (always visible)
    private static final String FILE_MENU_TITLE = "File";
    private static final String EDIT_MENU_TITLE = "Edit";
    private static final String VIEW_MENU_TITLE = "View";
    private static final String ANALYZE_MENU_TITLE = "Analyze";
    private static final String MENU_TITLES[] = {
            FILE_MENU_TITLE, EDIT_MENU_TITLE,
            VIEW_MENU_TITLE, ANALYZE_MENU_TITLE };

    // Menu options under "File"
    private static final String NEW_OPTION_TITLE = "New";
    private static final String OPEN_OPTION_TITLE = "Open";
    private static final String OPEN_RECENT_TITLE = "Open Recent";
    private static final String SAVE_OPTION_TITLE = "Save";
    private static final String EXIT_OPTION_TITLE = "Exit";
    private static final String FILE_MENU_ITEMS[] = {
            NEW_OPTION_TITLE, OPEN_OPTION_TITLE, OPEN_RECENT_TITLE,
            SAVE_OPTION_TITLE, EXIT_OPTION_TITLE };

    // Menu options under "Edit"
    private static final String SELECT_OPTION_TITLE = "Select All";
    private static final String CUT_OPTION_TITLE = "Cut";
    private static final String COPY_OPTION_TITLE = "Copy";
    private static final String PASTE_OPTION_TITLE = "Paste";
    private static final String EDIT_MENU_ITEMS[] = {
            SELECT_OPTION_TITLE, CUT_OPTION_TITLE,
            COPY_OPTION_TITLE, PASTE_OPTION_TITLE };

    // Menu options under "View"
    private static final String MIN_OPTION_TITLE = "Minimize Window";
    private static final String MAX_OPTION_TITLE = "Maximize Window";
    private static final String SIZE_OPTION_TITLE = "Default Size Window";
    private static final String LARGE_OPTION_TITLE = "Larger Font";
    private static final String SMALL_OPTION_TITLE = "Smaller Font";
    private static final String VIEW_MENU_ITEMS[] = {
            MIN_OPTION_TITLE, MAX_OPTION_TITLE,
            SIZE_OPTION_TITLE, LARGE_OPTION_TITLE, SMALL_OPTION_TITLE };

    // Menu options under "Analyze"
    private static final String WORD_COUNT_TITLE = "Word Count Document";
    private static final String SPELL_OPTION_TITLE = "Spell/Grammar Check Document";
    private static final String STYLE_OPTION_TITLE = "Analyze Style";
    private static final String BOTH_OPTION_TITLE = "Show All Suggestions";
    private static final String AUTHOR_OPTION_TITLE = "Find Closest Author";
    private static final String ANALYZE_MENU_ITEMS[] = {
            WORD_COUNT_TITLE, SPELL_OPTION_TITLE, STYLE_OPTION_TITLE,
            BOTH_OPTION_TITLE, AUTHOR_OPTION_TITLE };

    // All menu options together for convenience
    private static final String ALL_MENU_ITEMS[][] = {
            FILE_MENU_ITEMS, EDIT_MENU_ITEMS,
            VIEW_MENU_ITEMS, ANALYZE_MENU_ITEMS };

    // GUI components
    private final SCGUI gui;
    private final JMenuBar menuBar;
    private final SCMenuActionListener actionListener;

    // Recently opened
    private List<String> recentlyOpenedPaths;
    private java.util.List<String> recentlyOpenedNames;

    /**
     * Construct a new menu
     * @param g - the gui
     */
    public SCMenu(SCGUI g) {
        gui = g;
        actionListener = new SCMenuActionListener();
        menuBar = initializeMenuBar();
    }

    /**
     * Set up the menu bar and return it completed
     * @return the completed menu bar
     */
    private JMenuBar initializeMenuBar() {
        JMenuBar bar = new JMenuBar();
        bar.setBackground(Color.LIGHT_GRAY);

        // Initialize all menus
        for (int i = 0; i < MENU_TITLES.length; i++) {
            JMenu currentMenu = new JMenu(MENU_TITLES[i]);
            bar.add(currentMenu);

            // Initialize all menu items and listeners for this menu
            for (int j = 0; j < ALL_MENU_ITEMS[i].length; j++) {

                // The open recent menu
                if (i == 0 && j == 2) {
                    currentMenu.add(createOpenRecentMenu());
                    currentMenu.addSeparator();
                }

                // Everything else
                else {
                    JMenuItem currentItem = new JMenuItem(ALL_MENU_ITEMS[i][j]);
                    currentMenu.add(currentItem);
                    if (j < ALL_MENU_ITEMS[i].length - 1) {
                        currentMenu.addSeparator();
                    }
                    currentItem.addActionListener(actionListener);

                    // Set up keyboard shortcuts
                    switch (ALL_MENU_ITEMS[i][j]) {

                        case NEW_OPTION_TITLE:
                            currentItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK));
                            break;

                        case SAVE_OPTION_TITLE:
                            currentItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
                            break;

                        case OPEN_OPTION_TITLE:
                            currentItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
                            break;

                        case SELECT_OPTION_TITLE:
                            currentItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A, InputEvent.CTRL_MASK));
                            break;

                        case CUT_OPTION_TITLE:
                            currentItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, InputEvent.CTRL_MASK));
                            break;

                        case COPY_OPTION_TITLE:
                            currentItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, InputEvent.CTRL_MASK));
                            break;

                        case PASTE_OPTION_TITLE:
                            currentItem.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, InputEvent.CTRL_MASK));
                            break;

                        // Actions with no shortcut
                        default:
                            break;
                    }
                }
            }
        }

        return bar;
    }

    /**
     * Create the "open recent" menu
     * @return the open recent menu
     */
    private JMenu createOpenRecentMenu() {
        JMenu recentMenu = new JMenu(OPEN_RECENT_TITLE);
        recentlyOpenedPaths = gui.getNMostRecentlyOpened(SCGUI.NUM_RECENT_FILES);
        recentlyOpenedNames = gui.getNMostRecentlyOpenedNames(SCGUI.NUM_RECENT_FILES);
        for (String name : recentlyOpenedNames) {
            JMenuItem current = new JMenuItem(name);
            current.addActionListener(actionListener);
            recentMenu.add(current);
        }
        return recentMenu;
    }

    /**
     * Getter for the menu bar component
     * @return the menu bar
     */
    public JMenuBar getMenuBar() { return menuBar; }

    /**
     * Listener class for menu buttons
     */
    private class SCMenuActionListener implements ActionListener {

        /**
         * Action handler method for all menu items
         * @param e - the actionevent
         */
        @Override
        public void actionPerformed(ActionEvent e) {

            // Identify the menu item clicked and perform the action
            String itemTitle = e.getActionCommand();
            switch (itemTitle) {

                // FILE MENU
                case NEW_OPTION_TITLE:      // "New"
                    gui.newDocument();
                    break;

                case OPEN_OPTION_TITLE:     // "Open"
                    gui.openDocument();
                    break;

                case SAVE_OPTION_TITLE:     // "Save"
                    gui.saveDocument();
                    break;

                case EXIT_OPTION_TITLE:     // "Exit"
                    gui.closeMaps();
                    System.exit(0);
                    break;

                // EDIT MENU
                case SELECT_OPTION_TITLE:   // "Select All"
                    gui.selectAll();
                    break;

                case CUT_OPTION_TITLE:      // "Cut"
                    gui.cut();
                    break;

                case COPY_OPTION_TITLE:     // "Copy"
                    gui.copy();
                    break;

                case PASTE_OPTION_TITLE:    // "Paste"
                    gui.paste();
                    break;

                // VIEW MENU
                case MIN_OPTION_TITLE:      // "Minimize Window"
                    gui.getFrame().minimizeWindow();
                    break;

                case MAX_OPTION_TITLE:      // "Maximize Window"
                    gui.getFrame().maximizeWindow();
                    break;

                case SIZE_OPTION_TITLE:     // "Default Size Window"
                    gui.getFrame().setToDefaultSize();
                    break;

                case LARGE_OPTION_TITLE:    // "Larger Font"
                    gui.makeFontLarger();
                    break;

                case SMALL_OPTION_TITLE:    // "Smaller Font"
                    gui.makeFontSmaller();
                    break;

                // ANALYZE MENU
                case WORD_COUNT_TITLE:       // "Word Count Document"
                    gui.wordCountDocument(true);
                    break;

                case SPELL_OPTION_TITLE:     // "Spell/Grammar Check Document"
                    try {
                        gui.spellCheckDocument(true);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;

                case STYLE_OPTION_TITLE:     // "Analyze Style"
                    gui.styleCheckDocument(true);
                    break;

                case BOTH_OPTION_TITLE:      // "Show All Suggestions"
                    try {
                        gui.spellCheckDocument(false);
                        gui.styleCheckDocument(false);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    break;

                case AUTHOR_OPTION_TITLE:    // "Find Closest Author"
                    gui.findClosestAuthor();
                    break;

                // Open a recent file
                default:
                    gui.openFromPath(recentlyOpenedPaths
                            .get(recentlyOpenedNames.indexOf(itemTitle)));
                    break;
            }

        }
    }

}
