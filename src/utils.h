#ifndef Utils_Included
#define Utils_Included

#include <vector>
#include <string>

using namespace std;

string vector2string(const vector<char>& token);
vector<char> string2vector(const string& s);
void printVector(const vector<char>& v);

int numberAppearances(char c, const vector<char>& v);
#endif