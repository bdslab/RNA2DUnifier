/**
 * ANTLR 4 parser grammar for barnaba output files.
 *
 * This parser defines the grammar for processing barnaba output files,
 * building upon the tokens defined in BarnabaLexer. It structures the input
 * into file information, sequence data, and residue interactions.
 *
 * @author Francesco Palozzi
 * @see BarnabaLexer
 */
parser grammar BarnabaParser;

options {
    tokenVocab = BarnabaLexer;
}

barnabaFile: info* interaction+ EOF;

info: FILE_NAME # fileName
    | sequence  # sequenceList;

sequence: sequenceElement+;

sequenceElement : S_IUPAC_CODE S_INT S_INT ;

interaction: residue residue ANNOTATION;

residue: NUCLEOTIDE INT INT;