#ifndef Preprocessing_Included
#define Preprocessing_Included

#include <vector>

using namespace std;

struct Source {
	vector<char> searchModule;
	vector<char> evalModule;
	vector<char> endgameModule;
	vector<char> openingModule;

	Source() {
		searchModule = vector<char> (0);
		evalModule = vector<char> (0);
		endgameModule = vector<char> (0);
		openingModule = vector<char> (0);
	}
};

Source preprocessing(const vector<char>& charStream);

void printSource(const Source& source);

#endif