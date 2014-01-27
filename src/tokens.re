
SPACE "' '"
TAB "'\t' | '    '"
NEWLINE "'\n'"

ALL_CHAR_EXCEPT_NEWLINE "TAB | [' '-'~']"
COMMENT "'//' ALL_CHAR_EXCEPT_NEWLINE*"

ALL_CHAR_EXCEPT_* "TAB | NEWLINE | [' '-')'] | ['+'-'~']"
ALL_CHAR_EXCEPT_/ "TAB | NEWLINE | [' '-'.'] | ['0'-'~']"
MULTICOMMENT "'/*' (ALL_CHAR_EXCEPT_* | '*' ALL_CHAR_EXCEPT_/)* ('*'|) '*/'"

KEYWORD "'sym' | 'rule' |
 'bool' | 'string' | 'num' |
 'piece' | 'cell' | 'row' | 'col' | 'type' | 'player' | 'color' |
 'with' | 'if' | 'score' | 'in' |
 'value' | 'totalValue' | 'check' | 'castled' |
 'rank' | 'startingRow' |
 'pawn' | 'knight' | 'bishop' | 'rock' | 'queen' | 'king' |
 '$' ['1'-'8'] | '$' ['a'-'h'] | 
 'P' | 'N' | 'B' | 'R' | 'Q' | 'K' |
 'true' | 'false' | 'white' | 'black' | 'me' | 'foe'"

BOOL_OP "'and' | 'or' | 'not'"
ARITH_OP "'+' | '-' | '*' | '/'"
EQ_OP "'==' | '!='"

OPEN_P "'('"
CLOSE_P "')'"

COLON "':'"
PERIOD "'.'"
COMMA "','"
SEMICOLON "';'"

DIGIT "['0'-'9']"
NUM "('-'|) DIGIT+ ('.' DIGIT+|)"

LETTER "['a'-'z'] | ['A'-'Z']"
ID "LETTER (LETTER | DIGIT | '_')*"

