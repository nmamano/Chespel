#ifndef TokenPatterns_Included
#define TokenPatterns_Included

#include <vector>
#include <string>

using namespace std;

bool isComment(const vector<char>& token);
bool isMultilineComment(const vector<char>& token);

bool isNum(const vector<char>& token);

bool isID(const vector<char>& token);

bool isSpace(const vector<char>& token);
bool isTab(const vector<char>& token);
bool isNewline(const vector<char>& token);

bool isComma(const vector<char>& token);
bool isColon(const vector<char>& token);
bool isSemicolon(const vector<char>& token);
bool isPeriod(const vector<char>& token);

bool isOpenParentheses(const vector<char>& token);
bool isClosedParentheses(const vector<char>& token);
bool isOpenBrackets(const vector<char>& token);
bool isClosedBrackets(const vector<char>& token);

bool isSumOperator(const vector<char>& token);
bool isRestOperator(const vector<char>& token);
bool isProductOperator(const vector<char>& token);
bool isDivisionOperator(const vector<char>& token);

bool isLTComparison(const vector<char>& token);
bool isGTComparison(const vector<char>& token);
bool isLEComparison(const vector<char>& token);
bool isGEComparison(const vector<char>& token);
bool isEQComparison(const vector<char>& token);
bool isNEComparison(const vector<char>& token);

bool isAssignment(const vector<char>& token);


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

//auxiliar methods

string vector2string(const vector<char>& token);

#endif