package assets.gui;

import javax.swing.*;
import java.awt.*;

/**
 * Class to display a splash screen while StyleCheck
 * components load in the GUI.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
@SuppressWarnings("UnusedAssignment")
class SCSplashScreen extends JWindow {

    public static final String LOADING_TEXT = "Welcome to StyleCheck. Loading resources...";
    public static final String SPELLING_TEXT = "Checking text for spelling/grammar errors...";
    public static final String STYLE_TEXT = "Checking document for style suggestions...";

    private static final int SPLASH_WIDTH = 300;
    private static final int SPLASH_HEIGHT = 120;

    /**
     * Constructor to build the splash screen
     */
    public SCSplashScreen(String message) {

        // Set size and location
        setSize(SPLASH_WIDTH, SPLASH_HEIGHT);
        Dimension screenSize =
                Toolkit.getDefaultToolkit().getScreenSize();
        setLocation((screenSize.width / 2) - (SPLASH_WIDTH / 2),
                (screenSize.height / 2) - (SPLASH_HEIGHT / 2));

        // Add the text box
        getContentPane().add(initializeTextField(message),
                BorderLayout.CENTER);

        // Make the splash screen visible
        toFront();
        setVisible(true);
    }

    /**
     * Close the splash screen and free resources
     */
    public void closeSplashScreen() {
        setVisible(false);
        dispose();
    }

    /**
     * Initialize the text field
     * @return the text field
     */
    private JTextField initializeTextField(String message) {
        JTextField tf = new JTextField(message);
        tf.setEditable(false);
        tf.setVisible(true);
        return tf;
    }

}
