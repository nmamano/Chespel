#include <iostream>
#include <vector>
#include <string>
#include "constants.h"
#include "preprocessing.h"
#include "tokenPatterns.h"
#include "tests.h"
#include "utils.h"

using namespace std;

struct Node {
	Token token;
	Node* father;
	vector<Node*> sons;
};

struct AbstractSyntaxTree {
	Node* root;
};


int main() {

	//vector<char> input;
	//input = read_input();

	//test_token_patterns();
	//test_token_max_lengths();
	test_preprocessing();
	//test_lexical_parsing();
	//vector<char> token_stream;
	//token_stream = lexical_analysis(input);

	//printVector(token_stream);
	
	//AbstractSyntaxTree ast;
	//ast = syntax_analysis(token_stream);

	//semantic_analysis(ast);
}

