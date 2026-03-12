/*
 * ANTLR 4 parser grammar for mc-annotate output files.
 *
 * This parser defines the structure of mc-annotate output files,
 * including sections for residue conformations, adjacent and non-adjacent
 * stackings, and base-pair interactions. It relies on tokens defined in
 * McAnnotateLexer.
 *
 * Note: The current rules capture only the essential identifiers; additional
 * tokens (e.g., sugar pucker, orientation, bond details) are present in the
 * token stream but not yet assigned to specific parser rule elements. The
 * grammar can be extended to include these fields as needed.
 *
 * @author Francesco Palozzi
 * @see McAnnotateLexer
 */
parser grammar McAnnotateParser;

options {
    tokenVocab = McAnnotateLexer;
}

mcAannotateFile: residueSection adjStackingSection nonAdjStackingSection basePairsSection EOF;

residueSection: residueElement*;
residueElement: STRAND_POSITION NUCLEOTIDE?;

adjStackingSection: adjStackingElement*;
adjStackingElement: AS_STACK;

nonAdjStackingSection: nonAdjStackingElement*;
nonAdjStackingElement: NAS_STACK;

basePairsSection: basePairsElement*;
basePairsElement: POSITION_PAIR NUCLEOTIDE_PAIR BOND+ ORIENTATION?;