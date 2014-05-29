#include "generated_eval.h"
#include "predefined_functions.h"
#include "predefined_functions.tcc"
using namespace std;

// Configs
const int centipawn_value = 32000;
const bool default_PStables = false;

// Globals

// Functions Headers

// Rules Headers
long int rule_cosa();

// Preamble for array initialization
void preamble() {
}

// Functions code
// Rules code
long int rule_cosa() {
    int x;
    x = 2000;
    int y;
    y = 2000 + x;
    return 2000 + y;
}

// Opening eval
long int opn_eval() {
    long int score = 0;
    score += rule_cosa();
    reset();
    return score;
}

// Midgame eval
long int mid_eval() {
    long int score = 0;
    score += rule_cosa();
    reset();
    return score;
}

// Endgame eval
long int end_eval() {
    long int score = 0;
    score += rule_cosa();
    reset();
    return score;
}

