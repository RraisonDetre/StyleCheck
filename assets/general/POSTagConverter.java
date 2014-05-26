package assets.general;

/**
 * Class to convert tagsets between CLAWS7
 * (used by n-grams) and Penn Treebank (used by Stanford).
 *
 * Written for the StyleCheck program by Alex Welton.
 *
 */
public class POSTagConverter {

    /**
     * Static method to return the Penn tag related to
     * the more detailed CLAWS7 tag. Conversion in the opposite
     * direction is impractical, as it would require implementing
     * a CLAWS7 parser.
     * @param tag - the CLAWS7 tag
     * @return the appropriate Penn tag
     */
    public static String CLAWS7ToPenn(String tag) {
        String upTag = tag.toUpperCase();

        switch(upTag) {

            // Coordinating conjunctions
            case "CC": return "CC";
            case "CCB": return "CC";

            // Subordinate conjunctions/prepositions
            case "CS": return "IN";
            case "CSA": return "IN";
            case "CSN": return "IN";
            case "CST": return "IN";
            case "CSW": return "IN";

            // Determiners
            case "DA": return "DT";
            case "DA1": return "DT";
            case "DA2": return "DT";
            case "DAR": return "DT";
            case "DAT": return "DT";
            case "DD": return "DT";
            case "DD1": return "DT";
            case "DD2": return "DT";

            // Predeterminers
            case "DB": return "PDT";
            case "DB2": return "PDT";

            // Wh-Determiners
            case "DDQ": return "WDT";
            case "DDQGE": return "WDT";
            case "DDQV": return "WDT";

            // Cardinal numbers
            case "MC": return "CD";
            case "MC1": return "CD";
            case "MC2": return "CD";
            case "MCGE": return "CD";
            case "MCMC": return "CD";
            case "MD": return "CD";
            case "MF": return "CD";

            // Singular or mass nouns
            case "ND1": return "NN";
            case "NN": return "NN";
            case "NN1": return "NN";
            case "NNA": return "NN";
            case "NNB": return "NN";
            case "NNL1": return "NN";
            case "NNO": return "NN";
            case "NNT1": return "NN";
            case "NNU": return "NN";
            case "NNU1": return "NN";

            // Plural nouns
            case "NN2": return "NNS";
            case "NNL2": return "NNS";
            case "NNO2": return "NNS";
            case "NNT2": return "NNS";
            case "NNU2": return "NNS";

            // Singular proper nouns
            case "NP": return "NNP";
            case "NP1": return "NNP";
            case "NPD1": return "NNP";
            case "NPM1": return "NNP";

            // Plural proper nouns
            case "NP2": return "NNPS";
            case "NPD2": return "NNPS";
            case "NPM2": return "NNPS";

            // Personal pronouns
            case "PN": return "PRP";
            case "PN1": return "PRP";
            case "PNX1": return "PRP";
            case "PPH1": return "PRP";
            case "PPHO1": return "PRP";
            case "PPHO2": return "PRP";
            case "PPHS1": return "PRP";
            case "PPHS2": return "PRP";
            case "PPIO1": return "PRP";
            case "PPIO2": return "PRP";
            case "PPIS1": return "PRP";
            case "PPIS2": return "PRP";
            case "PPX1": return "PRP";
            case "PPX2": return "PRP";
            case "PPY": return "PRP";

            // Wh-Pronouns
            case "PNQO": return "WP";
            case "PNQS": return "WP";
            case "PNQV": return "WP";

            // Possessive pronouns
            case "PPGE": return "PRP$";

            // Adverbs
            case "RA": return "RB";
            case "REX": return "RB";
            case "RG": return "RB";
            case "RGQ": return "RB";
            case "RGQV": return "RB";
            case "RL": return "RB";
            case "RP": return "RB";
            case "RPK": return "RB";
            case "RR": return "RB";
            case "RT": return "RB";

            // Wh-Adverbs
            case "RRQ": return "WRB";
            case "RRQV": return "WRB";

            // Comparative adverbs
            case "RGR": return "RBR";
            case "RRR": return "RBR";

            // Superlative adverbs
            case "RGT": return "RBS";
            case "RRT": return "RBS";

            // Infinitive marker
            case "TO": return "TO";

            // Interjections
            case "UH": return "UH";

            // Modal
            case "VM": return "MD";
            case "VMK": return "MD";

            // Verbs in base form
            case "VB0": return "VB";
            case "VBI": return "VB";
            case "VD0": return "VB";
            case "VDI": return "VB";
            case "VH0": return "VB";
            case "VHI": return "VB";
            case "VVI": return "VB";

            // Verbs in past tense
            case "VBDZ": return "VBD";
            case "VDD": return "VBD";
            case "VHD": return "VBD";
            case "VVD": return "VBD";

            // Verbs in gerund or present participle
            case "VVG": return "VBG";
            case "VVGK": return "VBG";
            case "VBG": return "VBG";
            case "VDG": return "VBG";
            case "VHG": return "VBG";

            // Verbs in past participle
            case "VVN": return "VBN";
            case "VVNK": return "VBN";
            case "VHN": return "VBN";

            // Verbs, non 3rd-person singular present
            case "VBM": return "VBP";

            // Verbs, 3rd-person present
            case "VVZ": return "VBZ";

            // Existential there
            case "EX": return "EX";

            // Adjectives
            case "JJ": return "JJ";
            case "JK": return "JJ";

            // Comparative adjectives
            case "JJR": return "JJR";

            // Superlative adjectives
            case "JJT": return "JJS";

            // No match found or one does not exist
            default: return "UNKNOWN";
        }

    }

    /**
     * Check if a part of speech is an allowed type
     * @param pos - the part of speech
     * @return true if the POS is an allowed type
     */
    public static boolean isAllowedType(String pos) {
        return isAdverb(pos) || isVerb(pos)
                || isAdjective(pos) || isNoun(pos);
    }

    /**
     * Check if this tag is part of the adverb family
     * @param pos - the part of speech tag
     * @return true if this tag is part of the adverb family
     */
    public static boolean isAdverb(String pos) {
        String p = pos.toUpperCase();
        return p.equals("RB") || p.equals("WRB")
                || p.equals("RBR") || p.equals("RBS");
    }

    /**
     * Check if this tag is part of the verb family
     * @param pos - the part of speech tag
     * @return true if this tag is part of the verb family
     */
    public static boolean isVerb(String pos) {
        String p = pos.toUpperCase();
        return p.equals("VB") || p.equals("VBD")
                || p.equals("VBG") || p.equals("VBN");
    }

    /**
     * Check if this tag is part of the adjective family
     * @param pos - the part of speech tag
     * @return true if this tag is part of the adjective family
     */
    public static boolean isAdjective(String pos) {
        String p = pos.toUpperCase();
        return p.equals("JJ") || p.equals("JJR") || p.equals("JJS");
    }

    /**
     * Check if this tag is part of the noun family
     * @param pos - the part of speech tag
     * @return true if this tag is part of the noun family
     */
    public static boolean isNoun(String pos) {
        String p = pos.toUpperCase();
        return p.equals("NN") || p.equals("NNS")
                || p.equals("NNP") || p.equals("NNPS");
    }

}
