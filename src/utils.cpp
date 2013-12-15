#include "utils.h"
#include <vector>
#include <string>
#include <iostream>
#include <stdio.h>

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

void printVector(const vector<char>& v) {
	int s = v.size();
	for (int i = 0; i < s; ++i) {
		cout << v[i];
	}
}

int numberAppearances(char c, const vector<char>& v) {
	int cont = 0;
	int n = v.size();
	for (int i = 0; i < n; ++i) {
		if (v[i] == c) ++cont;
	}
	return cont;
}

vector<vector<char> > getPrefixes(const vector<char>& v) {
	vector<vector<char> > result(0);
	int s = v.size();
	for (int i = 0; i <= s; ++i) {
		vector<char> prefix(0);
		for (int j = 0; j < i; ++j) {
			prefix.push_back(v[j]);
		}
		result.push_back(prefix);
	}
	return result;
}

vector<char> read_input() {
	vector<char> charStream(0);
	
	bool eof = false;
	while (not eof) {
		int c;
		c = getchar();
		if (c == -1) eof = true;
		else charStream.push_back((char) c);
	}

	return charStream;
}