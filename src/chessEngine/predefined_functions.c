#include "predefined_functions.h"
#include "predefined_functions.tcc"
using namespace std;

bool inverted_players = false;

string concat(string s0, string s1) {
    return s0 + s1;
}

void invert_players() {
    inverted_players = not inverted_players;
}

vector<int> get_pieces(int player, int type) {
    if (inverted_players) player = (player + 1) % 2; // player : 0 self; 1 rival
    vector<int> pieces = vector<int>();
    switch (type) {
        case 0: // all pieces
        case 1: // pawns
        case 2: // bishops
        case 3: // rooks
        case 4: // knights
        case 5: // kings
        case 6: // queens
            break;
    }
}
