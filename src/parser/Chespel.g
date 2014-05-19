/**
 * Copyright (c) 2011, Jordi Cortadella
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *    * Redistributions of source code must retain the above copyright
 *      notice, this list of conditions and the following disclaimer.
 *    * Redistributions in binary form must reproduce the above copyright
 *      notice, this list of conditions and the following disclaimer in the
 *      documentation and/or other materials provided with the distribution.
 *    * Neither the name of the <organization> nor the
 *      names of its contributors may be used to endorse or promote products
 *      derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL <COPYRIGHT HOLDER> BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

grammar Chespel;

options {
    output = AST;
    ASTLabelType = ChespelTree;
}

// Imaginary tokens to create some AST nodes

tokens {
    LIST_DEF; // List of functions (the root of the tree)
    ASSIGN;     // Assignment instruction
    GLOBAL_DEF;
    PARAMS;     // List of parameters in the declaration of a function
    PARAM;
    FUNCALL;    // Function call
    ARGLIST;    // List of arguments passed in a function call
    LIST_INSTR; // Block of instructions
    BOOL;    // Bool atom (for boolean constants "true" or "false")
    FUNCTION_DEF;
    RULE_DEF;
    RULE_OPTIONS;
    VAR_DECL;
    LIST_ATOM;
    EMPTY_LIST;
    ACCESS_ATOM;
    PVALUE;     // Parameter by value in the list of parameters
    PREF;       // Parameter by reference in the list of parameters
    //VOID;
}

@header {
package parser;
import compiler.ChespelTree;
}

@lexer::header {
package parser;
}

// A program is a list of functions and rules
prog    : definition+ EOF -> ^(LIST_DEF definition+)
        ;
        
definition 
    :   func | rule | global_const
    ;
    
global_const
    :   GLOBAL type ID eq=EQUAL expr ';' -> ^(GLOBAL_DEF["GLOBAL"] type ID expr)
    ;
        
// A function has a name, a list of parameters and a block of instructions  
func    : function_types ID params block_instructions_strict -> ^(FUNCTION_DEF["FUNCTION"] function_types ID params block_instructions_strict)
        ;
        
rule        :   r=RULE rule_name rule_opt doif? block_instructions_strict -> ^(RULE_DEF["RULE"] rule_name rule_opt? doif? block_instructions_strict)  ;

rule_name
    :   t=ID | t=PIECE_LIT | t=BOARD_LIT | t=PIECE_TYPE | t=BOARD_TYPE -> ^(ID[$t,$t.text]);

rule_opt    :   (o+=option (',' o+=option)*)? -> ^(RULE_OPTIONS $o*) ;

option      :   SYM | 'opening' | 'midgame' | 'endgame' ; 

doif    :       d=DOIF expr -> ^(DOIF[$d,"DOIF"] expr) ;

// The list of parameters grouped in a subtree (it can be empty)
params  : '(' paramlist? ')' -> ^(PARAMS paramlist?)
        ;

// Parameters are separated by commas
paramlist: param (','! param)*
        ;

// Only one node with the parameter and its type is created
param   :   type paramid -> ^(PARAM type paramid) 
        ;

// Parameters with & as prefix are passed by reference
// Only one node with the name of the parameter is created
paramid :   '&' id=ID -> ^(PREF[$id,'&' + $id.text])
        |   id=ID -> ^(PVALUE[$id,$id.text])
        ;

// types
function_types  :   VOID_TYPE | type ;

type        :   STRING_TYPE | BOARD_TYPE | PIECE_TYPE | NUM_TYPE | BOOL_TYPE | L_BRACKET^ type R_BRACKET! ;
        
// A list of instructions, all of them gouped in a subtree
block_instructions_strict
        :   '{' ( instruction )* '}' -> ^(LIST_INSTR instruction+)
            
        ;
        
block_instructions
    :   block_instructions_strict |  (instruction -> ^(LIST_INSTR instruction)) ;
        
        

// The different types of instructions
instruction
        :   assign ';'!         // Assignment
        |   decl ';'!          // Declare a variable
        |   ite_stmt        // if-then-else
        |       forall_stmt     // forall
        |   while_stmt      // while statement
        |   return_stmt ';'!    // Return statement
        |   score ';'!           // Change score
        |       ';'!            // Nothing
        ;

// Assignment
assign  :   ID (eq=EQUAL expr)+ -> ^(ASSIGN[$eq,":="] ID expr)
        ;

decl        :   type declWithAssignation (',' declWithAssignation)* -> ^(VAR_DECL type declWithAssignation+) ;

declWithAssignation
    :   ID (eq=EQUAL expr -> ^(ASSIGN[$eq,":="] ID expr))? ;


score       :   SCORE^ expr (','! expr)? ; // valor a afegir seguit de string de comentari

// forall
forall_stmt
    :   FORALL^ in_decl block_instructions
    ;

in_decl
    : '('! ID IN^ expr ')'!
    ;

// if-then-else (else is optional)
ite_stmt    :   IF^ '('! expr ')'!   block_instructions  ( options {greedy=true;} :  ELSE! block_instructions)?
            ;

// while statement
while_stmt  :   WHILE^ '('! expr ')'! block_instructions
            ;

// Return statement with an expression
return_stmt :   RETURN (expr -> ^(RETURN expr) | -> ^(RETURN VOID_TYPE))
        ;

// Grammar for expressions with boolean, relational and aritmetic operators
expr    :   boolterm (OR^ boolterm)*
        ;

boolterm:   boolfact (AND^ boolfact)*
        ;

boolfact:   num_expr  ((IN^ |DOUBLE_EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^) num_expr)?
        ;


num_expr:   term ( (PLUS^ | MINUS^) term)*
        ;

term    :   factor ( (MUL^ | DIV^) factor)*
        ;

factor  :   (NOT^ | PLUS^ | MINUS^)? concat_atom 
        ;

concat_atom
    :   access_atom (CONCAT^ access_atom)* ;

access_atom
    :   atom (DOT^ id_extended | L_BRACKET^ expr R_BRACKET!)*;  

id_extended
    :   t=PIECE_TYPE | t=BOARD_TYPE | t=ID 
            ->  ^(ID[$t,$t.text]); // anything that comes, convert to ID token
    
atom    : ID
        | (b=TRUE | b=FALSE)  -> ^(BOOL[$b,$b.text])
        | funcall
        | STRING
        | ROW_LIT
        | FILE_LIT | RANK_LIT | CELL_LIT | rang_lit 
        | BOARD_LIT 
        | PIECE_LIT
        | n=NUM {int numValue = (int) Math.round (Float.parseFloat($n.text) * 1000); $n.setText(String.valueOf(numValue));}
        | '('! expr ')'!
        | L_BRACKET ((expr_list R_BRACKET -> ^(LIST_ATOM expr_list?))| R_BRACKET -> ^(EMPTY_LIST["[]"]) )
        | SELF | RIVAL
        ;
    
// A function call has a lits of arguments in parenthesis (possibly empty)
funcall :   ID '(' expr_list? ')' -> ^(FUNCALL ID ^(ARGLIST expr_list?))
        ;

// A list of expressions separated by commas
expr_list:  expr (','! expr)*
        ;

rang_lit: RANG_CELL_LIT | RANG_ROW_LIT | RANG_RANK_LIT | RANG_FILE_LIT ;

// Basic tokens
//RANG_LIT    :   '$' ( (FILE_ID ('-' FILE_ID | ROW_ID '-' FILE_ID ROW_ID)) |  ROW_ID '-' ROW_ID);
RANG_CELL_LIT:  '$' FILE_ID ROW_ID '..' FILE_ID ROW_ID ;
RANG_ROW_LIT:   '$' ROW_ID '..' ROW_ID ;
RANG_RANK_LIT:  '$' ('r'|'R') ROW_ID '..' ROW_ID ;
RANG_FILE_LIT:  '$' FILE_ID '..' FILE_ID ;

CELL_LIT    :   '$' FILE_ID ROW_ID ;
FILE_LIT  :   '$' FILE_ID ;
ROW_LIT     :   '$' ROW_ID ;
RANK_LIT    :   '$'('r'|'R') ROW_ID ;


//ARROW :   '->' ;
DOIF    :   'do if';
EQUAL   : '=' ;
DOUBLE_EQUAL
    :   '==' ;
NOT_EQUAL: '!=' ;
LT      : '<' ;
LE      : '<=';
GT      : '>';
GE      : '>=';
PLUS    : '+' ;
MINUS   : '-' ;
MUL     : '*';
DIV     : '/';
//MOD       : '%' ;
L_BRACKET:  '[';
R_BRACKET:  ']';
GLOBAL  :   'global';
NOT     : 'not';
AND     : 'and' ;
OR      : 'or' ;    
IF      : 'if' ;
ELSE    : 'else' ;
WHILE   : 'while' ;
FORALL  : 'forall' ;
SCORE       :   'score' ;
RULE        :   'rule' ;
SYM         :   'sym' ;
BOARD_TYPE  :   'cell'|'row'|'file'|'rank' ;
PIECE_TYPE  :   'piece'|'pawn'|'bishop'|'rook'|'knight'|'king'|'queen' ;
IN  :   'in' ;
CONCAT: '++' ;
BOARD_LIT
    :   'cells' | 'rows' | 'files' | 'ranks' ;
PIECE_LIT
    :   PIECE_MOD ('pieces' | 'pawns' | 'bishops' | 'rooks' | 'knights' | 'kings' | 'queens') ;

fragment
PIECE_MOD
    :   ('s' | 'r' | ) ; // self, rival, no-modified
    
SELF    :   'self' ;
RIVAL   :   'rival' ;
    
NUM_TYPE    :   'num' ;
BOOL_TYPE   :   'bool' ;
STRING_TYPE :   'string' ;
VOID_TYPE   :   'void' ;
ELEMIN  :   'element in' ;
//CHECK :   'check' ;
DOT :   '.' ;
//FILEON   : ':' ;
RETURN  : 'return' ;
TRUE    : 'true' | 'yes' ;
FALSE   : 'false' | 'no' ;
ID      :   ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;
NUM     :   (('0'..'9')+ ('.' ('0'..'9')+)?) | ('.'('0'..'9')+ );




// C-style comments
COMMENT : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
        | '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
        ;

// Strings (in quotes) with escape sequences        
STRING  :  '"' ( options{greedy=false;} : ( ESC_SEQ | ~('\\'|'"') ) )* '"'
        ;

fragment
ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    ;
    
fragment
FILE_ID : ('a'..'h')|('A'..'H') ;

fragment
ROW_ID : ('1'..'8') ;

// Newline
//NEWLINE     :   '\r'? '\n' ;

// White spaces
WS      : ( ' '
        | '\t'
        | '\n'
        | '\r'
        ) {$channel=HIDDEN;}
        ;
