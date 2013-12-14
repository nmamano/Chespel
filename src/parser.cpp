#include <iostream>
#include <vector>
#include <string>
#include <stdio.h>

using namespace std;

struct Node {
	string type;
	string content;
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

void test_read_input(const vector<char>& input) {
	for (unsigned int i = 0; i < input.size(); ++i) {
		cout << input[i];
	}
	cout << endl;
}

int main() {

	vector<char> input;
	input = read_input();
	
	test_read_input(input);

	//vector<string> token_stream;
	//token_stream = lexical_analysis(input);
	
	//AbstractSyntaxTree ast;
	//ast = syntax_analysis(token_stream);

	//semantic_analysis(ast);
}

