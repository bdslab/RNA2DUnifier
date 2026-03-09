/**
 * ANTLR 4 lexer grammar for barnaba output files.
 *
 * This lexer defines the tokens used by the BarnabaParser to parse barnaba
 * output files. The input typically consists of lines containing nucleotide
 * sequences, annotations, and structural information.
 *
 * @author Francesco Palozzi
 * @see BarnabaParser
 */
lexer grammar BarnabaLexer;

// ------------------------------------------------
// Fragments
// ------------------------------------------------
fragment WS_CHAR: [ \t];
fragment NEWLINE_CHAR: '\r'? '\n';
fragment IUPAC_CODE: [ACGUacguTtRrYysSWwKkMmBbDdHhVvNn];


// ----------------------------------------------------------------
// Default mode - for everything except specialized modes
// ----------------------------------------------------------------
WS: WS_CHAR+ -> skip;
NEWLINE: NEWLINE_CHAR -> skip;

NUCLEOTIDE: IUPAC_CODE;
INT: [0-9]+;
UNDERSCORE: '_' -> skip;

ANNOTATION: EDGE EDGE ORIENTATION | STACK_ANNOTATION | UNKNOWN;
UNKNOWN: 'XXX';
ORIENTATION: [ct];
EDGE: [GUWCHS];
STACK_ANNOTATION: [<>][<>];

// Switch to COMMENT mode when see '#'
COMMENT_START: '#' -> skip, pushMode(COMMENT_MODE);

// Switch to FILE NAME mode when see '# PDB '
FILE_NAME_START: '# PDB ' -> skip, pushMode(FILE_NAME_MODE);

// Switch to SEQUENCE mode when see '# sequence '
SEQUENCE_START: '# sequence ' -> skip, pushMode(SEQUENCE_MODE);

// ------------------------------------------------
// File name mode - after '# PDB '
// ------------------------------------------------
mode FILE_NAME_MODE;
F_WS: WS_CHAR+ -> skip;
F_NEWLINE: NEWLINE_CHAR -> skip, popMode;

FILE_NAME: ~[ \t\r\n]+;

// ------------------------------------------------
// Sequence mode - after '# sequence '
// ------------------------------------------------
mode SEQUENCE_MODE;
S_WS: WS_CHAR+ -> skip;
S_NEWLINE: NEWLINE_CHAR -> skip, popMode;

S_IUPAC_CODE: IUPAC_CODE;
SKIP_CHAR: [-_] -> skip;
S_INT: [0-9]+;

// ------------------------------------------------
// Comment mode - after '#'
// ------------------------------------------------
mode COMMENT_MODE;
C_WS: WS_CHAR+ -> skip;
C_NEWLINE: NEWLINE_CHAR -> skip, popMode;
COMMENT: ~[\r\n]+ -> skip;