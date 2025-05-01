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

 neg_number: NEG_NUMBER;
 namedFunction : NAMED_EXPRESSION;

 expr
     :   expr expr
     |   '(' expr ')'
     |   neg_number
     |   '-' expr                // unary minus
     |   '!' expr                // boolean not
     |   expr ('*'|'/') expr
     |   expr ('+'|'-') expr
     |   expr ('^') expr
     |   WS expr
     |   expr WS
     |   NAMED_EXPRESSION
     |   DOUBLE
     |   INT
     ;

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

 NAMED_EXPRESSION : '[' (' ')? ('\u00B1')? ('%')? ID (ID | '/' | ' ' | '*' | '.' | '_' | '%' | '-' | ':')* Parens* (' %err')* ']' ;

 ID  : (LETTER | NUMBER) (LETTER | NUMBER)* ;

 fragment
 Parens : '(' (LETTER | NUMBER | '.' | ' ')* ')';

 LETTER : [a-zA-Z_] ;

 NEG_NUMBER: '-' [0-9]+ ('.' [0-9]+)?;

 NUMBER : [0-9] ;

 INT : [0-9]+ ;

 INTEGER : '0' | ([1-9][0-9]*);

 DOUBLE : ('0' | ([1-9][0-9]*)) ('.' [0-9]*)? Exponent? ;

 fragment
 Exponent : ('e'|'E') ('+'|'-')? ('0'..'9')+ ;


 WS : (' ' | '\t' | '\n' | '\r') ;

 SL_COMMENT
     :   '//' .*? '\n' -> skip
     ;