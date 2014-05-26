package assets.spellcheck;

import org.clapper.util.misc.ObjectExistsException;
import org.clapper.util.misc.VersionMismatchException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Class to hold a client for the spell check functionality.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
class SpellCheckClient {

    /**
     * Get the file path from the user.
     * @return the filepath
     */
    private static String getUserPath() {
        String userPath = null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        System.out.print("Enter file path to spell check or 'quit' to quit: ");

        try {
            userPath = reader.readLine();
        } catch (IOException e) {
            System.out.println("Something went wrong entering the filename.");
            e.printStackTrace();
        }

        return userPath;
    }

    /**
     * Main method
     * @throws IOException
     */
    public static void main() throws IOException, ObjectExistsException,
            VersionMismatchException, ClassNotFoundException {

        EnhancedSpellCheck spellCheck = new EnhancedSpellCheck();
        String fileToCheck;

        while (true) {
            fileToCheck = getUserPath();
            if (fileToCheck.toLowerCase().equals("quit")) {
                return;
            }
            spellCheck.checkFileSpelling(fileToCheck);
        }

    }

}
