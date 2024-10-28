grammar ICSS;

//--- LEXER: ---

// IF support:
IF: 'if';
ELSE: 'else';
BOX_BRACKET_OPEN: '[';
BOX_BRACKET_CLOSE: ']';


//Literals
TRUE: 'TRUE';
FALSE: 'FALSE';
PIXELSIZE: [0-9]+ 'px';
PERCENTAGE: [0-9]+ '%';
SCALAR: [0-9]+;


//Color value takes precedence over id idents
COLOR: '#' [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f] [0-9a-f];

//Specific identifiers for id's and css classes
ID_IDENT: '#' [a-z0-9\-]+;
CLASS_IDENT: '.' [a-z0-9\-]+;

//General identifiers
LOWER_IDENT: [a-z] [a-z0-9\-]*;
CAPITAL_IDENT: [A-Z] [A-Za-z0-9_]*;

//All whitespace is skipped
WS: [ \t\r\n]+ -> skip;

//
OPEN_BRACE: '{';
CLOSE_BRACE: '}';
SEMICOLON: ';';
COLON: ':';
PLUS: '+';
MIN: '-';
MUL: '*';
DIV: '/';
MOD: '%';
ASSIGNMENT_OPERATOR: ':=';
COMMA: ',';




//--- PARSER: ---
stylesheet: variabledecleration* stylerule* EOF;
stylerule: selector OPEN_BRACE (declaration | conditional_block | variabledecleration)* CLOSE_BRACE;

declaration: property COLON expression+ SEMICOLON;
variabledecleration: variableReference ASSIGNMENT_OPERATOR expression+ SEMICOLON;
selector: (tagSelector | classSelector | idSelector) (COMMA selector)*;

conditional_block: if_block else_block?;
if_block: IF BOX_BRACKET_OPEN condition BOX_BRACKET_CLOSE OPEN_BRACE (declaration | variabledecleration| conditional_block)* CLOSE_BRACE;
else_block: ELSE OPEN_BRACE (declaration | conditional_block)* CLOSE_BRACE;
condition: variableReference| boolLiteral;


expression: literal| expression (MUL | DIV | MOD) expression | expression (PLUS | MIN) expression;
classSelector: CLASS_IDENT;
tagSelector: LOWER_IDENT;
idSelector: ID_IDENT | COLOR;
operator: PLUS | MIN | MUL | DIV | MOD;
property: (ID_IDENT | CAPITAL_IDENT | LOWER_IDENT)+;

literal: boolLiteral | colorLiteral | pixelLiteral | scalarLiteral | percentageLiteral| variableReference;
variableReference: CAPITAL_IDENT;
boolLiteral: TRUE | FALSE;
colorLiteral: COLOR;
pixelLiteral: PIXELSIZE;
scalarLiteral: SCALAR;
percentageLiteral: PERCENTAGE;