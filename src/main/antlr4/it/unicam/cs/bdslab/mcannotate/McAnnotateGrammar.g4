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
 * Combined grammar for mc-annotate output files.
 *
 * Parses sections: Residue conformations, Adjacent stackings,
 * Non-Adjacent stackings, and Base-pairs, including summary counts.
 *
 * @author Francesco Palozzi
 */
grammar McAnnotateGrammar;

// ------------------------------------------------
// Parser rules
// ------------------------------------------------

mcAnnotateFile
    : residueSection
      adjacentSection
      nonAdjacentSection
      countSection
      basePairsSection
      EOF
    ;                                         // File structure: all sections in order

residueSection
    : RESIDUE_HEADER residueLine*
    ;                                         // Header followed by zero or more residue lines

adjacentSection
    : ADJACENT_HEADER adjacentLine*
    ;                                         // Adjacent stacking section

nonAdjacentSection
    : NON_ADJ_HEADER nonAdjacentLine*
    ;                                         // Non-adjacent stacking section

countSection
    : countLine+
    ;                                         // One or more count summary lines

basePairsSection
    : BASE_PAIRS_HEADER basePairLine*
    ;                                         // Base-pairs section

residueLine
    : IDENTIFIER COLON IDENTIFIER SUGAR? ANTI_SYN?
    ;                                         // e.g., A:U C3p_endo anti

adjacentLine
    : PAIR_ID COLON ADJACENT_5P DIRECTION PAIRING?
    ;                                         // e.g., A1-U2: adjacent_5p outward

nonAdjacentLine
    : PAIR_ID COLON DIRECTION PAIRING?
    ;                                         // e.g., A1-U8: inward

basePairLine
    : PAIR_ID COLON NUCLEOTIDE_PAIR BOND+
      ( ADJACENT_5P | DIRECTION | PAIRING | ORIENTATION | ADDITIONAL | SAENGER )*
    ;                                         // es. A98-A99 : G-U Sw/Hw O2'/Hh adjacent_5p pairing parallel cis one_hbond 89

countLine
    : COUNT_STACKINGS
    | COUNT_ADJ
    | COUNT_NON_ADJ
    ;                                         // One of three count types


// ------------------------------------------------
// Lexer rules
// ------------------------------------------------

WS          : [ \t\r\n]+ -> skip ;            // Skip whitespace

// Section headers (dashed lines)
fragment DASH : '-' ;
RESIDUE_HEADER    : 'Residue conformations' WS* DASH+ ;
ADJACENT_HEADER   : 'Adjacent stackings' WS* DASH+ ;
NON_ADJ_HEADER    : 'Non-Adjacent stackings' WS* DASH+ ;
BASE_PAIRS_HEADER : 'Base-pairs' WS* DASH+ ;

// Count summary lines
COUNT_STACKINGS : 'Number of stackings =' WS* INT ;
COUNT_ADJ       : 'Number of adjacent stackings =' WS* INT ;
COUNT_NON_ADJ   : 'Number of non adjacent stackings =' WS* INT ;

// atomic token keyword
ADJACENT_5P : 'adjacent_5p' ;
DIRECTION   : 'outward' | 'downward' | 'inward' | 'upward' ;
PAIRING     : 'pairing' ;
ORIENTATION : 'antiparallel' | 'parallel' | 'cis' | 'trans' ;
ANTI_SYN    : 'anti' | 'syn' ;
SAENGER: [XVI]+;                              // Saenger classification (e.g., XI, VI)

SUGAR       : [a-zA-Z0-9_]+? ( 'endo' | 'exo' ) ;  // Sugar pucker (e.g., C3p_endo)

PAIR_ID : RESREF '-' RESREF ;                 // C36-C104  or  '3'1-'3'120
fragment RESREF
    : '\'' [A-Za-z0-9]+ '\'' [0-9]+           // number chain:  '3'1
    | [A-Za-z] [0-9]+                         // letter chain:  C1
    ;

IDENTIFIER
    : '\'' [A-Za-z0-9]+ '\'' [0-9]+           // residue id, number chain:  '3'1
    | [A-Za-z] [A-Za-z0-9]*                   // residue id (C1) or nucleotide (A,U,G,C)
    ;

COLON           : ':' ;
NUCLEOTIDE_PAIR : [ACGU] '-' [ACGU] ;
BOND            : [a-zA-Z0-9'/]+ '/' [a-zA-Z0-9'/]+ ;  // W/W, O2'/Ww, Hh/O2', ...
ADDITIONAL      : [a-zA-Z0-9_]+ ;             // antiparallel, parallel, one_hbond, 130, ...
INT             : [0-9]+ ;
