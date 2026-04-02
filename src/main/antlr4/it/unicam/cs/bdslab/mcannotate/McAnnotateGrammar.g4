/*
 * Combined grammar for mc-annotate output files.
 *
 * Parses sections: Residue conformations, Adjacent stackings,
 * Non-Adjacent stackings, and Base-pairs, including summary counts.
 *
 * @author Francesco Palozzi
 */
grammar McAnnotateGrammar;

// ------------------------------------------------
// Parser rules
// ------------------------------------------------

mcAnnotateFile
    : residueSection
      adjacentSection
      nonAdjacentSection
      countSection
      basePairsSection
      EOF
    ;                                         // File structure: all sections in order

residueSection
    : RESIDUE_HEADER residueLine*
    ;                                         // Header followed by zero or more residue lines

adjacentSection
    : ADJACENT_HEADER adjacentLine*
    ;                                         // Adjacent stacking section

nonAdjacentSection
    : NON_ADJ_HEADER nonAdjacentLine*
    ;                                         // Non-adjacent stacking section

countSection
    : countLine+
    ;                                         // One or more count summary lines

basePairsSection
    : BASE_PAIRS_HEADER basePairLine*
    ;                                         // Base-pairs section

residueLine
    : IDENTIFIER COLON IDENTIFIER SUGAR? ANTI_SYN?
    ;                                         // e.g., A:U C3p_endo anti

adjacentLine
    : PAIR_ID COLON ADJ_DESC
    ;                                         // e.g., A1-U2: adjacent_5p outward

nonAdjacentLine
    : PAIR_ID COLON NON_ADJ_DESC
    ;                                         // e.g., A1-U8: inward

basePairLine
    : PAIR_ID COLON NUCLEOTIDE_PAIR BOND+ ADJ_DESC? ADDITIONAL* ORIENTATION? ADDITIONAL* SAENGER?
    ;                                         // Detailed base-pair info

countLine
    : COUNT_STACKINGS
    | COUNT_ADJ
    | COUNT_NON_ADJ
    ;                                         // One of three count types


// ------------------------------------------------
// Lexer rules
// ------------------------------------------------

WS          : [ \t\r\n]+ -> skip ;            // Skip whitespace

// Section headers (dashed lines)
fragment DASH : '-' ;
RESIDUE_HEADER   : 'Residue conformations' WS* DASH+ ;
ADJACENT_HEADER  : 'Adjacent stackings' WS* DASH+ ;
NON_ADJ_HEADER   : 'Non-Adjacent stackings' WS* DASH+ ;
BASE_PAIRS_HEADER: 'Base-pairs' WS* DASH+ ;

// Count summary lines
COUNT_STACKINGS : 'Number of stackings =' WS* INT ;
COUNT_ADJ       : 'Number of adjacent stackings =' WS* INT ;
COUNT_NON_ADJ   : 'Number of non adjacent stackings =' WS* INT ;

SAENGER: [XVI]+;                              // Saenger classification (e.g., XI, VI)

IDENTIFIER  : [A-Z] [A-Z0-9]* ;               // Residue name (e.g., A, U, G)
PAIR_ID     : [A-Z] [0-9]+ '-' [A-Z] [0-9]+ ; // Pair identifier (e.g., A1-U2)

COLON       : ':' ;                           // Separator token

SUGAR       : [a-zA-Z0-9_]+? ( 'endo' | 'exo' ) ;  // Sugar pucker (e.g., C3p_endo)

ANTI_SYN    : 'anti' | 'syn' ;                // Nucleobase conformation

ADJ_DESC    : 'adjacent_5p' WS* NON_ADJ_DESC ( WS+ 'pairing' )? ;  // Adjacent stacking description
NON_ADJ_DESC: 'outward' | 'downward' | 'inward' | 'upward' ;       // Non-adjacent stacking orientation

NUCLEOTIDE_PAIR : [ACGU] '-' [ACGU] ;         // Base pair letters (e.g., A-U)
BOND            : [a-zA-Z0-9'/]+ '/' [a-zA-Z0-9'/]+ ;  // Bond type (e.g., W/WC)
ORIENTATION     : 'cis' | 'trans' ;           // Glycosidic bond orientation
ADDITIONAL      : [a-zA-Z0-9_]+ ;             // Extra annotation tokens

INT         : [0-9]+ ;                        // Integer value (positions, counts)