#include "tokenPatterns.h"
#include "utils.h"
#include <vector>
#include <string>

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
	if (s < 1) return false;

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
	if (s < 1) return false;

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
	if (s < 1) return false;
	if (not isLowerCase(token[0])) return false;
	for (int i = 1; i < s; ++i) {
		if (not (isLetter(token[i]) or isDigit(token[i]))) {
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
	if (s == "seach" or s == "evaluation" or s == "opening" or s == "endgame") {
		return true;
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


