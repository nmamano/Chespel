#ifndef TokenPatterns_Included
#define TokenPatterns_Included

#include <vector>
#include <string>

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
bool isPrefixDivisionOperator(const vector<char>& token);

bool isPrefixLTComparison(const vector<char>& token);
bool isPrefixGTComparison(const vector<char>& token);
bool isPrefixLEComparison(const vector<char>& token);
bool isPrefixGEComparison(const vector<char>& token);
bool isPrefixEQComparison(const vector<char>& token);
bool isPrefixNEComparison(const vector<char>& token);

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






int commentTokenMaxLength(const vector<char>& charStream, int startingPos);
int multilineCommentTokenMaxLength(const vector<char>& charStream, int startingPos);

int numTokenMaxLength(const vector<char>& charStream, int startingPos);

int idTokenMaxLength(const vector<char>& charStream, int startingPos);

int stringTokenMaxLength(const vector<char>& charStream, int startingPos);

int spaceTokenMaxLength(const vector<char>& charStream, int startingPos);
int tabTokenMaxLength(const vector<char>& charStream, int startingPos);
int newlineTokenMaxLength(const vector<char>& charStream, int startingPos);

int commaTokenMaxLength(const vector<char>& charStream, int startingPos);
int colonTokenMaxLength(const vector<char>& charStream, int startingPos);
int semicolonTokenMaxLength(const vector<char>& charStream, int startingPos);
int periodTokenMaxLength(const vector<char>& charStream, int startingPos);

int openParenthesesTokenMaxLength(const vector<char>& charStream, int startingPos);
int closedParenthesesTokenMaxLength(const vector<char>& charStream, int startingPos);
int openBracketsTokenMaxLength(const vector<char>& charStream, int startingPos);
int closedBracketsTokenMaxLength(const vector<char>& charStream, int startingPos);

int sumOperatorTokenMaxLength(const vector<char>& charStream, int startingPos);
int restOperatorTokenMaxLength(const vector<char>& charStream, int startingPos);
int productOperatorTokenMaxLength(const vector<char>& charStream, int startingPos);
int divisionOperatorTokenMaxLength(const vector<char>& charStream, int startingPos);

int LTComparisonTokenMaxLength(const vector<char>& charStream, int startingPos);
int GTComparisonTokenMaxLength(const vector<char>& charStream, int startingPos);
int LEComparisonTokenMaxLength(const vector<char>& charStream, int startingPos);
int GEComparisonTokenMaxLength(const vector<char>& charStream, int startingPos);
int EQComparisonTokenMaxLength(const vector<char>& charStream, int startingPos);
int NEComparisonTokenMaxLength(const vector<char>& charStream, int startingPos);

int assignmentTokenMaxLength(const vector<char>& charStream, int startingPos);

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





string longestTokenType(const vector<char>& charStream, int startingPos);

#endif