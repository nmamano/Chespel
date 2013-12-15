#include "preprocessing.h"
#include "utils.h"
#include <vector>
#include <iostream>
#include <string>
#include <cstdlib>

using namespace std;

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
		cerr << "Preprocessing error: comments' opening and closing marks do not match" << endl;
		exit(0);
	}
	return withoutComs;
}

void checkMatchingQuotations(const vector<char>& input) {
	if (numberAppearances('"',input)%2 != 0) {
		cerr << "Preprocessing error: string quotation marks do not match" << endl;
		exit(0);
	}
}

char nextNonSpaceChar(const vector<char>& input, int index) {
	int s = input.size();
	while (index < s) {
		if (input[index] != ' ') return input[index];
		++index;
	}
	return '\n'; //consider EOF = \n (for convenience)
}

bool matchInStream(const string& s, const vector<char>& input, int index) {
	int s_size = s.size();
	int input_size = input.size();
	if (index + s_size > input_size) return false;
	for (int i = 0; i < s_size; ++i) {
		if (s[i] != input[index+i]) return false;
	}
	return true;
}

//returns the index first character after the endline after the word evaluation
int evaluationModuleStart(const vector<char>& input) {
	int s = input.size();
	int index = 0;
	while (index < s) {
		//we are using that we have removed spaces before endlines
		if (matchInStream("evaluation\n", input, index)) {
			return index + 11;
		}
		++index;
	}
	cerr << "Preprocessing error: evaluation module not defined" << endl;
	exit(0);
}

//returns the index of the 'm' character of the "module" keyword of the
//next module defined after the evaluation module
//or EOF if there is no such module
int evaluationModuleEnd(const vector<char>& input) {
	int s = input.size();
	int index = evaluationModuleStart(input);
	while (index < s) {
		if (matchInStream("module\n", input, index)) {
			return index;
		}
		++index;
	}
	return index;
}

vector<char> removeSpacesBeforeNewlines(const vector<char>& input) {
	vector<char> charStream(0);
	int s = input.size();
	int index = 0;
	while (index < s) {
		if (input[index] != ' ' or nextNonSpaceChar(input,index+1) != '\n') {
			charStream.push_back(input[index]);
		}
		++index;
	}
	return charStream;
}

vector<char> deleteRedundantNewlines(const vector<char>& input) {
	vector<char> charStream(0);
	int index = evaluationModuleStart(input)+1;
	int end = evaluationModuleEnd(input);
	while (index < end) {
		if (input[index] != '\n' or 
			(input[index-1] == ':' or input[index-1] == ';')) {
			charStream.push_back(input[index]);
		}
		++index;
	}
	return charStream;
}

//splits the source code in modules and performs some preprocessing tasks
vector<char> preprocessing(const vector<char>& input) {
	vector<char> charStream = removeComments(input);
	checkMatchingQuotations(charStream);
	charStream = removeSpacesBeforeNewlines(charStream);
	charStream = deleteRedundantNewlines(charStream);
	//charStream = changeBlockModeToBrackets(charSteam);
	return charStream;
}