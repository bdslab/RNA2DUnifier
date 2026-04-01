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
    ;

residueSection
    : RESIDUE_HEADER residueLine*
    ;

adjacentSection
    : ADJACENT_HEADER adjacentLine*
    ;

nonAdjacentSection
    : NON_ADJ_HEADER nonAdjacentLine*
    ;

countSection
    : countLine+
    ;

basePairsSection
    : BASE_PAIRS_HEADER basePairLine*
    ;

residueLine
    : IDENTIFIER COLON IDENTIFIER SUGAR? ANTI_SYN?
    ;

adjacentLine
    : PAIR_ID COLON ADJ_DESC
    ;

nonAdjacentLine
    : PAIR_ID COLON NON_ADJ_DESC
    ;

basePairLine
    : PAIR_ID COLON NUCLEOTIDE_PAIR BOND+ ADJ_DESC? ADDITIONAL* ORIENTATION? ADDITIONAL* SAENGER?
    ;

countLine
    : COUNT_STACKINGS
    | COUNT_ADJ
    | COUNT_NON_ADJ
    ;

// ------------------------------------------------
// Lexer rules
// ------------------------------------------------

WS          : [ \t\r\n]+ -> skip ;

// Section headers
fragment DASH : '-' ;
RESIDUE_HEADER   : 'Residue conformations' WS* DASH+ ;
ADJACENT_HEADER  : 'Adjacent stackings' WS* DASH+ ;
NON_ADJ_HEADER   : 'Non-Adjacent stackings' WS* DASH+ ;
BASE_PAIRS_HEADER: 'Base-pairs' WS* DASH+ ;

// Count lines
COUNT_STACKINGS : 'Number of stackings =' WS* INT ;
COUNT_ADJ       : 'Number of adjacent stackings =' WS* INT ;
COUNT_NON_ADJ   : 'Number of non adjacent stackings =' WS* INT ;

SAENGER: [XVI]+;

// Residue identifiers
IDENTIFIER  : [A-Z] [A-Z0-9]* ;
PAIR_ID     : [A-Z] [0-9]+ '-' [A-Z] [0-9]+ ;

// Basic tokens
COLON       : ':' ;

// Sugar pucker: e.g., C3p_endo, O4p_endo, C2p_exo, etc.
SUGAR       : [a-zA-Z0-9_]+? ( 'endo' | 'exo' ) ;

// Anti/syn conformation
ANTI_SYN    : 'anti' | 'syn' ;

// Stacking descriptions
ADJ_DESC    : 'adjacent_5p' WS* NON_ADJ_DESC ( WS+ 'pairing' )? ;
NON_ADJ_DESC: 'outward' | 'downward' | 'inward' | 'upward' ;

// Base-pair components
NUCLEOTIDE_PAIR : [ACGU] '-' [ACGU] ;
BOND            : [a-zA-Z0-9'/]+ '/' [a-zA-Z0-9'/]+ ;
ORIENTATION     : 'cis' | 'trans' ;
ADDITIONAL      : [a-zA-Z0-9_]+ ;

// Integer numbers (used in counts, residue positions)
INT         : [0-9]+ ;