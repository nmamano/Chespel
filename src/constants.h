#ifndef Constants_Included
#define Constants_Included

#include <string>

using namespace std;


const string tokens[] = {
	"num", "id", "string",
	"space", "tab", "newline",
	"comma", "colon", "semicolon", "period",
	"openParentheses", "closedParentheses",
	"openBrackets", "closedBrackets",
	"sumOperator", "subOperator", "productOperator", "divisionOperator",
	"concatOperator",
	"LTComparison", "GTComparison", "LEComparison", "GEComparison",
	"EQComparison", "NEComparison", "assignment",
	"moduleKeyword", "moduleName",
	"symKeyword", "ruleKeyword", "pieceKeyword", "cellKeyword", "withKeyword",
	"ifKeyword", "scoreKeyword", "letKeyword", "inKeyword",
	"rowConstant", "colConstant", "cellConstant", "playerConstant",
	"typeConstant", "pieceConstant", "boolConstant",
	"wrongToken"
};

const string typeValues[] = {
	"pawn","knight","bishop","rock","queen","king",
	"P", "N", "B", "R", "Q", "K"
};

const string boolValues[] = {
	"true", "false"
};

const string playerValues[] = {
	"me", "foe"
};

const string cellValues[] = {
	"a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8",
	"b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8",
	"c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8",
	"d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8",
	"e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8",
	"f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8",
	"g1", "g2", "g3", "g4", "g5", "g6", "g7", "g8",
	"h1", "h2", "h3", "h4", "h5", "h6", "h7", "h8"
};

const string colValues[] = {
	"$a","$b","$c","$d","$e","$f","$g","$h"
};

const string rowValues[] = {
	"$1","$2","$3","$4","$5","$6","$7","$8"
};

const string reservedWords[] = {
	"seach", "evaluation", "opening", "endgame"
	"module",
	"sym", "rule",
	"score", "let", "if",
	"cell", "piece", "with",
	"in"
};

const string operators[] = {
	"+", "-", "*", "/",
	"<", ">", "<=", ">=", "==", "!=",
	"=",
	"++"
};

const string syntaxParticles[] = {
	",", ":", ";", ".",
	"(", ")", "[", "]"
};

const string spaces[] = {
	"\n", "\t", " "
};


#endif