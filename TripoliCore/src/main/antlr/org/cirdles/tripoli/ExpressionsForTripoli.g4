/*
 * Copyright 2022 James Bowring, Noah McLean, Scott Burdick, and CIRDLES.org.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

 grammar ExpressionsForTripoli;

 @header {
    package org.cirdles.tripoli; //- this causes problems with maven by writing twice
 }

 /*file:   (functionDecl | varDecl)+ ;

 varDecl
     :   type ID ('=' expr)? ';'
     ;
 type:   'float' | 'int' | 'void' ; // user-defined types

 functionDecl
     :   type FUNCTION '(' formalParameters? ')' block // "void f(int x) {...}"
     ;
 formalParameters
     :   formalParameter (',' formalParameter)*
     ;
 formalParameter
     :   type ID
     ;

 block:  '{' stat* '}' ;   // possibly empty statement block
 stat:   block
     |   varDecl
     |   'if' expr 'then' stat ('else' stat)?
     |   'return' expr? ';'
     |   expr '=' expr ';' // assignment
     |   expr ';'          // func call
     ;
 */

 // http://meri-stuff.blogspot.com/2011/09/antlr-tutorial-expression-language.html

 expr:   FUNCTION WS* '(' exprList+ WS* ')'    // func call like f(), f(x), f(1,2) switched ? to + to require 1 arg min
     |   expr expr
     |   '(' expr ')'
     |   '-' expr                // unary minus
     |   '!' expr                // boolean not
     |   expr ('*'|'/') expr
     |   expr ('+'|'-') expr
     |   expr ('^') expr
     |   WS expr
     |   expr WS
     |   ARRAY_CALL
     |   NAMED_EXPRESSION
     |   ID                      // variable reference
     |   DOUBLE
     |   INT
     ;
 exprList : expr (',' expr)* ;   // arg list

 // provides for case-insensitive function names
 fragment A:('a'|'A');
 fragment B:('b'|'B');
 fragment C:('c'|'C');
 fragment D:('d'|'D');
 fragment E:('e'|'E');
 fragment F:('f'|'F');
 fragment G:('g'|'G');
 fragment H:('h'|'H');
 fragment I:('i'|'I');
 fragment J:('j'|'J');
 fragment K:('k'|'K');
 fragment L:('l'|'L');
 fragment M:('m'|'M');
 fragment N:('n'|'N');
 fragment O:('o'|'O');
 fragment P:('p'|'P');
 fragment Q:('q'|'Q');
 fragment R:('r'|'R');
 fragment S:('s'|'S');
 fragment T:('t'|'T');
 fragment U:('u'|'U');
 fragment V:('v'|'V');
 fragment W:('w'|'W');
 fragment X:('x'|'X');
 fragment Y:('y'|'Y');
 fragment Z:('z'|'Z');

 FUNCTION :
     A G E P B '76' |
     A G E P B '76' W I T H E R R |
     A G E '7' C O R R W I T H E R R |
     A G E '8' C O R R W I T H E R R |
     A G E '7' C O R R P B '8' T H '2' W I T H E R R |
     R A D '8' C O R P B '7' U '5' W I T H E R R |
     R A D '8' C O R C O N C R H O |
     P B '76' |
     P B '46' C O R '7' |
     P B '46' C O R '8' |
     P B '206' U '238' R A D |
     S T D P B '86' R A D C O R '7' P E R |
     P B '86' R A D C O R '7' P E R |
     A N D |
     O R |
     C O N C O R D I A T W |
     C O N C O R D I A |
     E X P |
     I F |
     N O T |
     L N |
     L O G |
     S Q R T |
     R O B R E G |
     S Q B I W E I G H T |
     B I W E I G H T |
     S Q W T D A V |
     W T D A V |
     T O T A L C P S |
     T O T A L C P S T I M E |
     M A X |
     M I N |
     A B S |
     A V E R A G E |
     M E D I A N |
     S U M |
     C O U N T |
     C O U N T I F |
     T I N V |
     R O U N D |
     S T D E V |
     S T D E V P |
     C A L C U L A T E M E A N C O N C S T D |
     W T D M E A N A C A L C |
     V A L U E M O D E L |
     O R I G
 ;

 ARRAY_CALL : (ID | NAMED_EXPRESSION) ((' ')* '[' INT ']' (' ')*);       // array index like a[1]

 NAMED_EXPRESSION : '[' (' ')? ('\u00B1')? ('%')? '"' ID (ID | '/' | ' ' | '*' | '.' | '_' | '%' | '-')* PARENS* (' %err')* '"' ']' ;

 ID  :   (LETTER | NUMBER) (LETTER | NUMBER)* ;
 fragment
 PARENS : '(' (LETTER | NUMBER | '.' | ' ')* ')';

 LETTER : [a-zA-Z_] ;

 NUMBER : [0-9] ;

 INT :   [0-9]+ ;

 INTEGER:                '0' | ([1-9][0-9]*);

 DOUBLE :              ('0' | ([1-9][0-9]*)) ('.' [0-9]*)? Exponent? ;

 fragment
 Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;


 WS  :   (' ' | '\t' | '\n' | '\r') ;

 SL_COMMENT
     :   '//' .*? '\n' -> skip
     ;