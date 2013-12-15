#ifndef TokenPatterns_Included
#define TokenPatterns_Included

#include <vector>

using namespace std;

bool isComment(const vector<char>& token);
bool isMultilineComment(const vector<char>& token);

bool isNum(const vector<char>& token);

bool isId(const vector<char>& token);

bool isString(const vector<char>& token);

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

bool isRowConstant(const vector<char>& token);
bool isColConstant(const vector<char>& token);
bool isCellConstant(const vector<char>& token);
bool isPlayerConstant(const vector<char>& token);
bool isTypeConstant(const vector<char>& token);
bool isPieceConstant(const vector<char>& token);
bool isBoolConstant(const vector<char>& token);





bool isPrefixComment(const vector<char>& token);
bool isPrefixMultilineComment(const vector<char>& token);

bool isPrefixNum(const vector<char>& token);

bool isPrefixId(const vector<char>& token);

bool isPrefixString(const vector<char>& token);

bool isPrefixSpace(const vector<char>& token);
bool isPrefixTab(const vector<char>& token);
bool isPrefixNewline(const vector<char>& token);

bool isPrefixComma(const vector<char>& token);
bool isPrefixColon(const vector<char>& token);
bool isPrefixSemicolon(const vector<char>& token);
bool isPrefixPeriod(const vector<char>& token);

bool isPrefixOpenParentheses(const vector<char>& token);
bool isPrefixClosedParentheses(const vector<char>& token);
bool isPrefixOpenBrackets(const vector<char>& token);
bool isPrefixClosedBrackets(const vector<char>& token);

bool isPrefixSumOperator(const vector<char>& token);
bool isPrefixRestOperator(const vector<char>& token);
bool isPrefixProductOperator(const vector<char>& token);
bool isPrefixDivisPrefixionOperator(const vector<char>& token);

bool isPrefixLTComparisPrefixon(const vector<char>& token);
bool isPrefixGTComparisPrefixon(const vector<char>& token);
bool isPrefixLEComparisPrefixon(const vector<char>& token);
bool isPrefixGEComparisPrefixon(const vector<char>& token);
bool isPrefixEQComparisPrefixon(const vector<char>& token);
bool isPrefixNEComparisPrefixon(const vector<char>& token);

bool isPrefixAssignment(const vector<char>& token);


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

bool isPrefixRowConstant(const vector<char>& token);
bool isPrefixColConstant(const vector<char>& token);
bool isPrefixCellConstant(const vector<char>& token);
bool isPrefixPlayerConstant(const vector<char>& token);
bool isPrefixTypeConstant(const vector<char>& token);
bool isPrefixPieceConstant(const vector<char>& token);
bool isPrefixBoolConstant(const vector<char>& token);

#endif