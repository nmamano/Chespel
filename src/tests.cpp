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
	assert(isRestOperator(token) ? s == "-" : s != "-");
	assert(isDivisionOperator(token) ? s == "/" : s != "/");
	assert(isModuleKeyword(token) ? s == "module" : s != "module");
	assert(isSymKeyword(token) ? s == "sym" : s != "sym");
	assert(isRuleKeyword(token) ? s == "rule" : s != "rule");
	assert(isPieceKeyword(token) ? s == "piece" : s != "piece");
	assert(isCellKeyword(token) ? s == "cell" : s != "cell");
	assert(isWithKeyword(token) ? s == "with" : s != "with");
	assert(isIfKeyword(token) ? s == "if" : s != "if");
	assert(isScoreKeyword(token) ? s == "score" : s != "score");
	assert(isLetKeyword(token) ? s == "let" : s != "let");
	assert(isInKeyword(token) ? s == "in" : s != "in");
	
	assert((s == "evaluation" or s == "opening" or s == "endgame" or s == "endgame") ? isModuleName(token) : not isModuleName(token));
	assert((s == "1" or s == "2" or s == "3" or s == "4" or
			s == "5" or s == "6" or s == "7" or s == "8") ? isRowConstant(token) : not isRowConstant(token));	
	assert((s == "a" or s == "b" or s == "c" or s == "d" or
			s == "e" or s == "f" or s == "g" or s == "h") ? isColConstant(token) : not isColConstant(token));	
	assert((s == "me" or s == "foe") ? isPlayerConstant(token) : not isPlayerConstant(token));	
	assert((s == "pawn" or s == "knight" or s == "bishop" or s == "rock" or s == "queen" or s == "king" or
			s == "P" or s == "N" or s == "B" or s == "R" or s == "Q" or s == "K") ? isTypeConstant(token) : not isTypeConstant(token));	
	assert((s == "true" or s == "false") ? isBoolConstant(token) : not isBoolConstant(token));		
	
	assert((s == "a1" or s == "a2" or s == "a3" or s == "a4" or s == "a5" or s == "a6" or s == "a7" or s == "a8" or
			s == "b1" or s == "b2" or s == "b3" or s == "b4" or s == "b5" or s == "b6" or s == "b7" or s == "b8" or
			s == "c1" or s == "c2" or s == "c3" or s == "c4" or s == "c5" or s == "c6" or s == "c7" or s == "c8" or
			s == "d1" or s == "d2" or s == "d3" or s == "d4" or s == "d5" or s == "d6" or s == "d7" or s == "d8" or
			s == "e1" or s == "e2" or s == "e3" or s == "e4" or s == "e5" or s == "e6" or s == "e7" or s == "e8" or
			s == "f1" or s == "f2" or s == "f3" or s == "f4" or s == "f5" or s == "f6" or s == "f7" or s == "f8" or
			s == "g1" or s == "g2" or s == "g3" or s == "g4" or s == "g5" or s == "g6" or s == "g7" or s == "g8" or
			s == "h1" or s == "h2" or s == "h3" or s == "h4" or s == "h5" or s == "h6" or s == "h7" or s == "h8") ? 
			isCellConstant(token) : not isCellConstant(token));	
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
		//check(token);
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
	checkNotNum("-");
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
	checkId("he_llo");
	checkId("hello_");
	checkNotId("_hello");
	checkNotId("ha'i");
	checkNotId("ye-s");
	checkNotId("My");
	checkNotId("9res");
	checkNotId("");
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
	checkNotComment("");
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
	checkMultilineComment("/*/*/");
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

void checkString(const string& s) {
	vector<char> token = string2vector(s);
	assert(isString(token));
}

void checkNotString(const string& s) {
	vector<char> token = string2vector(s);
	assert(not isString(token));
}

void stringPatternChecks() {
	checkString("\"bla bla bla\"");
	checkString("\"\"");
	checkString("\"      \"");
	checkString("\"\n\n\t\"");
	checkString("\"''\"");
	checkString("\"/* */\"");
	checkString("\".,/_-[]();:\"");
	checkNotString(" \"\"");
	checkNotString("\"\" ");
	checkNotString(" \" \" ");
	checkNotString("\"\"\"");
	checkNotString("\"");
	checkNotString("\"''");
	checkNotString("\n\"\"");
	checkNotString("");
	checkNotString(" ");
	checkNotString("\"\"\"\"");
}

//receives a valid num token and checks that
//all of its prefixes are recognized as such
void checkPrefixesOfNum(const string& s) {
	vector<char> token = string2vector(s);
	assert(isNum(token)); //just checking that s really is a valid num token

	vector<vector<char> > prefixes = getPrefixes(token);
	int n = prefixes.size();
	for (int i = 0; i < n; ++i) {
		assert(isPrefixNum(prefixes[i]));
	}

	//check negated version of num too
	n = token.size();
	vector<char> negatedNum = vector<char> (n+1);
	negatedNum[0] = '-';
	for (int i = 0; i < n; ++i) {
		negatedNum[i+1] = token[i];
	}
	assert(isNum(negatedNum)); //just checking that negatedNum really is a valid num token

	prefixes = getPrefixes(negatedNum);
	n = prefixes.size();
	for (int i = 0; i < n; ++i) {
		assert(isPrefixNum(prefixes[i]));
	}
}

