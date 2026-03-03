/*
 * ANTLR 4 lexer grammar for RNApolis output files.
 *
 * This lexer defines the tokens used by the RNApolisParser to parse output
 * files from the RNApolis suite of bioinformatics tools. RNApolis output
 * typically contains multiple strands, each with a header line, a sequence line,
 * and several lines representing interactions (e.g., base pairs, hydrogen bonds).
 *
 * @author Francesco Palozzi
 * @see RNApolisParser
 */
lexer grammar RNApolisLexer;

// ------------------------------------------------
// Fragments
// ------------------------------------------------
fragment WS_CHAR: [ \t];
fragment NEWLINE_CHAR: '\r'? '\n';
fragment IUPAC_CODE: [ACGUacguTtRrYysSWwKkMmBbDdHhVvNn-];
fragment NON_STANDARD_CODE: ["?]~[-+=/47P0I];


// ----------------------------------------------------------------
// Default mode - for everything except specialized modes
// ----------------------------------------------------------------
WS: WS_CHAR+ -> skip;
NEWLINE: NEWLINE_CHAR -> skip;

// Switch to HEADER mode when see '>'
HEADER_START: '>' -> skip, pushMode(HEADER_MODE);

// Switch to SEQUENCE mode when see 'seq'
SEQ_START: 'seq' -> skip, pushMode(SEQ_MODE);

// Switch to INTERACTION mode when see 'cWW', 'tHW',...
INTERACTION_TYPE: [ct][WHS][WHS] -> pushMode(INT_MODE);


// ------------------------------------------------
// Header mode - after '>'
// ------------------------------------------------
mode HEADER_MODE;

TITLE: [a-zA-Z_][a-zA-Z0-9_]*;

H_NEWLINE: NEWLINE_CHAR -> skip, popMode;
H_WS: WS_CHAR+ -> skip;


// ------------------------------------------------
// Sequence mode - after 'seq'
// ------------------------------------------------
mode SEQ_MODE;

NUCLEOTIDE: (IUPAC_CODE | NON_STANDARD_CODE)+;

S_NEWLINE: NEWLINE_CHAR -> skip, popMode;
S_WS: WS_CHAR+ -> skip;


// ------------------------------------------------
// INTERACTION mode - after cWW, tHW,...
// ------------------------------------------------
mode INT_MODE;

SYMBOL: [.()[{}<>] | ']' | [A-Z] | [a-z];

I_NEWLINE: NEWLINE_CHAR -> skip, popMode;
I_WS: WS_CHAR+ -> skip;