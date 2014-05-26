package assets.general;

/**
 * Simple wrapper class for a word and an integer score.
 *
 * Written for the StyleCheck program by Alex Welton.
 */
@SuppressWarnings("rawtypes")
public class ScoredWord implements Comparable {

    public final String word;
    public double score;

    public ScoredWord(String w, double s) {
        word = w;
        score = s;
    }

    /**
     * Comparator for sorting by score
     * @param o - the other ScoredWord object
     * @return the appropriate signum
     */
    @SuppressWarnings({"NullableProblems", "CastToConcreteClass"})
    @Override
    public int compareTo(Object o) {
        return (int) Math.signum(score - ((ScoredWord) o).score);
    }

    /**
     * Check if this ScoredWord is equal to another by word
     * @param o - the other ScoredWord object
     * @return true if the two are equal
     */
    @SuppressWarnings("InstanceofInterfaces")
    @Override
    public boolean equals(Object o) {
        return o instanceof ScoredWord && ((ScoredWord) o).word.equals(word);
    }

}
