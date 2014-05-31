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
    LIST_VAR;
    LIST_ATOM;
    EMPTY_LIST;
    ACCESS_ATOM;
    PVALUE;     // Parameter by value in the list of parameters
    PREF;       // Parameter by reference in the list of parameters
    LIST_CONF;
    OPTION;
    PROGRAM;
    //VOID;
}

@header {
package parser;
import compiler.ChespelTree;
}

@lexer::header {
package parser;
}

prog    : program EOF -> ^(PROGRAM program)
        ;

program    : configs? definition+ -> ^(LIST_CONF configs?) ^(LIST_DEF definition+)
           ;

configs : CONFIG! '{'! config_assign+ '}'! ;

config_assign : ID eq=EQUAL config_values ';' -> ^(ASSIGN[$eq,":="] ID config_values) ;

config_values : ID | num_lit | ((i=TRUE | i=FALSE) -> ^(BOOL[$i,$i.text]))  ;
        
definition 
    :   func | rule | global_const
    ;
    
global_const
    :   GLOBAL type ID eq=EQUAL expr ';' -> ^(GLOBAL_DEF["GLOBAL"] type ID expr)
    ;
        
// A function has a name, a list of parameters and a block of instructions  
func    : function_type ID params block_instructions_strict -> ^(FUNCTION_DEF["FUNCTION"] function_type ID params block_instructions_strict)
        ;
        
rule        :   r=RULE rule_name rule_opt doif? block_instructions_strict -> ^(RULE_DEF["RULE"] rule_name rule_opt block_instructions_strict doif?)  ;

//allow some types/literals as rule names, as it does not create any ambiguity
rule_name
    :   (t=ID | t=PIECE_LIST | t=BOARD_LIST | t=PIECE_TYPE | t=BOARD_TYPE) -> ^(ID[$t,$t.text]);

rule_opt    :   (o+=option (',' o+=option)*)? -> ^(RULE_OPTIONS $o*) ;

option      :   SYM | 'opening' | 'midgame' | 'endgame' ; 

doif    :       d=DOIF expr -> ^(DOIF[$d,"DOIF"] expr) ;

params  : '(' paramlist? ')' -> ^(PARAMS paramlist?) ;

paramlist: param (','! param)* ;

param   :   type paramid -> ^(PARAM type paramid) 
        ;

// Parameters with & as prefix are passed by reference
// Only one node with the name of the parameter is created
paramid :   '&' id=ID -> ^(PREF[$id,'&' + $id.text])
        |   id=ID -> ^(PVALUE[$id,$id.text])
        ;

function_type  :   VOID_TYPE | type ;

type        :   STRING_TYPE | BOARD_TYPE | PIECE_TYPE | NUM_TYPE | BOOL_TYPE | L_BRACKET^ type R_BRACKET! ;
        
// A list of instructions, all gouped in a subtree
block_instructions_strict  :   ('{' ( instruction )* '}') -> ^(LIST_INSTR instruction*) ;
        
block_instructions
    :   block_instructions_strict |  (instruction -> ^(LIST_INSTR instruction)) ;
        
instruction
        :   assign ';'!         // Assignment
        |   decl ';'!           // Declare a variable
        |   ite_stmt            // if-then-else
        |   forall_stmt         // forall
        |   while_stmt          // while statement
        |   return_stmt ';'!    // return statement
        |   score ';'!          // change score
        |   funcall';'!         // function call
        |       ';'!            // nothing
        ;

assign  :   (ID (eq=EQUAL expr)+) -> ^(ASSIGN[$eq,":="] ID expr) ;

decl    :   type declOrAssignment (',' declOrAssignment)* -> ^(VAR_DECL type ^(LIST_VAR declOrAssignment+));

declOrAssignment  :   ID (eq=EQUAL expr -> ^(ASSIGN[$eq,":="] ID expr) | -> ^(ID)) ;

ite_stmt    :   IF^ '('! expr ')'!   block_instructions  ( options {greedy=true;} :  ELSE! block_instructions)? ;

while_stmt  :   WHILE^ '('! expr ')'! block_instructions ;

//if there is no expression, the token 'void' is added
return_stmt :   RETURN (expr -> ^(RETURN expr) | -> ^(RETURN VOID_TYPE)) ;

forall_stmt :   FORALL^ in_decl block_instructions ;

//'expr' is the list from which the variable ID draws values
in_decl  : '('! ID IN^ expr ')'! ;

//the first expression is the numerical value to add to the score
//the second, optional expression is the textual justification
score       :   SCORE^ expr (','! expr)? ;


expr    :   boolterm (OR^ boolterm)* ;

boolterm:   boolfact (AND^ boolfact)* ;

