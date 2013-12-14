#include "utils.h"
#include <vector>
#include <string>

using namespace std;


string vector2string(const vector<char>& token) {
	int n = token.size();
	string s = "";
	for (int i = 0; i < n; ++i) {
		s.push_back(token[i]);
	}
	return s;
}

vector<char> string2vector(const string& s) {
	int n = s.size();
	vector<char> v(n);
	for (int i = 0; i < n; ++i) {
		v[i] = s[i];
	}
	return v;
}