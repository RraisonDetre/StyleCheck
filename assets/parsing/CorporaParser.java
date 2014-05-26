package assets.parsing;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

/**
 * Class to parse various machine-readable corpora for insertion
 * into data structures.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
@SuppressWarnings("SameParameterValue")
public class CorporaParser {

    public static final String TXT_EXT = ".txt";

    /**
     * General method to return a text file's contents as a string.
     *
     * @param filename - the string filename to open
     * @return the string containing the entire file contents.
     */
    public static String getFileAsString(String filename) {

        try {
            BufferedReader fileReader = new BufferedReader(new FileReader(filename));
            StringBuilder fileBuilder = new StringBuilder();

            String currentLine = fileReader.readLine();
            while (currentLine != null) {
                fileBuilder.append(currentLine);
                fileBuilder.append(System.lineSeparator());
                currentLine = fileReader.readLine();
            }

            String fileContents = fileBuilder.toString();
            fileReader.close();
            return fileContents;

        } catch (FileNotFoundException e) {
            System.err.println("Uh oh! FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("Uh oh! IOException: " + e.getMessage());
        }

        return null;
    }

    /**
     * Method to return a String array of all lines in a given file.
     * @param filename - the file to read
     * @return the array of Strings
     */
    private static String[] getFileLines(String filename) {
        return getFileAsString(filename).split(System.getProperty("line.separator"));
    }

    /**
     * Return the lines from a file as a LinkedList
     * @param filename - the file to read
     * @return the LinkedList of Strings
     */
    public static LinkedList<String> getFileLineList(String filename) {
        return new LinkedList<>(Arrays.asList(getFileLines(filename)));
    }

    /**
     * Splits a given String into tokens by the regex delimiter passed in
     * @param line - the String to split
     * @param delimiter - - the delimiters to apply when splitting an individual line
     * @return an array of Strings - the tokens from the line
     */
    static String[] splitLineOnDelimiter(String line, String delimiter) {
        return line.split(delimiter);
    }

    /**
     * Utility function to validate that a filename has the specified extension.
     * @param filename - the String filename
     * @param extension - the String extension to check against
     * @return the boolean indicating whether the filename is valid
     */
    public static boolean validateExtension(String filename, String extension) {
        return filename.substring(filename.lastIndexOf('.')).equals(extension);
    }

    /**
     * General method to parse the contents of a corpus text file, assumed to be in the
     * usual one-block-of-information-per-line format. The method takes each line
     * and splits it on the provided string regex delimiter expression, then stores
     * and returns the contents of the file in a 2D array.
     *
     * @param fileContents - the contents of a file to be split on newlines and parsed
     * @param delimiter - the delimiters to apply when splitting an individual line
     * @return a 2D array of strings, where the indices are [line number][field number]
     */
    static String[][] splitFileOnDelimiter(String fileContents, String delimiter) {
        String fileLines[] = fileContents.split(System.getProperty("line.separator"));
        String result[][] = new String[fileLines.length][];
        String currentLineFields[];

        for (int i = 0; i < fileLines.length; i++) {
            currentLineFields = fileLines[i].split(delimiter);
            result[i] = new String[currentLineFields.length];
            System.arraycopy(currentLineFields, 0, result[i], 0, currentLineFields.length);
        }

        return result;
    }

}
