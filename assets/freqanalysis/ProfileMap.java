package assets.freqanalysis;

import assets.parsing.AuthorParser;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * Class to store the author profile map on disk.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class ProfileMap {

    private final HashMap<String, AuthorProfile> map;
    private final LinkedList<String> authors;

    /**
     * Initialize datastructures
     */
    private ProfileMap() {
        map = new HashMap<>();
        authors = new LinkedList<>();
    }

    /**
     * Build a new ProfileMap from profile files
     * @return the new map
     */
    public static ProfileMap buildMapFromFiles() throws IOException {
        ProfileMap profileMap = new ProfileMap();
        File authorsDir = new File(AuthorParser.AUTHORS_DIR);
        assert authorsDir.isDirectory();

        File authors[] = authorsDir.listFiles();
        assert authors != null;

        for (File author : authors) {
            if (author.isFile() && !author.getName().equals("test")
                    && author.getCanonicalPath().contains(AuthorParser.PROFILE_EXT)) {

                assert author.isFile();
                AuthorProfile current = AuthorProfile.buildFromFile(author.getName());

                profileMap.addProfile(current);
            }
        }

        return profileMap;
    }

    /**
     * Add a given AuthorProfile to the map
     * @param profile - the profile to add
     */
    void addProfile(AuthorProfile profile) {
        if (!map.containsKey(profile.author)) {
            map.put(profile.author, profile);
            authors.add(profile.author);
        }
    }

    /**
     * Check if a given author is in the map
     * @param a - the author to check
     * @return true if the author has at least one work in the map
     */
    public boolean containsNoAuthor(String a) { return !authors.contains(a); }

    /**
     * Getter for authors
     * @return the list of authors
     */
    public LinkedList<String> getAuthors() { return authors; }

    /**
     * Getter for the map
     * @return the map
     */
    public Map<String, AuthorProfile> getMap() { return map; }

}
