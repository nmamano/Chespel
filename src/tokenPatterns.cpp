#include "tokenPatterns.h"
#include "utils.h"
#include <vector>
#include <string>
#include <utility>
#include <iostream>

using namespace std;

//auxiliar methods

bool matchString(const vector<char>& token, string s) {
	string aux = vector2string(token);
	return s == aux;
}

bool matchChar(const vector<char>& token, char c) {
	if (token.size() == 1 and token[0] == c) {
		return true;
	}
	return false;
}

bool isDigit(char c) {
	if (c >= '0' and c <= '9') {
		return true;
	}
	return false;
}

bool isLowerCase(char c) {
	if (c >= 'a' and c <= 'z') {
		return true;
	}
	return false;
}

bool isUpperCase(char c) {
	if (c >= 'A' and c <= 'Z') {
		return true;
	}
	return false;
}

bool isLetter(char c) {
	if (isLowerCase(c) or isUpperCase(c)) {
		return true;
	}
	return false;
}

bool isPosNum(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return false;

	//must start with digit
	if (not isDigit(token[0])) return false;

	//must end with digit
	if (not isDigit(token[s-1])) return false;

	//the rest are digits, except possibly a period
	bool foundPeriod = false;
	for (int i = 1; i < s-1; ++i) {
		if (token[i] == '.') {
			//can't have more than one decimal point
			if (foundPeriod) return false;
			foundPeriod = true;
		}
		else if (not isDigit(token[i])) return false;
	}

	return true;
}




//token patterns

bool isNum(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return false;

	//token starts with '-': it is num if what is left removing the '-' is a positive number
	if (token[0] == '-') {
		vector<char> copy(s-1);
		for (int i = 0; i < s-1; ++i) {
			copy[i] = token[i+1];
		}
		return isPosNum(copy);
	}

	return isPosNum(token);
}

bool isId(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return false;
	if (not isLowerCase(token[0])) return false;
	for (int i = 1; i < s; ++i) {
		if (not (isLetter(token[i]) or isDigit(token[i]) or token[i] == '_')) {
			return false;
		}
	}
	return true;
}

bool isComment(const vector<char>& token) {
	int s = token.size();	
	//a comment has two starting '/' and an ending '\n' 
	if (s < 3) return false;
	
	if (token[0] != '/' or token[1] != '/' or token[s-1] != '\n') {
		return false;
	}
	//it can't have endlines within
	for (int i = 2; i < s-1; ++i) {
		if (token[i] == '\n') {
			return false;
		}
	}
	return true;
}

bool isMultilineComment(const vector<char>& token) {
	int s = token.size();
	if (s < 4) return false;

	if (token[0] != '/' or token[1] != '*' or token[s-2] != '*' or token[s-1] != '/') {
		return false;
	}
	//it can't have */ within
	for (int i = 2; i < s-3; ++i) {
		if (token[i] == '*' and token[i+1] == '/') {
			return false;
		}
	}
	return true;
}

bool isString(const vector<char>& token) {
	int s = token.size();	 
	if (s < 2) return false;
	
	if (token[0] != '"' or token[s-1] != '"') {
		return false;
	}
	//it can't have " within
	for (int i = 1; i < s-1; ++i) {
		if (token[i] == '"') {
			return false;
		}
	}
	return true;
}


bool isSpace(const vector<char>& token) {
	return matchChar(token, ' ');
}

bool isTab(const vector<char>& token) {
	return matchChar(token, '\t');
}

bool isNewline(const vector<char>& token) {
	return matchChar(token, '\n');
}


bool isComma(const vector<char>& token) {
	return matchChar(token, ',');
}

bool isColon(const vector<char>& token) {
	return matchChar(token, ':');
}

bool isSemicolon(const vector<char>& token) {
	return matchChar(token, ';');
}

bool isPeriod(const vector<char>& token) {
	return matchChar(token, '.');
}


bool isOpenParentheses(const vector<char>& token) {
	return matchChar(token, '(');
}

bool isClosedParentheses(const vector<char>& token) {
	return matchChar(token, ')');
}

bool isOpenBrackets(const vector<char>& token) {
	return matchChar(token, '[');
}

bool isClosedBrackets(const vector<char>& token) {
	return matchChar(token, ']');
}


bool isSumOperator(const vector<char>& token) {
	return matchChar(token,'+');
}

bool isRestOperator(const vector<char>& token) {
	return matchChar(token, '-');
}

bool isProductOperator(const vector<char>& token) {
	return matchChar(token, '*');
}

bool isDivisionOperator(const vector<char>& token) {
	return matchChar(token, '/');
}


