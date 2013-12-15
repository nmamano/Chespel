#include <iostream>
#include <vector>
#include <string>
#include <stdio.h>
#include "tokenPatterns.h"
#include "tests.h"
#include "utils.h"

using namespace std;


struct Token {
	string type;
	string content;

	Token(const string& type, const string& content) {
		this->type = type;
		this->content = content;
	}
};

struct Node {
	Token token;
	Node* father;
	vector<Node*> sons;
};

struct AbstractSyntaxTree {
	Node* root;
};


vector<char> read_input() {
	vector<char> input(0);
	
	bool eof = false;
	while (not eof) {
		int c;
		c = getchar();
		if (c == -1) eof = true;
		else input.push_back((char) c);
	}

	return input;
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
	return withoutComs;
}

vector<char> lexical_analysis(const vector<char>& input) {
	vector<char> result = removeComments(input);
	return result;
}

int main() {

	//vector<char> input;
	//input = read_input();

	//test_token_patterns();
	test_token_max_lengths();
	//vector<char> token_stream;
	//token_stream = lexical_analysis(input);

	//printVector(token_stream);
	
	//AbstractSyntaxTree ast;
	//ast = syntax_analysis(token_stream);

	//semantic_analysis(ast);
}

