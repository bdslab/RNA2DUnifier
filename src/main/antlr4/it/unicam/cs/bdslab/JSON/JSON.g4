/**
 * ANTLR 4 grammar for JSON (JavaScript Object Notation).
 *
 * This parser defines the grammar rules used to parse JSON files.
 * JSON is a lightweight data-interchange format based on name-value pairs
 * and ordered lists of values. Supported data types include objects,
 * arrays, strings, numbers, booleans, and null.
 *
 * Standard JSON grammar adapted for ANTLR 4
 *
 * @author Francesco Palozzi
 */
grammar JSON;


// ------------------------------------
// Parser rules
// ------------------------------------

json : value EOF;                                        // Root rule: a value followed by EOF

value : object                                           // JSON object
      | array                                            // JSON array
      | STRING                                           // String literal
      | NUMBER                                           // Number literal
      | 'true'                                           // Boolean true
      | 'false'                                          // Boolean false
      | 'null'                                           // Null value
      ;

object : '{' (member (',' member)*)? '}';                // Object with optional members
member : STRING ':' value;                               // Key-value pair inside an object
array : '[' (value (',' value)*)? ']';                   // Array with optional values


// ------------------------------------
// Lexer rules
// ------------------------------------

STRING : '"' (ESC | ~["\\])* '"';                        // Double-quoted string with escapes
fragment ESC : '\\' (["\\/bfnrt] | 'u' HEX HEX HEX HEX); // Escape sequences
fragment HEX : [0-9a-fA-F];                              // Hexadecimal digit

NUMBER : '-'? INT ('.' DIGITS)? (EXPONENT)?;             // Integer, decimal, and scientific notation
fragment INT : '0' | [1-9] DIGITS?;                      // Zero or non-zero with optional digits
fragment DIGITS : [0-9]+;                                // One or more digits
fragment EXPONENT : [eE] [+-]? DIGITS;                   // Exponent with optional sign

WS : [ \t\n\r]+ -> skip;                                 // Skip whitespace