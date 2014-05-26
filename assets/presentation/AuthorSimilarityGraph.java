package assets.presentation;

import assets.freqanalysis.AuthorIdentifier;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Class to create a graph of all authors'
 * similarity to all others' and save it to file.
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
class AuthorSimilarityGraph {

    private static final String GRAPH_FILE_PATH
            = "src/files/presentation/author_similarity_graph.txt";
    private static final String FIELD_DELIM = ":";

    private double stylometryMatrix[][];
    private LinkedList<String> authorList;
    private AuthorIdentifier identifier;

    /**
     * Initialize the matrix
     */
    private AuthorSimilarityGraph() throws
            IOException {

        identifier = new AuthorIdentifier();
        authorList = identifier.profiles.getAuthors();
        assert !authorList.isEmpty();

        stylometryMatrix = new double[authorList.size()][authorList.size()];
    }

    /**
     * Build the similarity matrix
     */
    void buildMatrix() {

        // Iterate over author list and fill out matrix
        for (int i = 0; i < authorList.size(); i++) {
            for (int j = 0; j < authorList.size(); j++) {
                if (i != j) {
                    stylometryMatrix[i][j] = identifier
                            .getAuthorDistance(authorList.get(i), authorList.get(j));
                }
            }
        }
    }

    /**
     * Save the similarity matrix to file
     */
    void saveToFile() {
        BufferedWriter writer;
        File graphFile = new File(GRAPH_FILE_PATH);
        assert graphFile.isFile();

        try {
            writer = new BufferedWriter(new FileWriter(graphFile));
            writer.write(getMatrixString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get the string format version of the matrix
     * @return the string version of the matrix
     */
    private String getMatrixString() {
        String matrixString = "";

        // Iterate over matrix and build string
        for (int i = 0; i < authorList.size(); i++) {
            for (int j = 0; j < authorList.size(); j++) {
                if (i != j) {
                    matrixString += authorList.get(i) + " " + FIELD_DELIM + " "
                            + authorList.get(j) + " " + FIELD_DELIM + " "
                            + stylometryMatrix[i][j] + "\n";
                }
            }
        }
        return matrixString;
    }

    /**
     * Graph creation method
     * @param args
     */
    public static void main(String... args) {

        System.out.println("Calculating author similarity graph...");
        long startTime = System.currentTimeMillis();

        try {
            AuthorSimilarityGraph aig = new AuthorSimilarityGraph();
            aig.buildMatrix();
            aig.saveToFile();
            System.out.println(aig.getMatrixString());
        } catch (IOException e) {
            e.printStackTrace();
        }

        long endTime = System.currentTimeMillis();
        long runTime = endTime - startTime;
        System.out.println("Finished building author similarity graph in " + runTime + " ms.\n");
    }

}
