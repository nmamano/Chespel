#ifndef Utils_Included
#define Utils_Included

#include <vector>
#include <string>

using namespace std;

string vector2string(const vector<char>& token);
vector<char> string2vector(const string& s);
void printVector(const vector<char>& v);

int numberAppearances(char c, const vector<char>& v);

//includes both the empty prefix and whole v
vector<vector<char> > getPrefixes(const vector<char>& v);

//stores the input in a vector of chars
vector<char> read_input();

//returns whether charStream contains s starting at index
bool matchInStream(const string& s, const vector<char>& charStream, int index);

bool contains(const string& s, const vector<char>& v);

void append(const vector<char>& appendix, vector<char>& v);

int min(const vector<int>& v);

vector<vector<char> > splitIntoLines(const vector<char>& v);

bool isDigit(char c);

bool isLowerCase(char c);

bool isUpperCase(char c);

bool isLetter(char c);

#endif