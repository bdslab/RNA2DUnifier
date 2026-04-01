grammar JSON;

// Parser rules
json : value EOF;

value : object
      | array
      | STRING
      | NUMBER
      | 'true'
      | 'false'
      | 'null'
      ;

object : '{' (member (',' member)*)? '}';
member : STRING ':' value;
array : '[' (value (',' value)*)? ']';

// Lexer rules
STRING : '"' (ESC | ~["\\])* '"';
fragment ESC : '\\' (["\\/bfnrt] | 'u' HEX HEX HEX HEX);
fragment HEX : [0-9a-fA-F];

NUMBER : '-'? INT ('.' DIGITS)? (EXPONENT)?;
fragment INT : '0' | [1-9] DIGITS?;      // zero oppure cifra non zero seguita da cifre opzionali
fragment DIGITS : [0-9]+;                // una o più cifre (possono iniziare con zero)
fragment EXPONENT : [eE] [+-]? DIGITS;   // esponente con almeno una cifra

WS : [ \t\n\r]+ -> skip;