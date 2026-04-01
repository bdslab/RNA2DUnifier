grammar RNAviewGrammar;

// Parser
rnaviewFile: basePairLine* EOF ;

basePairLine: ASSIGNED_NUMBERS
              CHAIN
              NUMBER
              BASE_PAIR
              NUMBER
              CHAIN
              annotation
              SAENGER?
             ;

annotation: STACKED | EDGE_PAIR ORIENTATION ;

// Lexer
SKIP_START : .*? 'BEGIN_base-pair' -> skip ;
SKIP_END   : 'END_base-pair' .* -> skip ;

EDGE_PAIR  : [sSWH+-.?] '/' [sSWH+-.?] ;
ORIENTATION: 'cis' | 'tran' ;
NUMBER     : [0-9]+ ;
ASSIGNED_NUMBERS: NUMBER '_' NUMBER ',';
CHAIN      : [A-Z] ':';
BASE_PAIR  : IUPAC_BASE '-' IUPAC_BASE ;
STACKED    : 'stacked' ;

SAENGER: '!' ( '1H' )? '(' [bs] '_' [bs] ')'
       | 'n/a'
       | [XVI]+ ;

WS : [ \t\r\n]+ -> skip ;

// Fragments
fragment IUPAC_BASE : [ACGUacguTtRrYysSWwKkMmBbDdHhVvNn] ;