boolfact:   num_expr  ((DOUBLE_EQUAL^ | NOT_EQUAL^ | LT^ | LE^ | GT^ | GE^) num_expr)? ;


num_expr:   term ( (PLUS^ | MINUS^) term)* ;

term    :   factor ( (MUL^ | DIV^) factor)* ;

factor  :   (NOT^ | PLUS^ | MINUS^)? in_factor ;

in_factor   :  concat_atom (IN^ concat_atom)? ;

concat_atom :   access_atom (CONCAT^ access_atom)* ;

access_atom :   atom (DOT^ id_extended | L_BRACKET^ expr R_BRACKET!)*;  

//there are predefined functions whose name is the same as a type (such as 'cell')
//in this context they are functions, so we convert them to ID tokens
id_extended :   (t=PIECE_TYPE | t=BOARD_TYPE | t=ID)
            ->  ^(ID[$t,$t.text]);
    
atom    : ID
        | (b=TRUE | b=FALSE)  -> ^(BOOL[$b,$b.text])
        | funcall
        | STRING
        | board_lit
        | BOARD_LIST 
        | PIECE_LIST
        | num_lit
        | '('! expr ')'!
        | list
        | SELF | RIVAL
        ;

board_lit : FILE_LIT | ROW_LIT | RANK_LIT | CELL_LIT | rang_lit ;
rang_lit: RANG_CELL_LIT | RANG_ROW_LIT | RANG_RANK_LIT | RANG_FILE_LIT ;

//automatically transform numbers to internal representation
num_lit :   n=NUM {int numValue = (int) Math.round (Float.parseFloat($n.text) * 1000); $n.setText(String.valueOf(numValue));} ;

//function call with a (possibly empty) list of arguments
funcall :   (id_extended '(' expr_list? ')') -> ^(FUNCALL id_extended ^(ARGLIST expr_list?)) ;

list    :   L_BRACKET ((expr_list R_BRACKET -> ^(LIST_ATOM expr_list?)) | R_BRACKET -> ^(EMPTY_LIST["[]"]) ) ;

//list of expressions separated by commas
expr_list:  expr (','! expr)* ;


// Basic tokens
RANG_CELL_LIT:  '$' FILE_ID ROW_ID '..' FILE_ID ROW_ID ;
RANG_ROW_LIT:   '$' ROW_ID '..' ROW_ID ;
RANG_RANK_LIT:  '$' ('r'|'R') ROW_ID '..' ROW_ID ;
RANG_FILE_LIT:  '$' FILE_ID '..' FILE_ID ;

CELL_LIT    :   '$' FILE_ID ROW_ID ;
FILE_LIT  :   '$' FILE_ID ;
ROW_LIT     :   '$' ROW_ID ;
RANK_LIT    :   '$'('r'|'R') ROW_ID ;

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
BOARD_LIST
    :   'cells' | 'rows' | 'files' | 'ranks' ;
PIECE_LIST
    :   PIECE_MOD ('pieces' | 'pawns' | 'bishops' | 'rooks' | 'knights' | 'kings' | 'queens') ;

fragment PIECE_MOD
    :   ('s' | 'r' | ) ; // self, rival, no-modified
    
SELF    :   'self' ;
RIVAL   :   'rival' ;
    
NUM_TYPE    :   'num' ;
BOOL_TYPE   :   'bool' ;
STRING_TYPE :   'string' ;
VOID_TYPE   :   'void' ;
ELEMIN  :   'element in' ;
DOT :   '.' ;
RETURN  : 'return' ;
TRUE    : 'true'  ;
FALSE   : 'false' ;
CONFIG  : 'config' ;
ID      :   ('a'..'z'|'A'..'Z') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')* ;
NUM     :   (('0'..'9')+ ('.' ('0'..'9')+)?) | ('.'('0'..'9')+ );

// C-style comments
COMMENT : '//' ~('\n'|'\r')* '\r'? '\n' {$channel=HIDDEN;}
        | '/*' ( options {greedy=false;} : . )* '*/' {$channel=HIDDEN;}
        ;

// Strings (in quotes) with escape sequences        
STRING  :  '"' ( options{greedy=false;} : ( ESC_SEQ | ~('\\'|'"') ) )* '"'
        ;

fragment ESC_SEQ
    :   '\\' ('b'|'t'|'n'|'f'|'r'|'\"'|'\''|'\\')
    ;

fragment FILE_ID : ('a'..'h')|('A'..'H') ;
fragment ROW_ID : ('1'..'8') ;

// White spaces
WS      : ( ' '
        | '\t'
        | '\n'
        | '\r'
        ) {$channel=HIDDEN;}
        ;
