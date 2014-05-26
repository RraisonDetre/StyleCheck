package assets.gui;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Class to encapsulate the file managing in the GUI.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
class SCFileManager {

    private static final String TXT_EXTENSION = ".txt";   // For file validation

    private final SCGUI gui;                                   // The main GUI component

    /**
     * Initialize the file manager
     */
    public SCFileManager(SCGUI g) {
        gui = g;
    }

    /**
     * Open an existing file
     * @return the opened file
     */
    public File openFromFile() {
        return getFileFromChooser(false);
    }

    /**
     * Save any unsaved changes to file
     */
    public File saveToFile() {
        BufferedWriter writer = null;
        File saveFile;

        // Get the file to save to
        if (gui.getWorkFile() == null) {
            saveFile = gui.getWorkFile();
        } else {
            saveFile = getFileFromChooser(true);
        }

        // Save data to file
        if (saveFile != null) {
            try {
                writer = new BufferedWriter(
                        new FileWriter(saveFile, false));
                writer.write(gui.getEditorText());
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // Mark file as saved
        gui.setUnsavedChanges(false);
        return saveFile;
    }

    /**
     * Get a file to open or save to using the chooser
     * @return the selected file
     */
    File getFileFromChooser(boolean isSave) {
        JFileChooser chooser = new JFileChooser();

        // Filter out non-text files
        chooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                int dot = f.getName().lastIndexOf('.');
                return dot > 0 && f.getName().substring(dot + 1).equals(TXT_EXTENSION);
            }
            @Override
            public String getDescription() {
                return "Text Files";
            }
        });

        // Get the file and ask the user for approval
        if (isSave) {
            if (chooser.showSaveDialog(gui.getFrame()) == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile();
            }
        } else {
            if (chooser.showOpenDialog(gui.getFrame()) == JFileChooser.APPROVE_OPTION) {
                return chooser.getSelectedFile();
            }
        }

        // No file chosen
        return null;
    }

}
