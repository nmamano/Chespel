#include "tests.h"
#include "utils.h"
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
	for (int i = 32; i <= 126; ++i) {
		v.push_back((char) i);
	}
	v.push_back('\n');
	v.push_back('\t');
	v.push_back('\r');
	v.push_back('\f');
	v.push_back('\b');
	return v;
}

vector<char> someChars() {
	vector<char> v(0);
	for (int i = 32; i <= 47; ++i) {
		v.push_back((char) i);
	}
	for (int i = 58; i <= 64; ++i) {
		v.push_back((char) i);
	}
	for (int i = 91; i <= 126; ++i) {
		v.push_back((char) i);
	}
	v.push_back('\n');
	v.push_back('\t');
	return v;	
}

vector<char> allLowerCase() {
	vector<char> v(0);
	for (int i = 97; i <= 122; ++i) {
		v.push_back((char) i);
	}
	v.push_back('\n');
	v.push_back('\t');
	v.push_back(' ');
	return v;	
}

//checks for inconsistencies in token patterns
void check(const vector<char>& token) {
	string s = vector2string(token);
	assert(isSpace(token) ? s == " " : s != " ");
	assert(isTab(token) ? s == "\t" : s != "\t");
	assert(isNewline(token) ? s == "\n" : s != "\n");
	assert(isComma(token) ? s == "," : s != ",");
	assert(isColon(token) ? s == ":" : s != ":");
	assert(isSemicolon(token) ? s == ";" : s != ";");
	assert(isPeriod(token) ? s == "." : s != ".");
	assert(isOpenParentheses(token) ? s == "(" : s != "(");
	assert(isClosedParentheses(token) ? s == ")" : s != ")");
	assert(isOpenBrackets(token) ? s == "[" : s != "[");
	assert(isClosedBrackets(token) ? s == "]" : s != "]");
	assert(isSumOperator(token) ? s == "+" : s != "+");
	assert(isRestOperator(token) ? s == "-" : s != "-");
	assert(isProductOperator(token) ? s == "*" : s != "*");
	assert(isDivisionOperator(token) ? s == "/" : s != "/");
	assert(isLTComparison(token) ? s == "<" : s != "<");
	assert(isGTComparison(token) ? s == ">" : s != ">");
	assert(isLEComparison(token) ? s == "<=" : s != "<=");
	assert(isGEComparison(token) ? s == ">=" : s != ">=");
	assert(isEQComparison(token) ? s == "==" : s != "==");
	assert(isNEComparison(token) ? s == "!=" : s != "!=");
	assert(isAssignment(token) ? s == "=" : s != "=");
	assert(isModuleKeyword(token) ? s == "module" : s != "module");
	assert((s == "evaluation" or s == "opening" or s == "endgame" or s == "endgame") ? isModuleName(token) : not isModuleName(token));
	assert(isSymKeyword(token) ? s == "sym" : s != "sym");
	assert(isRuleKeyword(token) ? s == "rule" : s != "rule");
	assert(isPieceKeyword(token) ? s == "piece" : s != "piece");
	assert(isCellKeyword(token) ? s == "cell" : s != "cell");
	assert(isWithKeyword(token) ? s == "with" : s != "with");
	assert(isIfKeyword(token) ? s == "if" : s != "if");
	assert(isScoreKeyword(token) ? s == "score" : s != "score");
	assert(isLetKeyword(token) ? s == "let" : s != "let");
	assert(isInKeyword(token) ? s == "in" : s != "in");
}