bool isLTComparison(const vector<char>& token) {
	return matchChar(token, '<');
}

bool isGTComparison(const vector<char>& token) {
	return matchChar(token, '>');
}

bool isLEComparison(const vector<char>& token) {
	return matchString(token, "<=");
}

bool isGEComparison(const vector<char>& token) {
	return matchString(token, ">=");
}

bool isEQComparison(const vector<char>& token) {
	return matchString(token, "==");
}

bool isNEComparison(const vector<char>& token) {
	return matchString(token, "!=");
}


bool isAssignment(const vector<char>& token) {
	return matchChar(token, '=');
}


bool isModuleKeyword(const vector<char>& token) {
	return matchString(token, "module");
}

bool isModuleName(const vector<char>& token) {
	string s = vector2string(token);
	string values[] = {"seach","evaluation","opening","endgame"};
	for (int i = 0; i < 4; ++i) {
		if (s == values[i]) return true;
	}
	return false;
}

bool isSymKeyword(const vector<char>& token) {
	return matchString(token, "sym");
}

bool isRuleKeyword(const vector<char>& token) {
	return matchString(token, "rule");
}

bool isPieceKeyword(const vector<char>& token) {
	return matchString(token, "piece");
}

bool isCellKeyword(const vector<char>& token) {
	return matchString(token, "cell");
}

bool isWithKeyword(const vector<char>& token) {
	return matchString(token, "with");
}

bool isIfKeyword(const vector<char>& token) {
	return matchString(token, "if");
}

bool isScoreKeyword(const vector<char>& token) {
	return matchString(token, "score");
}

bool isLetKeyword(const vector<char>& token) {
	return matchString(token, "let");
}

bool isInKeyword(const vector<char>& token) {
	return matchString(token, "in");
}

bool isRowConstant(const vector<char>& token) {
	string s = vector2string(token);
	string values[] = {"1","2","3","4","5","6","7","8"};
	for (int i = 0; i < 8; ++i) {
		if (s == values[i]) return true;
	}
	return false;
}

bool isColConstant(const vector<char>& token) {
	string s = vector2string(token);
	string values[] = {"a","b","c","d","e","f","g","h"};
	for (int i = 0; i < 8; ++i) {
		if (s == values[i]) return true;
	}
	return false;
}

bool isCellConstant(const vector<char>& token) {
	int n = token.size();
	if (n != 2) return false;
	if (token[0] >= 'a' and token[0] <= 'h' and
		token[1] >= '1' and token[1] <= '8') {
		return true;
	}
	return false;
}

bool isPlayerConstant(const vector<char>& token) {
	string s = vector2string(token);
	string values[] = {"me","foe"};
	for (int i = 0; i < 2; ++i) {
		if (s == values[i]) return true;
	}
	return false;
}

bool isTypeConstant(const vector<char>& token) {
	string s = vector2string(token);
	string values[] = {"pawn","knight","bishop","rock","queen","king",
					   "P", "N", "B", "R", "Q", "K"};
	for (int i = 0; i < 6*2; ++i) {
		if (s == values[i]) return true;
	}
	return false;
}

bool isPieceConstant(const vector<char>& token) {
	if (numberAppearances('-',token) != 2) return false;

	vector<char> type(0), cell(0), player(0);
	int i = 0;
	int n = token.size();

	while (i != '-') {
		type.push_back(token[i]);
		++i;
	}
	if (not isTypeConstant(type)) return false;
	++i;
	while (i != '-') {
		cell.push_back(token[i]);
		++i;
	}
	if (not isCellConstant(cell)) return false;
	++i;
	while (i < n) {
		player.push_back(token[i]);
		++i;
	}
	return isPlayerConstant(player);
}

bool isBoolConstant(const vector<char>& token) {
	string s = vector2string(token);
	string values[] = {"true","false"};
	for (int i = 0; i < 2; ++i) {
		if (s == values[i]) return true;
	}
	return false;
}




//auxiliar functions


bool isPrefixPosNum(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return true;
	if (not isDigit(token[0])) return false;
	bool periodFound = false;
	for (int i = 1; i < s; ++i) {
		if (token[i] == '.') {
			if (periodFound) return false;
			else periodFound = true;
		}
		else if (not isDigit(token[i])) return false;
	}
	return true;
}

bool isPrefix(const vector<char>& token, const string& word) {
	int wordSize = word.size();
	int tokenSize = token.size();
	if (tokenSize > wordSize) return false;
	for (int i = 0; i < tokenSize; ++i) {
		if (token[i] != word[i]) return false;
	}
	return true;
}



//prefix patterns

