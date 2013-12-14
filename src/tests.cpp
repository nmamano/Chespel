#include "tests.h"
#include "tokenPatterns.h"
#include <iostream>
#include <vector>
#include <string>
#include <cassert>

using namespace std;


//auxiliar methods

//returns a vector with all printable characters and
//some others
vector<char> allChars() {
	vector<char> v(0);
	for (int i = 33; i <= 126; ++i) {
		v.push_back((char) i);
	}
	v.push_back('\n');
	v.push_back('\t');
	v.push_back('\r');
	v.push_back('\f');
	v.push_back('\b');
	return v;
}

//checks for inconsistencies in token patterns
void check(const vector<char>& token) {
	string s = vector2string(token);
	assert(s == " " or not isSpace(token));
	assert(s == "\t" or not isTab(token));
	assert(s == "\n" or not isNewline(token));
	assert(s == "," or not isComma(token));
	assert(s == ":" or not isColon(token));
	assert(s == ";" or not isSemicolon(token));
	assert(s == "." or not isPeriod(token));
	assert(s == "(" or not isOpenParentheses(token));
	assert(s == ")" or not isClosedParentheses(token));
	assert(s == "[" or not isOpenBrackets(token));
	assert(s == "]" or not isClosedBrackets(token));
	assert(s == "+" or not isSumOperator(token));
	assert(s == "-" or not isRestOperator(token));
	assert(s == "*" or not isProductOperator(token));
	assert(s == "/" or not isDivisionOperator(token));
	assert(s == "<" or not isLTComparison(token));
	assert(s == ">" or not isGTComparison(token));
	assert(s == "<=" or not isLEComparison(token));
	assert(s == ">=" or not isGEComparison(token));
	assert(s == "==" or not isEQComparison(token));
	assert(s == "!=" or not isNEComparison(token));
	assert(s == "=" or not isAssignment(token));
	assert(s == "module" or not isModuleKeyword(token));
	assert(s == "search" or s == "evaluation" or s == "opening" or s == "endgame" or not isModuleName(token));
	assert(s == "sym" or not isSymKeyword(token));
	assert(s == "rule" or not isRuleKeyword(token));
	assert(s == "piece" or not isPieceKeyword(token));
	assert(s == "cell" or not isCellKeyword(token));
	assert(s == "with" or not isWithKeyword(token));
	assert(s == "if" or not isIfKeyword(token));
	assert(s == "score" or not isScoreKeyword(token));
	assert(s == "let" or not isLetKeyword(token));
	assert(s == "in" or not isInKeyword(token));
}


//tests

void test_read_input(const vector<char>& input) {
	for (unsigned int i = 0; i < input.size(); ++i) {
		cout << input[i];
	}
	cout << endl;
}



void test_token_patterns() {
	vector<char> chars = allChars();
	int n = chars.size();
	vector<char> token;
	
	//tokens of length 1
	token = vector<char> (1);
	for (int i = 0; i < n; ++i) {
		token[0] = chars[i];
		check(token);
	}

	//tokens of length 2
	token = vector<char> (2);
	for (int i = 0; i < n; ++i) {
		for (int j = 0; j < n; ++j) {
			token[0] = chars[i];
			token[1] = chars[j];
			check(token);			
		}
	}

	//tokens of length 3
	token = vector<char> (3);
	for (int i = 0; i < n; ++i) {
		for (int j = 0; j < n; ++j) {
			for (int k = 0; k < n; ++k) {
				token[0] = chars[i];
				token[1] = chars[j];
				token[2] = chars[k];
				check(token);
			}			
		}
	}

}