#include "tokenPatterns.h"
#include "utils.h"
#include "constants.h"
#include <vector>
#include <string>
#include <utility>
#include <iostream>
#include <cstdlib>

using namespace std;

//auxiliar methods

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

bool isKeyword(const vector<char>& token) {
	string s = vector2string(token);
	for (int i = 0; i < keywordsCount; ++i) {
		if (s == keywords[i]) return true;
	}
	return false;
}

bool isModuleName(const vector<char>& token) {
	string s = vector2string(token);
	for (int i = 0; i < moduleNamesCount; ++i) {
		if (s == moduleNames[i]) return true;
	}
	return false;
}


bool isCellConstant(const vector<char>& token) {
	string s = vector2string(token);
	for (int i = 0; i < cellValuesCount; ++i) {
		if (s == cellValues[i]) return true;
	}
	return false;
}

bool isPlayerConstant(const vector<char>& token) {
	string s = vector2string(token);
	for (int i = 0; i < playerValuesCount; ++i) {
		if (s == playerValues[i]) return true;
	}
	return false;
}

bool isTypeConstant(const vector<char>& token) {
	string s = vector2string(token);
	for (int i = 0; i < typeValuesLongCount; ++i) {
		if (s == typeValuesLong[i]) return true;
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
	for (int i = 0; i < boolValuesCount; ++i) {
		if (s == boolValues[i]) return true;
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

bool isPrefixKeyword(const vector<char>& token) {
	for (int i = 0; i < keywordsCount; ++i) {
		if (isPrefix(token,keywords[i])) return true;
	}
	return false;
}

bool isPrefixModuleName(const vector<char>& token) {
	for (int i = 0; i < moduleNamesCount; ++i) {
		if (isPrefix(token,moduleNames[i])) return true;
	}
	return false;
}


bool isPrefixCellConstant(const vector<char>& token) {
	for (int i = 0; i < cellValuesCount; ++i) {
		if (isPrefix(token,cellValues[i])) return true;
	}
	return false;
}

bool isPrefixPlayerConstant(const vector<char>& token) {
	for (int i = 0; i < playerValuesCount; ++i) {
		if (isPrefix(token,playerValues[i])) return true;
	}
	return false;
}

bool isPrefixTypeConstant(const vector<char>& token) {
	for (int i = 0; i < typeValuesLongCount; ++i) {
		if (isPrefix(token,typeValuesLong[i])) return true;
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
	for (int i = 0; i < boolValuesCount; ++i) {
		if (isPrefix(token,boolValues[i])) return true;
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

int numTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixNum, isNum);
}


int keywordTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixKeyword, isKeyword);
}

int idTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixId, isId);
}

int subOperatorTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixSubOperator, isSubOperator);
}

int moduleNameTokenMaxLength(const vector<char>& charStream, int startingPos) {
	return tokenMaxLength(charStream, startingPos, isPrefixModuleName, isModuleName);
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
	
	v.push_back(make_pair("keyword", keywordTokenMaxLength(charStream, startingPos)));
	v.push_back(make_pair("moduleName", moduleNameTokenMaxLength(charStream, startingPos)));
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


vector<Token> lexical_parse(const vector<char>& charStream) {
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

