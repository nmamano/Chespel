#include "utils.h"
#include <vector>
#include <string>
#include <iostream>
#include <stdio.h>
#include <cstdlib>

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

bool matchInStream(const string& s, const vector<char>& input, int index) {
	int s_size = s.size();
	int input_size = input.size();
	if (index + s_size > input_size) return false;
	for (int i = 0; i < s_size; ++i) {
		if (s[i] != input[index+i]) return false;
	}
	return true;
}

bool contains(const string& s, const vector<char>& v) {
	int sSize = s.size();
	int vSize = v.size();
	if (sSize > vSize) return false;
	for (int i = 0; i < vSize-sSize+1; ++i) {
		if (matchInStream(s,v,i)) return true;
	}
	return false;
}

void append(const vector<char>& appendix, vector<char>& v) {
	int n = appendix.size();
	for (int i = 0; i < n; ++i) v.push_back(appendix[i]);
}

int min(const vector<int>& v) {
	int n = v.size();
	if (n == 0) {
		cerr << "Internal error: min function with empty vector as parameter" << endl;
		exit(0);
	}
	int min = v[0];
	for (int i = 1; i < n; ++i) {
		if (v[i] < min) min = v[i];
	}
	return min;
}

vector<vector<char> > splitIntoLines(const vector<char>& v) {
	vector<vector<char> > lines(0);
	int n = v.size();
	vector<char> line(0);
	for (int i = 0; i < n; ++i) {
		line.push_back(v[i]);
		if (v[i] == '\n') {
			lines.push_back(line);
			line = vector<char> (0);
		}
	}
	if (line.size() > 0) lines.push_back(line);
	return lines;
}

bool isDigit(char c) {
	return c >= '0' and c <= '9';
}

bool isLowerCase(char c) {
	return c >= 'a' and c <= 'z';
}

bool isUpperCase(char c) {
	return c >= 'A' and c <= 'Z';
}

bool isLetter(char c) {
	return isLowerCase(c) or isUpperCase(c);
}