void checkNotPrefixNum(const string& s) {
	vector<char> token = string2vector(s);
	assert(not isPrefixNum(token));	
}

void numPrefixPatternChecks() {
	checkPrefixesOfNum("0");
	checkPrefixesOfNum("0.1");
	checkPrefixesOfNum("1.0");
	checkPrefixesOfNum("12340");
	checkPrefixesOfNum("53460.8654");
	checkPrefixesOfNum("123456789");
	checkPrefixesOfNum("0003");
	checkPrefixesOfNum("00000");
	checkPrefixesOfNum("0.000004");
	checkPrefixesOfNum("0.0");
	checkPrefixesOfNum("20.000");
	checkPrefixesOfNum("0000.000");

	checkNotPrefixNum(".");
	checkNotPrefixNum(".0");
	checkNotPrefixNum(".733");
	checkNotPrefixNum("342.214.457");
	checkNotPrefixNum("343..687");
	checkNotPrefixNum("353,456");
	checkNotPrefixNum("324 234");
	checkNotPrefixNum("543 ");
	checkNotPrefixNum(" 547");
	checkNotPrefixNum("--65");
	checkNotPrefixNum("-.0");
	checkNotPrefixNum("-0.-0");
	checkNotPrefixNum("hi");
	checkNotPrefixNum("4O6");
	checkNotPrefixNum("4+7");
	checkNotPrefixNum("3-2");
	checkNotPrefixNum(" ");
	checkNotPrefixNum("3e-6");
}

//receives a valid id token and checks that
//all of its prefixes are recognized as such
void checkPrefixesOfId(const string& s) {
	vector<char> token = string2vector(s);
	assert(isId(token)); //just checking that s is really a valid id token
	vector<vector<char> > prefixes = getPrefixes(token);
	int n = prefixes.size();
	for (int i = 0; i < n; ++i) {
		assert(isPrefixId(prefixes[i]));
	}
}

void checkNotPrefixId(const string& s) {
	vector<char> token = string2vector(s);
	assert(not isPrefixId(token));	
}

void idPrefixPatternChecks() {
	checkPrefixesOfId("hi");
	checkPrefixesOfId("hai");
	checkPrefixesOfId("x");
	checkPrefixesOfId("sUp");
	checkPrefixesOfId("aBC");
	checkPrefixesOfId("a945k32");
	checkPrefixesOfId("z34ks");
	checkPrefixesOfId("mmm");
	checkPrefixesOfId("knight");
	checkPrefixesOfId("where");
	checkPrefixesOfId("rule");
	checkPrefixesOfId("module");
	checkPrefixesOfId("he_llo");
	checkPrefixesOfId("hello_");
	checkNotPrefixId("_hello");
	checkNotPrefixId("ha'i");
	checkNotPrefixId("ye-s");
	checkNotPrefixId("My");
	checkNotPrefixId("9res");
	checkNotPrefixId("Azar");
	checkNotPrefixId("Zara");
	checkNotPrefixId("0ste");
	checkNotPrefixId("a.b");
	checkNotPrefixId(" ");
	checkNotPrefixId("rst ");
	checkNotPrefixId(" rst");
	checkNotPrefixId("rst rst");
}

//receives a valid comment token and checks that
//all of its prefixes are recognized as such
void checkPrefixesOfComment(const string& s) {
	vector<char> token = string2vector(s);
	assert(isComment(token)); //just checking that s is really a valid comment token
	vector<vector<char> > prefixes = getPrefixes(token);
	int n = prefixes.size();
	for (int i = 0; i < n; ++i) {
		assert(isPrefixComment(prefixes[i]));
	}
}

void checkNotPrefixComment(const string& s) {
	vector<char> token = string2vector(s);
	assert(not isPrefixComment(token));	
}

//receives a valid multiline comment token and checks that
//all of its prefixes are recognized as such
void checkPrefixesOfMultilineComment(const string& s) {
	vector<char> token = string2vector(s);
	assert(isMultilineComment(token)); //just checking that s is really a valid multiline comment token
	vector<vector<char> > prefixes = getPrefixes(token);
	int n = prefixes.size();
	for (int i = 0; i < n; ++i) {
		assert(isPrefixMultilineComment(prefixes[i]));
	}
}

void checkNotPrefixMultilineComment(const string& s) {
	vector<char> token = string2vector(s);
	assert(not isPrefixMultilineComment(token));	
}