/*
checks that for all possible words up to length 2,
and a lot of words up to length 3,
the matched patterns are correct
*/
void bruteForceChecks() {
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
	chars = someChars();
	n = chars.size();
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

void checkNum(const string& s) {
	vector<char> token = string2vector(s);
	assert(isNum(token));
	
	//check negated version too
	int n = token.size();
	vector<char> negatedNum = vector<char> (n+1);
	negatedNum[0] = '-';
	for (int i = 0; i < n; ++i) {
		negatedNum[i+1] = token[i];
	}
	assert(isNum(negatedNum));
}

void checkNotNum(const string& s) {
	vector<char> token = string2vector(s);
	assert(not isNum(token));
}

/*
checks that some representative num patterns are matched correctly
*/
void numPatternChecks() {
	checkNum("0");
	checkNum("0.1");
	checkNum("1.0");
	checkNum("12340");
	checkNum("53460.8654");
	checkNum("123456789");
	checkNum("0003");
	checkNum("00000");
	checkNum("0.000004");
	checkNum("0.0");
	checkNum("20.000");
	checkNum("0000.000");

	checkNotNum(".");
	checkNotNum("0.");
	checkNotNum(".0");
	checkNotNum("153.");
	checkNotNum(".733");
	checkNotNum("342.214.457");
	checkNotNum("343..687");
	checkNotNum("353,456");
	checkNotNum("");
	checkNotNum("324 234");
	checkNotNum("543 ");
	checkNotNum(" 547");
	checkNotNum("--65");
	checkNotNum("-.0");
	checkNotNum("-0.");
	checkNotNum("-0.-0");
	checkNotNum("hi");
	checkNotNum("4O6");
	checkNotNum("4+7");
	checkNotNum("3-2");
	checkNotNum(" ");
	checkNotNum("3e-6");
}

void checkId(const string& s) {
	vector<char> token = string2vector(s);
	assert(isId(token));
}

void checkNotId(const string& s) {
	vector<char> token = string2vector(s);
	assert(not isId(token));
}

/*
checks that some representative id patterns are matched correctly
*/
void idPatternChecks() {
	checkId("hi");
	checkId("hai");
	checkId("x");
	checkId("sUp");
	checkId("aBC");
	checkId("a945k32");
	checkId("z34ks");
	checkId("mmm");
	checkId("knight");
	checkId("where");
	checkId("rule");
	checkId("module");
	checkNotId("he_llo");
	checkNotId("_hello");
	checkNotId("hello_");
	checkNotId("ha'i");
	checkNotId("ye-s");
	checkNotId("My");
	checkNotId("9res");
	checkNotId("Azar");
	checkNotId("Zara");
	checkNotId("0ste");
	checkNotId("a.b");
	checkNotId(" ");
	checkNotId("rst ");
	checkNotId(" rst");
	checkNotId("rst rst");
}


void checkComment(const string& s) {
	vector<char> token = string2vector(s);
	assert(isComment(token));
}

void checkNotComment(const string& s) {
	vector<char> token = string2vector(s);
	assert(not isComment(token));
}

void checkMultilineComment(const string& s) {
	vector<char> token = string2vector(s);
	assert(isMultilineComment(token));
}

void checkNotMultilineComment(const string& s) {
	vector<char> token = string2vector(s);
	assert(not isMultilineComment(token));
}

void commentPatternChecks() {
	checkComment("//dasrta\n");
	checkComment("// hsrt ast   \n");
	checkComment("//0 02 0 rs\n");
	checkComment("//////\n");
	checkComment("///\n");
	checkComment("//\n");
	checkNotComment("//  ");
	checkNotComment("//");
	checkNotComment("/ ensrte\n");
	checkNotComment("/ /resnt\n");
	checkNotComment("hi");
	checkNotComment("//dare\n\n");
	checkNotComment(" ");
	checkNotComment(" //eoes\n");
	checkNotComment("//eoes\n ");
	checkNotComment(" //eoes\n ");
	checkNotComment("//eoes\n\t");
	checkMultilineComment("/* resn */");
	checkMultilineComment("/* ;:.,_ -- // //re */");
	checkMultilineComment("/* \n \n \n \t \t \t */");
	checkMultilineComment("/**/");
	checkMultilineComment("/***/");
	checkMultilineComment("/*/* */");
	checkMultilineComment("/*//*/");
	checkNotMultilineComment("/*/");
	checkNotMultilineComment("/* res /");
	checkNotMultilineComment("arsd */");
	checkNotMultilineComment("/* redr */ resd */");
	checkNotMultilineComment(" /**/");
	checkNotMultilineComment("/**/ ");
	checkNotMultilineComment(" /**/ ");
	checkNotMultilineComment("//  ard */");
	checkNotMultilineComment("");
	checkNotMultilineComment(" ");
	checkNotMultilineComment("/* lul * /");
	checkNotMultilineComment("/ * arsedr */");

}




//tests

void test_read_input(const vector<char>& input) {
	printVector(input);
	cout << endl;
}



void test_token_patterns() {

	bruteForceChecks();
	numPatternChecks();
	idPatternChecks();
	commentPatternChecks();

}