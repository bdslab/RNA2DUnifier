grammar Fr3d;

fr3dFile: '{' pdb_id chain_id modified annotations '}' EOF;

pdb_id: string_pair ',';

chain_id: string_pair ',';

modified: '"modified":' '[' object (',' object)* '],' | '[' '],';

annotations: '"annotations":' '[' object (',' object)* ']' | '[' ']';

object: '{' string_pair (',' string_pair)* '}';

string_pair: String ':' String;

String : '"' (~["\\])* '"' ;

WS : [ \t\n\r]+ -> skip ;