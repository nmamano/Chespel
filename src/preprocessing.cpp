#include "preprocessing.h"
#include "utils.h"
#include <vector>
#include <iostream>
#include <string>
#include <cstdlib>

using namespace std;

void printSource(const Source& source) {
	if (source.searchModule.size() > 0) {
		cout << "Search module" << endl;
		printVector(source.searchModule);
		cout << endl << "======================" << endl;
	}
	if (source.evalModule.size() > 0) {
		cout << "Eval module" << endl;
		printVector(source.evalModule);
		cout << endl << "======================" << endl;
	}
	if (source.endgameModule.size() > 0) {
		cout << "Endgame module" << endl;
		printVector(source.endgameModule);
		cout << endl << "======================" << endl;
	}
	if (source.openingModule.size() > 0) {
		cout << "Opening module" << endl;
		printVector(source.openingModule);
		cout << endl << "======================" << endl;
	}
}

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

//the lines containing "module xxxx" are not included in the corresponding module
Source splitSourceInModules(const vector<char>& input) {
	int searchStartLine = -1, searchEndLine = -1;
	int evalStartLine = -1, evalEndLine = -1;
	int endgameStartLine = -1, endgameEndLine = -1;
	int openingStartLine = -1, openingEndLine = -1;

	vector<vector<char> > lines = splitIntoLines(input);
	int n = lines.size();
	for (int i = 0; i < n; ++i) {
		if (contains("module", lines[i])) {
			if (contains("search", lines[i])) {
				if (searchStartLine != -1) {
					cerr << "Preprocessing error (line " << i << "): search module defined twice" << endl;
					exit(0);
				}
				searchStartLine = i+1;
			}
			else if (contains("endgame", lines[i])) {
				if (endgameStartLine != -1) {
					cerr << "Preprocessing error (line " << i << "): endgame module defined twice" << endl;
					exit(0);
				}
				endgameStartLine = i+1;
			}
			else if (contains("opening", lines[i])) {
				if (openingStartLine != -1) {
					cerr << "Preprocessing error (line " << i << "): opening module defined twice" << endl;
					exit(0);
				}
				openingStartLine = i+1;
			}			
			else if (contains("evaluation", lines[i])) {
				if (evalStartLine != -1) {
					cerr << "Preprocessing error (line " << i << "): evaluation module defined twice" << endl;
					exit(0);
				}
				evalStartLine = i+1;
			}
			else {
				cerr << "Preprocessing error (line " << i << "): module keyword without specified module name" << endl;
				exit(0);
			}
		}
	}

	if (searchStartLine != -1) {
		vector<int> candidates(0);
		if (evalStartLine > searchStartLine) candidates.push_back(evalStartLine-2);
		if (openingStartLine > searchStartLine) candidates.push_back(openingStartLine-2);
		if (endgameStartLine > searchStartLine) candidates.push_back(endgameStartLine-2);
		
		if (candidates.size() == 0) searchEndLine = n-1;
		else searchEndLine = min(candidates);
		//searchEndLine = samllest number larger or equal than searchSearchLine from
		//candidates: evalStartLine -2, openingStartLine -2, endgameStartLine -2
		//or last line of none		
	}
	if (evalStartLine != -1) {
		vector<int> candidates(0);
		if (searchStartLine > evalStartLine) candidates.push_back(searchStartLine-2);
		if (openingStartLine > evalStartLine) candidates.push_back(openingStartLine-2);
		if (endgameStartLine > evalStartLine) candidates.push_back(endgameStartLine-2);
		
		if (candidates.size() == 0) evalEndLine = n-1;
		else evalEndLine = min(candidates);		
	}
	if (openingStartLine != -1) {
		vector<int> candidates(0);
		if (searchStartLine > openingStartLine) candidates.push_back(searchStartLine-2);
		if (evalStartLine > openingStartLine) candidates.push_back(evalStartLine-2);
		if (endgameStartLine > openingStartLine) candidates.push_back(endgameStartLine-2);
		
		if (candidates.size() == 0) openingEndLine = n-1;
		else openingEndLine = min(candidates);		
	}
	if (endgameStartLine != -1) {
		vector<int> candidates(0);
		if (searchStartLine > endgameStartLine) candidates.push_back(searchStartLine-2);
		if (evalStartLine > endgameStartLine) candidates.push_back(evalStartLine-2);
		if (openingStartLine > endgameStartLine) candidates.push_back(openingStartLine-2);
		
		if (candidates.size() == 0) endgameEndLine = n-1;
		else endgameEndLine = min(candidates);		
	}

	Source source;

	if (searchStartLine != -1) {
		for (int i = searchStartLine; i <= searchEndLine; ++i) {
			append(lines[i], source.searchModule);
		}
	}
	if (endgameStartLine != -1) {
		for (int i = endgameStartLine; i <= endgameEndLine; ++i) {
			append(lines[i], source.endgameModule);
		}
	}
	if (openingStartLine != -1) {
		for (int i = openingStartLine; i <= openingEndLine; ++i) {
			append(lines[i], source.endgameModule);
		}
	}
	if (evalStartLine != -1) {
		for (int i = evalStartLine; i <= evalEndLine; ++i) {
			append(lines[i], source.evalModule);
		}
	}
	else {
		cerr << "Preprocessing error: evaluation module not defined" << endl;
		exit(0);	
	}

	return source;
}

//only applicable to the eval module
vector<char> deleteRedundantNewlines(const vector<char>& input) {
	vector<char> charStream(0);
	int n = input.size();
	int i = 0;
	while (i < n) {
		if (input[i] != '\n' or 
			(i > 0 and (input[i-1] == ':' or input[i-1] == ';'))) {
			charStream.push_back(input[i]);
		}
		++i;
	}
	return charStream;
}

//splits the source code in modules and performs some preprocessing tasks
Source preprocessing(const vector<char>& input) {
	vector<char> charStream = removeComments(input);
	checkMatchingQuotations(charStream);
	charStream = removeSpacesBeforeNewlines(charStream);
	Source source = splitSourceInModules(charStream);

	source.evalModule = deleteRedundantNewlines(source.evalModule);
	//charStream = changeBlockModeToBrackets(charSteam);
	return source;
}