#ifndef TokenPatterns_Included
#define TokenPatterns_Included

#include <vector>
#include <string>

using namespace std;

struct Token {
	string type;
	string content;

	Token(const string& type, const string& content) {
		this->type = type;
		this->content = content;
	}

	Token() {
		type = "";
		content = "";
	}
};


bool isNum(const vector<char>& token);
bool isId(const vector<char>& token);

bool isSubOperator(const vector<char>& token);

bool isModuleKeyword(const vector<char>& token);
bool isModuleName(const vector<char>& token);

bool isSymKeyword(const vector<char>& token);
bool isRuleKeyword(const vector<char>& token);
bool isPieceKeyword(const vector<char>& token);
bool isCellKeyword(const vector<char>& token);
bool isWithKeyword(const vector<char>& token);
bool isIfKeyword(const vector<char>& token);
bool isScoreKeyword(const vector<char>& token);
bool isLetKeyword(const vector<char>& token);
bool isInKeyword(const vector<char>& token);

bool isCellConstant(const vector<char>& token);
bool isPlayerConstant(const vector<char>& token);
bool isTypeConstant(const vector<char>& token);
bool isPieceConstant(const vector<char>& token);
bool isBoolConstant(const vector<char>& token);




bool isPrefixNum(const vector<char>& token);
bool isPrefixId(const vector<char>& token);

bool isPrefixSubOperator(const vector<char>& token);

bool isPrefixModuleKeyword(const vector<char>& token);
bool isPrefixModuleName(const vector<char>& token);

bool isPrefixSymKeyword(const vector<char>& token);
bool isPrefixRuleKeyword(const vector<char>& token);
bool isPrefixPieceKeyword(const vector<char>& token);
bool isPrefixCellKeyword(const vector<char>& token);
bool isPrefixWithKeyword(const vector<char>& token);
bool isPrefixIfKeyword(const vector<char>& token);
bool isPrefixScoreKeyword(const vector<char>& token);
bool isPrefixLetKeyword(const vector<char>& token);
bool isPrefixInKeyword(const vector<char>& token);

bool isPrefixCellConstant(const vector<char>& token);
bool isPrefixPlayerConstant(const vector<char>& token);
bool isPrefixTypeConstant(const vector<char>& token);
bool isPrefixPieceConstant(const vector<char>& token);
bool isPrefixBoolConstant(const vector<char>& token);



int numTokenMaxLength(const vector<char>& charStream, int startingPos);
int idTokenMaxLength(const vector<char>& charStream, int startingPos);

int subOperatorTokenMaxLength(const vector<char>& charStream, int startingPos);

int moduleKeywordTokenMaxLength(const vector<char>& charStream, int startingPos);
int moduleNameTokenMaxLength(const vector<char>& charStream, int startingPos);

int symKeywordTokenMaxLength(const vector<char>& charStream, int startingPos);
int ruleKeywordTokenMaxLength(const vector<char>& charStream, int startingPos);
int pieceKeywordTokenMaxLength(const vector<char>& charStream, int startingPos);
int cellKeywordTokenMaxLength(const vector<char>& charStream, int startingPos);
int withKeywordTokenMaxLength(const vector<char>& charStream, int startingPos);
int ifKeywordTokenMaxLength(const vector<char>& charStream, int startingPos);
int scoreKeywordTokenMaxLength(const vector<char>& charStream, int startingPos);
int letKeywordTokenMaxLength(const vector<char>& charStream, int startingPos);
int inKeywordTokenMaxLength(const vector<char>& charStream, int startingPos);

int rowConstantTokenMaxLength(const vector<char>& charStream, int startingPos);
int colConstantTokenMaxLength(const vector<char>& charStream, int startingPos);
int cellConstantTokenMaxLength(const vector<char>& charStream, int startingPos);
int playerConstantTokenMaxLength(const vector<char>& charStream, int startingPos);
int typeConstantTokenMaxLength(const vector<char>& charStream, int startingPos);
int pieceConstantTokenMaxLength(const vector<char>& charStream, int startingPos);
int boolConstantTokenMaxLength(const vector<char>& charStream, int startingPos);





pair<string,int> longestTokenType(const vector<char>& charStream, int startingPos);

//the important function: does the lexical parsing
vector<Token> lexical_parse(const vector<char>& charStream);

#endif