void commentPrefixPatternChecks() {
	checkPrefixesOfComment("//dasrta\n");
	checkPrefixesOfComment("// hsrt ast   \n");
	checkPrefixesOfComment("//0 02 0 rs\n");
	checkPrefixesOfComment("//////\n");
	checkPrefixesOfComment("///\n");
	checkPrefixesOfComment("//\n");
	checkNotPrefixComment("/ ensrte\n");
	checkNotPrefixComment("/ /resnt\n");
	checkNotPrefixComment("hi");
	checkNotPrefixComment("//dare\n\n");
	checkNotPrefixComment(" ");
	checkNotPrefixComment(" //eoes\n");
	checkNotPrefixComment("//eoes\n ");
	checkNotPrefixComment(" //eoes\n ");
	checkNotPrefixComment("//eoes\n\t");
	checkPrefixesOfMultilineComment("/* resn */");
	checkPrefixesOfMultilineComment("/* ;:.,_ -- // //re */");
	checkPrefixesOfMultilineComment("/* \n \n \n \t \t \t */");
	checkPrefixesOfMultilineComment("/**/");
	checkPrefixesOfMultilineComment("/***/");
	checkPrefixesOfMultilineComment("/*/* */");
	checkPrefixesOfMultilineComment("/*//*/");
	checkPrefixesOfMultilineComment("/*/*/");
	checkNotPrefixMultilineComment("arsd */");
	checkNotPrefixMultilineComment("/* redr */ resd */");
	checkNotPrefixMultilineComment(" /**/");
	checkNotPrefixMultilineComment("/**/ ");
	checkNotPrefixMultilineComment(" /**/ ");
	checkNotPrefixMultilineComment("//  ard */");
	checkNotPrefixMultilineComment(" ");
	checkNotPrefixMultilineComment("/ * arsedr */");
}

//receives a valid string token and checks that
//all of its prefixes are recognized as such
void checkPrefixesOfString(const string& s) {
	vector<char> token = string2vector(s);
	assert(isString(token)); //just checking that s is really a valid string token
	vector<vector<char> > prefixes = getPrefixes(token);
	int n = prefixes.size();
	for (int i = 0; i < n; ++i) {
		assert(isPrefixString(prefixes[i]));
	}
}

void checkNotPrefixString(const string& s) {
	vector<char> token = string2vector(s);
	assert(not isPrefixString(token));	
}
void stringPrefixPatternChecks() {
	checkPrefixesOfString("\"bla bla bla\"");
	checkPrefixesOfString("\"\"");
	checkPrefixesOfString("\"      \"");
	checkPrefixesOfString("\"\n\n\t\"");
	checkPrefixesOfString("\"''\"");
	checkPrefixesOfString("\"/* */\"");
	checkPrefixesOfString("\".,/_-[]();:\"");
	checkNotPrefixString(" \"\"");
	checkNotPrefixString("\"\" ");
	checkNotPrefixString(" \" \" ");
	checkNotPrefixString("\"\"\"");
	checkNotPrefixString("\n\"\"");
	checkNotPrefixString(" ");
	checkNotPrefixString("\"\"\"\"");
}




//tests

void test_token_patterns() {
	bruteForceChecks();
	numPatternChecks();
	idPatternChecks();
	commentPatternChecks();
	stringPatternChecks();
	numPrefixPatternChecks();
	idPrefixPatternChecks();
	commentPrefixPatternChecks();
	stringPrefixPatternChecks();
}


void test_token_max_lengths() {
	string example = "\n//com here ---  // rsenoatie\n// reonodiae\n     /// risenoa\n          //\nmodule/* */ evaluation // arodeinsi\n/*\n\"     \"\n// arisnedars\n*/\nrule check:\n// yo\n	if (me.check): //ars\n		score -5, \" /*   */ \" ++ \" // /// // \";\n		\n//comment - hello ?\n	";
	vector<char> charStream = string2vector(example);
	int s = example.size();
	for (int i = 0; i < s; ++i) {
		cout << charStream[i] << " " << longestTokenType(charStream, i).first << endl;
	}
}


void test_lexical_parsing() {
	string example = "\n//com here ---  // rsenoatie\n// reonodiae\n     /// risenoa\n          //\nmodule/* */ evaluation // arodeinsi\n/*\n\"     \"\n// arisnedars\n*/\nrule check:\n// yo\n	if (me.check): //ars\n		score -5, \" /*   */ \" ++ \" // /// // \";\n		\n//comment - hello ?\n	";
	cout << "Initial text:" << endl << example << endl;
	vector<char> charStream = string2vector(example);
	vector<Token> v = parse(charStream);
	int s = v.size();
	cout << "Parsed text:" << endl;
	for (int i = 0; i < s; ++i) {
		cout << v[i].type << "\t->\t|";
		if (v[i].content == "\n") cout << "\\" << "n";
		else if (v[i].content == "\t") cout << "\\" << "t";
		//for comments, print the ending endline as \n
		else if (v[i].type == "comment") {
			for (unsigned int j = 0; j < v[i].content.size()-1; ++j) {
				cout << v[i].content[j];
			}
			cout << "\\" << "n";
		}
		else cout << v[i].content;
		cout << "|" << endl;
	}
	cout << endl;
}