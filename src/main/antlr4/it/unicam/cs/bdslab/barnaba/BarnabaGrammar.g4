/**
 * ANTLR 4 grammar for barnaba output files.
 *
 * This parser defines the grammar rules used to parse barnaba interaction files.
 * The input typically consists of residue pairs with their positions and
 * interaction annotations (base pairing, stacking, etc.).
 *
 * @author Francesco Palozzi
 */
grammar BarnabaGrammar;


// ------------------------------------
// Parser rules
// ------------------------------------

barnabaFile: commentLine* interactionLine* EOF;        // File with optional comments then interactions

residueSpec: NUCLEOTIDE '_' INT '_' INT;               // e.g., A_1_2 (base_index1_index2)

interactionLine: residueSpec residueSpec ANNOTATION;   // Two residues + interaction type

commentLine: COMMENT;                                  // Single comment line


// ------------------------------------
// Lexer rules
// ------------------------------------

NUCLEOTIDE: [ACGUacguTtRrYysSWwKkMmBbDdHhVvNn];        // Standard and degenerate nucleotide codes

INT: [0-9]+;                                           // Integer (position indices)

ANNOTATION: ( [GUWCHS] [GUWCHS] [ct] )                 // Base-pair edge annotation (e.g., WWc)
          | ( [<>] [<>] )                              // Stack annotations: >>, <<, <>, ><
          | 'XXX';                                     // Unknown annotation placeholder

COMMENT: '#' ~[\r\n]+;                                 // Line comment (ignored)

WS: [ \t\r\n]+ -> skip;                                // Skip whitespace