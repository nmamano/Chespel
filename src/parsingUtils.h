#ifndef ParsingUtils_Included
#define ParsingUtils_Included

#include <vector>
#include <string>
#include <utility>

using namespace std;

//given a text, returns a vector with its content split in lines.
//the \n at the end of the line is not added to each particular line
vector<vector<char> > splitIntoLines(const vector<char>& v);
//given a vector of lines (sequences of characters without \n),
//merges them into a single text, separating them with \n
vector<char> mergeLines(const vector<char>& v);

//given a text, returns a vector with its content split in words.
//the spaces between words are not removed
vector<vector<char> > splitIntoWords(const vector<char>& v);
//given a vector of words (sequences of characters without spaces, tabs nor \n),
//merges them into a single line, separating them with ' '
vector<vector<char> > mergeWords(const vector<char>& v);

//replaces each sequence of one or more spaces (' ' or '\t') for a single ' '
void mergeMultipleSpaces(vector<char>& v);
//replaces each sequence of two or more \n for a single \n
void mergeMultipleNewlines(vector<char>& v);
//if v contains lines with only ' ' or '\t', they are removed
void removeLinesWithSpaces(vector<char>& v);
//replaces sequences of one or more spaces (' ' or '\t') followed by '\n' for just '\n'
void removeSpacesBeforeNewlines(vector<char>& v);
//assuming a valid line must end in one of the characters in validEndings, replaces each
//'\n' preceeded by a character not in validEndings for a ' '
void mergeSplitLines(const vector<char>& validEndings, vector<char>& v);

//replaces each occurrence of pattern for substitution in v
void substitute(const string& pattern, const string& substitution, vector<char>& v);

//returns all the starting positions of the word word in v
vector<int> wordPositions(const string& word, const vector<char>& v);
//returns all the stating positions of the words in words in v
vector<int> wordsPositions(const vector<string>& words, const vector<char>& v);
//returns the words appearing after word, together with their starting position.
vector<pair<string,int> > wordsAfter(const string& word, const vector<char>& v);
//assuming pos is the first position of a word, returns the first position of the next word in v
//or -1 if it was the last word.
int nextWordPos(int pos, const vector<char>& v);




#endif