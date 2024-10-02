grammar Expressions;


// Lexer rules
DIGIT: [0-9];
PLUS: '+';
MULT: '*';
DIV: '/';
MINUS: '-';
EQ: '=';
PARENL: '(';
PARENR: ')';
WS: [ \t\n\r]+ -> skip;

// Parser rules
//expression:  sum ((PLUS| MINUS) sum)* (EQ number)? ;
//sum: number ((MULT| DIV) number)*;
//number: DIGIT+;

//
 start: expression EOF;
 expression: PARENL expression PARENR | expression (MULT| DIV) expression | expression (PLUS| MINUS) expression | DIGIT+;