bool isPrefixComment(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return true;
	if (s == 1) return token[0] == '/';
	if (token[0] != '/' or token[1] != '/') return false;

	//there can't be no new lines except in the last position
	for (int i = 2; i < s-1; ++i) {
		if (token[i] == '\n') {
			return false;
		}
	}
	return true;
}

bool isPrefixMultilineComment(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return true;
	if (s == 1) return token[0] == '/';
	if (token[0] != '/' or token[1] != '*') return false;

	//there can't be no '*/' except in the last positions
	for (int i = 2; i < s-2; ++i) {
		if (token[i] == '*' and token[i+1] == '/') {
			return false;
		}
	}
	return true;
}


bool isPrefixNum(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return true;

	//token starts with '-': it is num if what is left removing the '-' is a positive number
	if (token[0] == '-') {
		vector<char> copy(s-1);
		for (int i = 0; i < s-1; ++i) {
			copy[i] = token[i+1];
		}
		return isPrefixPosNum(copy);
	}
	return isPrefixPosNum(token);
}

bool isPrefixId(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return true;
	if (not isLowerCase(token[0])) return false;
	for (int i = 1; i < s; ++i) {
		if (not (isLetter(token[i]) or isDigit(token[i]) or token[i] == '_')) {
			return false;
		}
	}
	return true;	
}


bool isPrefixString(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return true;
	if (s == 1) return token[0] == '"';

	//there can't be '"' except in the last position
	for (int i = 1; i < s-1; ++i) {
		if (token[i] == '"') {
			return false;
		}
	}
	return true;
}


bool isPrefixSpace(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, ' ');
}

bool isPrefixTab(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, '\t');
}

bool isPrefixNewline(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, '\n');
}


bool isPrefixComma(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, ',');
}

bool isPrefixColon(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, ':');
}

bool isPrefixSemicolon(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, ';');
}

bool isPrefixPeriod(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, '.');
}


bool isPrefixOpenParentheses(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, '(');
}

bool isPrefixClosedParentheses(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, ')');
}

bool isPrefixOpenBrackets(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, '[');
}

bool isPrefixClosedBrackets(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, ']');
}


bool isPrefixSumOperator(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token,'+');
}

bool isPrefixRestOperator(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, '-');
}

bool isPrefixProductOperator(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, '*');
}

bool isPrefixDivisionOperator(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, '/');
}


bool isPrefixLTComparison(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, '<');
}

bool isPrefixGTComparison(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, '>');
}

bool isPrefixLEComparison(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return true;
	if (s == 1) return token[0] == '<';
	if (s == 2) return token[0] == '<' and token[1] == '=';
	return false;	
}

bool isPrefixGEComparison(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return true;
	if (s == 1) return token[0] == '>';
	if (s == 2) return token[0] == '>' and token[1] == '=';
	return false;	
}

bool isPrefixEQComparison(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return true;
	if (s == 1) return token[0] == '=';
	if (s == 2) return token[0] == '=' and token[1] == '=';
	return false;	
}

bool isPrefixNEComparison(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return true;
	if (s == 1) return token[0] == '!';
	if (s == 2) return token[0] == '!' and token[1] == '=';
	return false;	
}


bool isPrefixAssignment(const vector<char>& token) {
	if (token.size() == 0) return true;
	return matchChar(token, '=');
}



bool isPrefixModuleKeyword(const vector<char>& token) {
	return isPrefix(token,"module");
}

bool isPrefixModuleName(const vector<char>& token) {
	string values[] = {"seach","evaluation","opening","endgame"};
	for (int i = 0; i < 4; ++i) {
		if (isPrefix(token,values[i])) return true;
	}
	return false;
}


bool isPrefixSymKeyword(const vector<char>& token) {
	return isPrefix(token,"sym");
}

bool isPrefixRuleKeyword(const vector<char>& token) {
	return isPrefix(token,"rule");
}

bool isPrefixPieceKeyword(const vector<char>& token) {
	return isPrefix(token,"piece");
}

bool isPrefixCellKeyword(const vector<char>& token) {
	return isPrefix(token,"cell");
}

bool isPrefixWithKeyword(const vector<char>& token) {
	return isPrefix(token,"with");
}

bool isPrefixIfKeyword(const vector<char>& token) {
	return isPrefix(token,"if");
}

bool isPrefixScoreKeyword(const vector<char>& token) {
	return isPrefix(token,"score");
}

bool isPrefixLetKeyword(const vector<char>& token) {
	return isPrefix(token,"let");
}

bool isPrefixInKeyword(const vector<char>& token) {
	return isPrefix(token,"in");
}


