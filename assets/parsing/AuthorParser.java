package assets.parsing;

/**
 * Class to parse out the main eBook from Project Gutenberg files.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class AuthorParser extends CorporaParser {

    private final static String DIR_DELIM = "/";
    private final static char SPACE_DELIM = '_';
    private final static char SPACE = ' ';

    public final static String AUTHORS_DIR = "src/files/authors";
    public final static String TEST_DIR = "src/files/authors/test";

    public final static String BOOK_EXT = ".txt";
    public final static String PROFILE_EXT = "_PROFILE.txt";
    private final static String MODEL_EXT = "_MODEL.txt";

    /**
     * Retrieve the title and author
     * @param filename - the file to open
     * @return a String array of title and author
     */
    public static String[] titleAndAuthor(String filename) {
        String titleAuthor[] = new String[2];
        String filenameParts[];

        if (filename.contains(PROFILE_EXT)) {
            filenameParts = splitLineOnDelimiter(filename
                    .replace(PROFILE_EXT, BOOK_EXT), DIR_DELIM);
        } else {
            filenameParts = splitLineOnDelimiter(filename, DIR_DELIM);
        }

        titleAuthor[0] = filenameParts[filenameParts.length - 1]
                .replace(SPACE_DELIM, SPACE).substring(0,
                        filenameParts[filenameParts.length - 1].length() - 4);
        titleAuthor[1] = filenameParts[filenameParts.length - 2]
                .replace(SPACE_DELIM, SPACE);

        return titleAuthor;
    }

    /**
     * Get a given author's directory name from the author name
     * @param author - the author to query
     * @return the directory for the given author
     */
    private static String getDirectoryFromAuthor(String author) {
        return AUTHORS_DIR.concat("/").concat(author.replace(SPACE, SPACE_DELIM));
    }

    /**
     * Get the processed profile filename for a given book filename
     * @param bookFilename - the book filename
     * @return the corresponding profile filename
     */
    public static String getProfileFilename(String bookFilename) {
        if (bookFilename.contains(BOOK_EXT)) {
            return bookFilename.replace(BOOK_EXT, PROFILE_EXT);
        }
        return null;
    }

    /**
     * Get the processed model filename for a given author
     * @param author - the author to query
     * @return the corresponding model filename
     */
    public static String getModelFilename(String author) {
        return getDirectoryFromAuthor(author)
                .concat(author.replace(SPACE, SPACE_DELIM)).concat(MODEL_EXT);
    }

}
