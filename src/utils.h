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

#endif