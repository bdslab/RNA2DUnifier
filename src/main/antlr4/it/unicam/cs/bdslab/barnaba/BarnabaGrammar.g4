grammar BarnabaGrammar;

// Parser
barnabaFile: commentLine* interactionLine* EOF;

residueSpec: NUCLEOTIDE '_' INT '_' INT;

interactionLine: residueSpec residueSpec ANNOTATION;

commentLine: COMMENT;


// Lexer
NUCLEOTIDE: [ACGUacguTtRrYysSWwKkMmBbDdHhVvNn];

INT: [0-9]+;

ANNOTATION: ( [GUWCHS] [GUWCHS] [ct] )    // e.g., WWc
          | ( [<>] [<>] )                 // stack annotations: >>, <<, <>, ><
          | 'XXX';                        // unknown annotation

COMMENT: '#' ~[\r\n]+;

// Whitespace (ignored)
WS: [ \t\r\n]+ -> skip;