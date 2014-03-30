grammar Chespel;

// /---------------\
// |    GRAMMAR    |
// \---------------/
program     :   (function)* rule (function | rule)* ;
function    :   type ID '(' list_params? ')' ':' block_instr ;
list_params :   type ID (',' type ID)* ;
rule        :   RULE ID rule_opt? ':' (expr '->')? block_instr ;
rule_opt    :   SYM ;

type        :   STRING_TYPE | list_type  ;
list_type   :   BOARD_TYPE | PIECE_TYPE | NUM_TYPE | BOOL_TYPE | '[' list_type ']' ;
block_instr :   '{' (instr (';' | NEWLINE))* instr (';' | NEWLINE)?  '}' ;
instr       :   (if
            |   while
            |   forall
            |   score
            |   decl
            |   assig)? 
            ;

// Definició de les diferents instruccions
if          :   IF expr ':' NEWLINE? block_instr (ELSE block_instr)? ;

while       :   WHILE expr ':' NEWLINE? block_instr

score       :   SCORE expr expr? ; // valor a afegir seguit de string de comentari

decl        :   type ID ((',' ID)+ | ('=' expr))? ; // múltiple declaració (int x, y) 
                                                    // o declaració més assignació  (int x = 3)

assig       :   ID (options{greedy=false;} : '=' ID)* ('=' expr) ;

// Definició de les expressions
expr        :   expr_and (OR expr_and)* ;
expr_and    :   expr_comp (AND expr_comp)* ;
expr_comp   :   expr_arit (COMP expr_arit)? ; // no hi poden haver quedenes de comparadors
expr_arit   :   expr_prod (PLUS expr_prod)* ;
expr_prod   :   atom (PROD atom)* ;
atom        :   '(' expr ')' | STRING | ID | NUM
            |   func_call 
            |   ROW_LIT | COLUMN_LIT | RANK_LIT | CELL_LIT | RANG_LIT
            ;
func_call   :   ID '(' (expr (',' expr)*)? ')' ;


// /---------------\
// |    TOKENS     |
// \---------------/

// instruccions
FORALL      :   'forall' ;
WHILE       :   'while' ;
IF          :   'if' ;
SCORE       :   'score' ;
ELSE        :   'else' ;


// paraules clau per definir rule
RULE        :   'rule' ;
SYM         :   'sym' ;

// tipus
BOARD_TYPE  :   'cell'|'row'|'file'|'rank' ;
PIECE_TYPE  :   'piece'|'pawn'|'bishop'|'rook'|'knight'|'king'|'queen' ;
NUM_TYPE    :   'num' ;
BOOL_TYPE   :   'bool' ;
STRING_TYPE :   'string' ;

// operacions
PLUS        :   '+' | '-' ;
PROD        :   '*' | '/' ;
AND         :   'and' ;
OR          :   'or' ;
COMP        :   '==' | '<' | '>' | '>=' | '<=' ;

// àtoms d'expressions
STRING      :   '"' ('a'..'z' | 'A'..'Z' | '0'..'9' | '!' | '#'..'/' | ':'..'@' | '['..'`' | '{'..'-' )* '"' ;
ID          :   ('a'..'z'|'A'..'Z')('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;
NUM         :   ('0'..'9')+('.' ('0'..'9')+)? | '.'('0'..'9')+ ;
ROW_LIT     :   '$' ('1'..'8') ;
COLUMN_LIT  :   '$' ('a'..'h')|('A'..'H') ;
RANK_LIT    :   '$'('r'|'R') ('1'..'8') ;
CELL_LIT    :   '$' ('a'..'h')|('A'..'H') ('1'..'8') ;
RANG_LIT    :   '$' (('1'..'8')'-'('1'..'8') 
            |   ('a'..'h')|('A'..'H')'-'('a'..'h')|('A'..'H') 
            |   ('a'..'h')|('A'..'H') ('1'..'8') '-' ('a'..'h')|('A'..'H') ('1'..'8'))
            ;

// addicionals
MULTI_LINE  :   '\' (' '|'\t')* '\r'? '\n' (skip();) ;
NEWLINE     :   '\r'? '\n' ;
WS          :   (' '|'\t')+ {skip();} ;