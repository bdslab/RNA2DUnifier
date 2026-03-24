grammar X3dnaGrammar;

x3dnaFile: '{' item* '}' EOF;

item: (String ':' (array | object) | number_pair | string_pair) ','?;

object: '{' (string_pair | number_pair | item) (',' (string_pair | number_pair | item))* '}';

array : '[' ((String | Number | item | object | 'null') (',' (String | Number | item | object | 'null'))*)? ']';

string_pair: String ':' (String | 'null');

number_pair: String ':' Number;

String: '"' (~["\\])* '"' ;

Number: '-'? ([0-9]+ | [0-9]+ '.' [0-9]+);

WS : [ \t\n\r]+ -> skip ;