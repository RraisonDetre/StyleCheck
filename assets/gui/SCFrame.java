package assets.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Class to provide a predefined JFrame for the GUI.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class SCFrame extends JFrame {

    private static final int GUI_DEFAULT_WIDTH = 1100;
    private static final int GUI_DEFAULT_HEIGHT = 850;

    private static final int WINDOW_START_X = 80;
    private static final int WINDOW_START_Y = 0;

    private static final String GUI_TITLE = "StyleCheck Text Editor";

    /**
     * Set up the main window
     */
    public SCFrame() {

        // Set the title, size, and initial position
        setTitle(GUI_TITLE);
        setToDefaultSize();

        // Exit program on window close
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // General set up
        getContentPane().setLayout(new BorderLayout());
        setResizable(true);
    }

    /**
     * Set the window to the default size
     */
    public void setToDefaultSize() {
        setSize(GUI_DEFAULT_WIDTH, GUI_DEFAULT_HEIGHT);
        setLocation(WINDOW_START_X, WINDOW_START_Y);
    }

    /**
     * Maximize the StyleCheck window
     */
    public void maximizeWindow() {
        setExtendedState(getExtendedState() | JFrame.MAXIMIZED_BOTH);
        pack();
    }

    /**
     * Minimize the StyleCheck window
     */
    public void minimizeWindow() {
        setState(JFrame.ICONIFIED);
    }

    /**
     * Test method
     * @param args
     */
    public static void main(String... args) {
        SCFrame testFrame = new SCFrame();
    }

}
