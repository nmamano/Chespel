/*
includes a sequence of tests
to test different parts of the parser

some tests are automated with asserts,
whereas others print output that need to be checked by humans
*/

#ifndef Tests_Included
#define Tests_Included

#include <vector>

using namespace std;


void test_token_patterns();

void test_token_max_lengths();

void test_lexical_parsing();

void test_preprocessing();

#endif