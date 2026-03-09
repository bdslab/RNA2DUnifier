/**
 * ANTLR 4 lexer grammar for RNAview output files.
 *
 * This lexer defines the tokens used by the RNAviewParser to parse RNAview
 * output files. The file structure includes a header line indicating the PDB
 * file name, followed by a section of base-pair data.
 *
 * @author Francesco Palozzi
 * @see RNAviewParser
 */
lexer grammar RNAviewLexer;

// ------------------------------------------------
// Fragments
// ------------------------------------------------
fragment WS_CHAR: [ \t];
fragment NEWLINE_CHAR: '\r'? '\n';
fragment IUPAC_CODE: [ACGUacguTtRrYysSWwKkMmBbDdHhVvNn-];

// ----------------------------------------------------------------
// Default mode - for everything except specialized modes
// ----------------------------------------------------------------
NEWLINE: NEWLINE_CHAR -> skip;
UNCOMMON_RESIDUE: 'uncommon residue' [a-zA-Z0-9#:[\] ]* -> skip;

// Switch to FILE_NAME mode when see 'PDB data file name: '
FILE: 'PDB data file name: ' -> skip, pushMode(FILE_NAME_MODE);

// Switch to PAIRS mode when see 'BEGIN_base-pair'
BEGIN_PAIR: '-'+.*'BEGIN_base-pair'NEWLINE_CHAR -> skip, pushMode(PAIRS_MODE);


// ------------------------------------------------
// File name mode - after 'PDB data file name: '
// ------------------------------------------------
mode FILE_NAME_MODE;

F_NEWLINE: NEWLINE_CHAR -> skip;

FILE_NAME
    : ( '/'? [a-zA-Z0-9_-]+ '/' )*
      [a-zA-Z0-9_]+
      '.' ~[ /?#\n]+
    -> popMode;

// ------------------------------------------------
// Pairs mode - after 'BEGIN_base-pair'
// ------------------------------------------------
mode PAIRS_MODE;
fragment P_WS: WS_CHAR;
ORIENTATION: 'cis' | 'tran';

CHARS_TO_SKIP: [_,: \n] -> skip;

CHAIN: [A-Z];

PAIR_ANNOTATION: [sSWH+-.?];

NUMBER: [1-9][0-9]*;

BASE_PAIR: IUPAC_CODE'-'IUPAC_CODE;

BASE_PAIR_ANNOTATION:
    PAIR_ANNOTATION '/' PAIR_ANNOTATION P_WS
;

STACKED: 'stacked';

SAENGER: [XVI]+ | 'n/a' | '!' ('1H')? '('[bs]'_'[bs]')';

END_PAIR: 'END_base-pair' -> skip, popMode, pushMode(EXTRA_MODE);

// ------------------------------------------------
// Extra mode - after 'END_base-pair'
// ------------------------------------------------
mode EXTRA_MODE;

OTHERS: .* -> skip, popMode;