/*
 * Copyright 2026 Francesco Palozzi
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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


// ------------------------------------
// Parser rules
// ------------------------------------

bpnetFile: pairs+ EOF;                    // One or more pair blocks

pairs: INT INT TEXT '?' TEXT pair*;       // Header line + optional bond details

pair:  INT INT TEXT '?' TEXT BOND;        // Single bond line


// ------------------------------------
// Lexer rules
// ------------------------------------

PAIR_TYPE: ('BP' | 'TP' | 'BF') -> skip;  // Ignored (already captured as TEXT)
DEFORMATION: [0-9]'.'INT+ -> skip;        // Ignored numeric deformation values

INT: [0] | [1-9][0-9]*;                   // Non‑negative integer
TEXT: [A-Za-z0-9]+;                       // Alphanumeric string
BOND: EDGE ':' EDGE [CT];                 // e.g., W:WC (edge:edge + cis/trans)

WS: [ \t\r\n] -> skip;                    // Skip whitespace
COMMENT: '#' ~[?#\n]+ -> skip;            // Skip # comments


// ------------------------------------
// Fragments
// ------------------------------------

fragment EDGE: [WHSwhszg+];               // Valid edge characters
