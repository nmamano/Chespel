#include "tokenPatterns.h"
#include "utils.h"
#include <vector>
#include <string>
#include <utility>
#include <iostream>
#include <cstdlib>

using namespace std;

//auxiliar methods


vector<char> removeComments(const vector<char>& input) {
	int pos = 0;
	bool insideString = false;
	bool insideCom = false;
	bool insideMultiCom = false;
	vector<char> withoutComs(0);
	int s = input.size();

	while (pos < s) {
		if (insideString and input[pos] == '"') {
			withoutComs.push_back(input[pos]);
			insideString = false;
		}
		else if (insideString) {
			withoutComs.push_back(input[pos]);
		}
		else if (insideCom and input[pos] == '\n') {
			withoutComs.push_back(input[pos]);
			insideCom = false;
		}
		else if (insideCom) {
			//do nothing
		}
		else if (insideMultiCom and input[pos-1] == '*' and input[pos] == '/') {
			insideMultiCom = false;
		}
		else if (insideMultiCom) {
			//do nothing
		} 
		else if (input[pos] == '"') {
			insideString = true;
			withoutComs.push_back(input[pos]);
		}
		else if (pos < s-1 and input[pos] == '/' and input[pos+1] == '/') {
			insideCom = true;
		}
		else if (pos < s-1 and input[pos] == '/' and input[pos+1] == '*') {
			insideMultiCom = true;
		}
		else {
			withoutComs.push_back(input[pos]);
		}
		++pos;
	}
	if (insideCom or insideMultiCom) {
		cerr << "Lexic error: comments' opening and closing marks do not match" << endl;
		exit(0);
	}
	return withoutComs;
}

void check_matching_quotations(const vector<char>& input) {
	if (numberAppearances('"',input)%2 != 0) {
		cerr << "Lexic error: string quotation marks do not match" << endl;
		exit(0);
	}
}

void check_no_wrongTokens(const vector<Token>& v) {
	int s = v.size();
	bool wrongTokenFound = false;
	for (int i = 0; i < s; ++i) {
		if (v[i].type == "wrongToken") {
			wrongTokenFound = true;
			cerr << "Lexic error: wrong token (" << i << "): " << v[i].content << endl;
		}
	}
	if (wrongTokenFound) exit(0);
}


bool matchString(const vector<char>& token, string s) {
	string aux = vector2string(token);
	return s == aux;
}

bool isDigit(char c) {
	return c >= '0' and c <= '9';
}

bool isLowerCase(char c) {
	return c >= 'a' and c <= 'z';
}

bool isUpperCase(char c) {
	return c >= 'A' and c <= 'Z';
}

bool isLetter(char c) {
	return isLowerCase(c) or isUpperCase(c);
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

bool isSubOperator(const vector<char>& token) {
	if (token.size() == 1 and token[0] == '-') {
		return true;
	}
	return false;
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
	if (s == "me") return true;
	return s == "foe";
}

bool isTypeConstant(const vector<char>& token) {
	string s = vector2string(token);
	string values[] = {"pawn","knight","bishop","rock","queen","king"};
	for (int i = 0; i < 6; ++i) {
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
	if (s == "true") return true;
	return s == "false";
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
	return isId(token); //Every non-empty prefix of an id is an id
}

bool isPrefixSubOperator(const vector<char>& token) {
	if (token.size() == 0) return true;
	if (token.size() == 1 and token[0] == '-') {
		return true;
	}
	return false;
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
	if (isPrefix(token,"me")) return true;
	return isPrefix(token,"foe");
}

bool isPrefixTypeConstant(const vector<char>& token) {
	string values[] = {"pawn","knight","bishop","rock","queen","king"};
	for (int i = 0; i < 6; ++i) {
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
	if (isPrefix(token,"true")) return true;
	return isPrefix(token,"false");
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

int numTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixNum, isNum);
}


int idTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixId, isId);
}

int subOperatorTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixSubOperator, isSubOperator);
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
	//pushed in order of priority
	v.push_back(make_pair("num", numTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("subOperator", subOperatorTokenMaxLength(charStream, startingPos)));
	
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
	v.push_back(make_pair("cellConstant", cellConstantTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("playerConstant", playerConstantTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("typeConstant", typeConstantTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("pieceConstant", pieceConstantTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("boolConstant", boolConstantTokenMaxLength(charStream, startingPos)));

	//finally, generic ids (for example, variable names)
	v.push_back(make_pair("id", idTokenMaxLength(charStream, startingPos)));

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




vector<Token> lexical_parse(const vector<char>& stream) {
	vector<char> charStream = removeComments(stream);
	check_matching_quotations(charStream);

	vector<Token> v(0);
	int n = charStream.size();
	int index = 0;
	while (index < n) {
		if (charStream[index] == ',') {
			v.push_back(Token("comma",","));
		}
		else if (charStream[index] == '.') {
			v.push_back(Token("period","."));			
		}
		else if (charStream[index] == ';') {
			v.push_back(Token("semicolon",";"));			
		}
		else if (charStream[index] == ':') {
			v.push_back(Token("colon",":"));			
		}
		else if (charStream[index] == '(') {
			v.push_back(Token("openParentheses","("));			
		}
		else if (charStream[index] == ')') {
			v.push_back(Token("closedParentheses",")"));			
		}
		else if (charStream[index] == '[') {
			v.push_back(Token("openBrackets","["));			
		}
		else if (charStream[index] == ']') {
			v.push_back(Token("closedBrackets","]"));			
		}
		else if (charStream[index] == ' ') {
			v.push_back(Token("space"," "));			
		}
		else if (charStream[index] == '\t') {
			v.push_back(Token("tab","\t"));			
		}
		else if (charStream[index] == '\n') {
			v.push_back(Token("newline","\n"));			
		}
		else if (charStream[index] == '*') {
			v.push_back(Token("productOperator","*"));			
		}
		else if (charStream[index] == '/') {
			v.push_back(Token("divisionOperator","/"));			
		}
		else if (charStream[index] == '+') {
			if (index == n-1 or charStream[index+1] != '+') {
				v.push_back(Token("sumOperator","+"));
			}
			else if ( charStream[index+1] == '+') {
				v.push_back(Token("concatOperator","++"));
				++index;
			}
		}
		else if (charStream[index] == '<') {
			if (index == n-1 or charStream[index+1] != '=') {
				v.push_back(Token("LTComparison","<"));
			}
			else if ( charStream[index+1] == '=') {
				v.push_back(Token("LEComparison","<="));
				++index;
			}
		}
		else if (charStream[index] == '>') {
			if (index == n-1 or charStream[index+1] != '=') {
				v.push_back(Token("GTComparison",">"));
			}
			else if ( charStream[index+1] == '=') {
				v.push_back(Token("GEComparison",">="));
				++index;
			}
		}
		else if (charStream[index] == '=') {
			if (index == n-1 or charStream[index+1] != '=') {
				v.push_back(Token("assignment","="));
			}
			else if ( charStream[index+1] == '=') {
				v.push_back(Token("EQComparison","=="));
				++index;
			}
		}
		else if (charStream[index] == '!') {
			if (index == n-1 or charStream[index+1] != '=') {
				v.push_back(Token("wrongToken","!"));
			}
			else if ( charStream[index+1] == '=') {
				v.push_back(Token("NEComparison","!="));
				++index;
			}
		}
		else if (charStream[index] == '$') {
			if (index == n-1) {
				v.push_back(Token("wrongToken","!"));
			}
			else {
				char c = charStream[index+1];
				string s = "$";
				if (c >= 'a' and c <= 'h') {
					s.push_back(c);
					v.push_back(Token("colConstant",s));
					++index;
				}
				else if (c >= '1' and c <= '8') {
					s.push_back(c);
					v.push_back(Token("rowConstant",s));
					++index;
				}
				else {
					v.push_back(Token("wrongToken",s));
				}
			}
		}
		else if (charStream[index] >= 'A' and charStream[index] <= 'Z') {
			char c = charStream[index];
			string s = "";
			s.push_back(c);
			if (c == 'P' or c == 'N' or c == 'B' or c == 'R' or c == 'Q' or c == 'K') {
				v.push_back(Token("typeConstant",s));
			}
			else {
				v.push_back(Token("wrongToken",s));
			}
		}
		else if (charStream[index] == '"') {
			//we know that there is a matching quotation mark
			string s = "\"";
			do {
				++index;
				s.push_back(charStream[index]);
			} while (charStream[index] != '"');
			v.push_back(Token("string",s));
		}
		//everything that's left at this point can not be discerned with the first char
		//in particular, tokens starting with lower case letter, sub operator and numbers
		else {
			pair<string,int> nextToken = longestTokenType(charStream, index);
			string content = "";
			for (int i = index; i < index + nextToken.second; ++i) {
				content.push_back(charStream[i]);
			}
			v.push_back(Token(nextToken.first, content));
			index = index + nextToken.second;
			--index; //to compensate all-case increase
		}
		++index;
	}
	check_no_wrongTokens(v);
	return v;
}

