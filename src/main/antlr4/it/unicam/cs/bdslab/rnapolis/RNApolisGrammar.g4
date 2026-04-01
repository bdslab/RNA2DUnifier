grammar RNApolisGrammar;

// Parser
rnapolisFile : strandSection+ EOF ;

strandSection : header sequence interaction* ;

header : HEADER_STRING ;
sequence : 'seq' NUCLEOTIDE_SEQUENCE ;
interaction : INTERACTION_TYPE INTERACTION_SEQUENCE ;

// Lexer
INTERACTION_TYPE : [ct][WHS][WHS] ;
HEADER_STRING : '>' STRING;
NUCLEOTIDE_SEQUENCE: [ACGU]+ ;
INTERACTION_SEQUENCE: ([.()[{}<>] | ']' | [A-Z] | [a-z])+ ;

WS : [ \t\r\n]+ -> skip ;

fragment STRING : ~[ \t\r\n]+ ;