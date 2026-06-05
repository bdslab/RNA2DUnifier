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
 * ANTLR 4 grammar for barnaba output files.
 *
 * This parser defines the grammar rules used to parse barnaba interaction files.
 * The input typically consists of residue pairs with their positions and
 * interaction annotations (base pairing, stacking, etc.).
 *
 * @author Francesco Palozzi
 */
grammar BarnabaGrammar;


// ------------------------------------
// Parser rules
// ------------------------------------

barnabaFile: commentLine* interactionLine* EOF;        // File with optional comments then interactions

residueSpec: NUCLEOTIDE '_' INT '_' INT;               // e.g., A_1_2 (base_index1_index2)

interactionLine: residueSpec residueSpec ANNOTATION;   // Two residues + interaction type

commentLine: COMMENT;                                  // Single comment line


// ------------------------------------
// Lexer rules
// ------------------------------------

NUCLEOTIDE: [ACGUacguTtRrYysSWwKkMmBbDdHhVvNn];        // Standard and degenerate nucleotide codes

INT: [0-9]+;                                           // Integer (position indices)

ANNOTATION: ( [GUWCHS] [GUWCHS] [ct] )                 // Base-pair edge annotation (e.g., WWc)
          | ( [<>] [<>] )                              // Stack annotations: >>, <<, <>, ><
          | 'XXX';                                     // Unknown annotation placeholder

COMMENT: '#' ~[\r\n]+;                                 // Line comment (ignored)

WS: [ \t\r\n]+ -> skip;                                // Skip whitespace
