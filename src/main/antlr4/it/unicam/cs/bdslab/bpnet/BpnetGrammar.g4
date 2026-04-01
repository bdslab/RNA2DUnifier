/**
 * ANTLR 4 grammar for bpnet output files.
 *
 * This parser defines the grammar rules used to parse bpnet output files.
 * The input typically consists of lines representing nucleotide pairs with
 * their positions, annotations, and bonding information.
 *
 * @author Francesco Palozzi
 */
grammar BpnetGrammar;

bpnetFile: pairs+ EOF;

pairs: INT INT TEXT '?' TEXT pair*;

pair:  INT INT TEXT '?' TEXT BOND;

PAIR_TYPE: ('BP' | 'TP' | 'BF') -> skip;
DEFORMATION: [0-9]'.'INT+ -> skip;

INT: [0] | [1-9][0-9]*;
TEXT: [A-Za-z0-9]+;
BOND: EDGE ':' EDGE [CT];

WS: [ \t\r\n] -> skip;
COMMENT: '#' ~[?#\n]+ -> skip;

fragment EDGE: [WHSwhszg+];