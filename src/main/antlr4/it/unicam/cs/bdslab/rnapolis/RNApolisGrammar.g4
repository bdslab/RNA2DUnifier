/**
 * ANTLR 4 grammar for RNApolis output files.
 *
 * This parser defines the grammar rules used to parse RNApolis output files.
 * The input typically consists of one or more strands, each with a header,
 * a nucleotide sequence, and interaction lines describing base pairing.
 *
 * @author Francesco Palozzi
 */
grammar RNApolisGrammar;

// ------------------------------------
// Parser rules
// ------------------------------------

rnapolisFile : strandSection+ EOF ;                // One or more strand sections

strandSection : header sequence interaction* ;     // Header, sequence, optional interactions

header : HEADER_STRING ;                           // FASTA-style header line

sequence : 'seq' NUCLEOTIDE_SEQUENCE ;             // 'seq' keyword + nucleotide sequence

interaction : INTERACTION_TYPE INTERACTION_SEQUENCE ;  // Interaction type + encoded structure


// ------------------------------------
// Lexer rules
// ------------------------------------

INTERACTION_TYPE : [ct][WHS][WHS] ;                // e.g., cWW, tSH (cis/trans + edges)

HEADER_STRING : '>' STRING;                        // Header starting with '>'

NUCLEOTIDE_SEQUENCE: [ACGU]+ ;                     // RNA sequence (A, C, G, U)

INTERACTION_SEQUENCE: ([.()[{}<>] | ']' | [A-Z] | [a-z])+ ;  // Dot-bracket notation + annotations

WS : [ \t\r\n]+ -> skip ;                          // Skip whitespace


// ------------------------------------
// Fragments
// ------------------------------------

fragment STRING : ~[ \t\r\n]+ ;                    // Any non-whitespace characters