bool isPrefixRowConstant(const vector<char>& token) {
	string values[] = {"1","2","3","4","5","6","7","8"};
	for (int i = 0; i < 8; ++i) {
		if (isPrefix(token,values[i])) return true;
	}
	return false;
}

bool isPrefixColConstant(const vector<char>& token) {
	string values[] = {"a","b","c","d","e","f","g","h"};
	for (int i = 0; i < 8; ++i) {
		if (isPrefix(token,values[i])) return true;
	}
	return false;
}

bool isPrefixCellConstant(const vector<char>& token) {
	int n = token.size();
	if (n == 0) return true;

	if (n >= 1) {
		if (not (token[0] >= 'a' and token[0] <= 'h')) return false;
	}
	if (n == 2) {
		if (not (token[1] >= '1' and token[1] <= '8')) return false;
	}
	return n <= 2;
}

bool isPrefixPlayerConstant(const vector<char>& token) {
	string values[] = {"me","foe"};
	for (int i = 0; i < 2; ++i) {
		if (isPrefix(token,values[i])) return true;
	}
	return false;
}

bool isPrefixTypeConstant(const vector<char>& token) {
	string values[] = {"pawn","knight","bishop","rock","queen","king",
					   "P", "N", "B", "R", "Q", "K"};
	for (int i = 0; i < 6*2; ++i) {
		if (isPrefix(token,values[i])) return true;
	}
	return false;
}

bool isPrefixPieceConstant(const vector<char>& token) {
	int s = token.size();
	if (s == 0) return true;

	int n = numberAppearances('-',token);
	if (n > 2) return false;

	if (n == 0) {
		return isPrefixTypeConstant(token);
	}
	if (n == 1) {
		vector<char> type(0), cellPrefix(0);
		int i = 0;
		while (i != '-') {
			type.push_back(token[i]);
			++i;
		}
		if (not isTypeConstant(type)) return false;
		++i;
		while (i < s) {
			cellPrefix.push_back(token[i]);
			++i;
		}
		if (not isPrefixCellConstant(cellPrefix)) return false;
	}
	if (n == 2) {
		vector<char> type(0), cell(0), playerPrefix(0);
		int i = 0;
		while (i != '-') {
			type.push_back(token[i]);
			++i;
		}
		if (not isTypeConstant(type)) return false;
		++i;
		while (i != '-') {
			cell.push_back(token[i]);
			++i;
		}
		if (not isCellConstant(cell)) return false;
		++i;
		while (i < s) {
			playerPrefix.push_back(token[i]);
			++i;
		}
		return isPrefixPlayerConstant(playerPrefix);
	}

	return false; //execution flux never reaches here, this is to shut off a warning.
}

bool isPrefixBoolConstant(const vector<char>& token) {
	string values[] = {"true","false"};
	for (int i = 0; i < 2; ++i) {
		if (isPrefix(token,values[i])) return true;
	}
	return false;
}


//auxiliar functions


/*
The first function parameter is a 'isPrefix...' function
The second function parameter is a 'is...' function
Returns the largest amount of characters you can "take"
from the char stream (starting at startingPos) such
that the taken chars fulfill the function 'is...'
*/
int tokenMaxLength(const vector<char>& charStream,
				   int startingPos,
				   bool (*prefixToken)(const vector<char>&),
				   bool (*fullToken)(const vector<char>&)) {
	int s = charStream.size();
	int cont = 0;
	vector<char> token(0);
	for (int i = startingPos; i < s; ++i) {
		token.push_back(charStream[i]);
		if ((*prefixToken)(token)) ++cont;
		else {
			token.pop_back();
			if ((*fullToken)(token)) {
				return cont;
			}
			else return 0;
		}
	}
	if ((*fullToken)(token)) {
		return cont;
	}
	else return 0;
}


//max length functions


int commentTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixComment, isComment);
}

int multilineCommentTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixMultilineComment, isMultilineComment);
}


int numTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixNum, isNum);
}


int idTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixId, isId);
}


int stringTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixString, isString);
}


int spaceTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixSpace, isSpace);
}

int tabTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixTab, isTab);
}

int newlineTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixNewline, isNewline);
}


int commaTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixComma, isComma);
}

int colonTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixColon, isColon);
}

int semicolonTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixSemicolon, isSemicolon);
}

int periodTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixPeriod, isPeriod);
}


int openParenthesesTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixOpenParentheses, isOpenParentheses);
}

int closedParenthesesTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixClosedParentheses, isClosedParentheses);
}

int openBracketsTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixOpenBrackets, isOpenBrackets);
}

int closedBracketsTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixClosedBrackets, isClosedBrackets);
}


int sumOperatorTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixSumOperator, isSumOperator);
}

int restOperatorTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixRestOperator, isRestOperator);
}

int productOperatorTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixProductOperator, isProductOperator);
}

int divisionOperatorTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixDivisionOperator, isDivisionOperator);
}


int LTComparisonTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixLTComparison, isLTComparison);
}

int GTComparisonTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixGTComparison, isGTComparison);
}

int LEComparisonTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixLEComparison, isLEComparison);
}

int GEComparisonTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixGEComparison, isGEComparison);
}

int EQComparisonTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixEQComparison, isEQComparison);
}

int NEComparisonTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixNEComparison, isNEComparison);
}


int assignmentTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixAssignment, isAssignment);
}


int moduleKeywordTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixModuleKeyword, isModuleKeyword);
}

int moduleNameTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixModuleName, isModuleName);
}


int symKeywordTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixSymKeyword, isSymKeyword);
}

int ruleKeywordTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixRuleKeyword, isRuleKeyword);
}

int pieceKeywordTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixPieceKeyword, isPieceKeyword);
}

int cellKeywordTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixCellKeyword, isCellKeyword);
}

int withKeywordTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixWithKeyword, isWithKeyword);
}

int ifKeywordTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixIfKeyword, isIfKeyword);
}

int scoreKeywordTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixScoreKeyword, isScoreKeyword);
}

int letKeywordTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixLetKeyword, isLetKeyword);
}

int inKeywordTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixInKeyword, isInKeyword);
}


int rowConstantTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixRowConstant, isRowConstant);
}

int colConstantTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixColConstant, isColConstant);
}

int cellConstantTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixCellConstant, isCellConstant);
}

int playerConstantTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixPlayerConstant, isPlayerConstant);
}

int typeConstantTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixTypeConstant, isTypeConstant);
}

int pieceConstantTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixPieceConstant, isPieceConstant);
}

int boolConstantTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixBoolConstant, isBoolConstant);
}



pair<string,int> longestTokenType(const vector<char>& charStream, int startingPos) {
	vector<pair<string,int> > v(0);
	v.push_back(make_pair("comment", commentTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("multilineComment", multilineCommentTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("num", numTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("id", idTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("string", stringTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("space", spaceTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("tab", tabTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("newline", newlineTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("comma", commaTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("colon", colonTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("semicolon", semicolonTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("period", periodTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("openParentheses", openParenthesesTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("closedParentheses", closedParenthesesTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("openBrackets", openBracketsTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("closedBrackets", closedBracketsTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("sumOperator", sumOperatorTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("restOperator", restOperatorTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("productOperator", productOperatorTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("divisionOperator", divisionOperatorTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("LTComparison", LTComparisonTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("GTComparison", GTComparisonTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("LEComparison", LEComparisonTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("GEComparison", GEComparisonTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("EQComparison", EQComparisonTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("NEComparison", NEComparisonTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("assignment", assignmentTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("moduleKeyword", moduleKeywordTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("moduleName", moduleNameTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("symKeyword", symKeywordTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("ruleKeyword", ruleKeywordTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("pieceKeyword", pieceKeywordTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("cellKeyword", cellKeywordTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("withKeyword", withKeywordTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("ifKeyword", ifKeywordTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("scoreKeyword", scoreKeywordTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("letKeyword", letKeywordTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("inKeyword", inKeywordTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("rowConstant", rowConstantTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("colConstant", colConstantTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("cellConstant", cellConstantTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("playerConstant", playerConstantTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("typeConstant", typeConstantTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("pieceConstant", pieceConstantTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("boolConstant", boolConstantTokenMaxLength(charStream, startingPos)));

	int max = 0;
	pair<string,int> result;
	int s = v.size();

	for (int i = 0; i < s; ++i) {
		if (v[i].second > max) {
			max = v[i].second;
			result.first = v[i].first;
			result.second = v[i].second;
		}
	}

	if (max == 0) {
		result = make_pair("couldn't detect any token", -1);
	}
	return result;
}




vector<Token> parse(const vector<char>& charStream) {
	vector<Token> v(0);
	int n = charStream.size();
	int index = 0;

	while (index < n) {
		pair<string,int> nextToken = longestTokenType(charStream, index);
		string content = "";
		for (int i = index; i < index + nextToken.second; ++i) {
			content.push_back(charStream[i]);
		}
		v.push_back(Token(nextToken.first, content));
		index = index + nextToken.second;
	}

	return v;
}

