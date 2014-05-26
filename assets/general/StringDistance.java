package assets.general;

/**
 * Class to encapsulate the computation of relative
 * String distances (i.e. Levenshtein)
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public final class StringDistance {

    /**
     * Return the Levenshtein distance (computed dynamically for speed)
     * between two Strings
     * @param word1 - the first word
     * @param word2 - the second word
     * @return the integer distance between the words
     */
    public static int levenshtein(String word1, String word2) {

        // Convert to all lowercase for dictionary matching
        String first = word1.toLowerCase();
        String second = word2.toLowerCase();

        // Initialize matrix
        int matrix[][] = new int[first.length() + 1][second.length() + 1];

        // Dropping/inserting all characters in edge rows
        for (int i = 0; i <= first.length(); i++) { matrix[i][0] = i; }
        for (int j = 0; j <= second.length(); j++) { matrix[0][j] = j; }

        // Go through and compute the matrix
        for (int j = 1; j <= second.length(); j++) {
            for (int i = 1; i <= first.length(); i++) {

                // If there is a match
                if (first.charAt(i - 1) == second.charAt(j - 1)) {
                    matrix[i][j] = matrix[i - 1][j - 1];
                }

                // No match - get minimum of insertion, deletion, substitution
                else {
                    int deletion = matrix[i - 1][j] + 1;
                    int insertion = matrix[i][j - 1] + 1;
                    int substitution = matrix[i - 1][j - 1] + 1;

                    matrix[i][j] = Math.min(Math.min(deletion, insertion), substitution);
                }
            }
        }

        // Uncomment to print matrix for debugging
//        for (int i = 0; i <= first.length(); i++) {
//            String print = "";
//            for (int j = 0; j <= second.length(); j++) {
//                print += matrix[i][j] + " ";
//            }
//            System.out.println(print);
//        }

        // Bottom-right corner of the matrix has distance
        return matrix[first.length()][second.length()];
    }

    public static void main(String args[]) {
        System.out.println(levenshtein("satement", "statement"));
    }

}
