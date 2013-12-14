#ifndef Tests_Included
#define Tests_Included

#include <vector>

using namespace std;

/*
the parameter should be the result of the read_input function
the parameter is printed in order to see its content
*/
void test_read_input(const vector<char>& input);


/*
a sequence of automated tests are executed
*/
void test_token_patterns();

#endif