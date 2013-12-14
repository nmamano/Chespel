#include <iostream>
#include <vector>
#include <string>

using namespace std;

struct AbstractSyntaxTree {
	node* root;
};

struct node {
	string type;
	string content;
	node* father;
	vector<node*> sons;
};

int main() {

	vector<char> input;
	input = read_input();
	
	vector<string> token_stream;
	token_stream = lexical_analysis(input);
	
	AbstractSyntaxTree ast;
	ast = syntax_analysis(token_stream);

	semantic_analysis(ast);
}