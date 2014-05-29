#include "generated_eval.h"
#include "predefined_functions.h"
#include "predefined_functions.tcc"
using namespace std;

// Configs
const int centipawn_value = 32000;
const bool default_PStables = false;

// Globals

// Functions Headers
int func_prova();
void func_cosa();

// Rules Headers
long int rule_checkCoses();

// Preamble for array initialization
void preamble() {
}

// Functions code
int func_prova() {
    return 2000;
}

void func_cosa() {
    return;
}

// Rules code
long int rule_checkCoses() {
    int x, y, z;
    y = 20000;
    z = y;
    string s;
    s = "patates";
    string p;
    p = concat(s," i dracs");
    vector<string> vec;
    vector<string> _array_1 = vector<string>();
    vec = _array_1;
    vector<string> _array_2 = vector<string>();
    _array_2.push_back(p);
    vec = concat(vec,_array_2);
    func_len(vec);
    func_prova();
    x = 4000;
    return x + y + z + func_len(vec);
}

// Opening eval
long int opn_eval() {
    long int score = 0;
    score += rule_checkCoses();
    return score;
}

// Midgame eval
long int mid_eval() {
    long int score = 0;
    score += rule_checkCoses();
    return score;
}

// Endgame eval
long int end_eval() {
    long int score = 0;
    score += rule_checkCoses();
    return score;
}

