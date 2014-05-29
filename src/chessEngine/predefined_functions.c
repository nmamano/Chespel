#include "predefined_functions.h"
#include "generated_eval.h"
#include "predefined_functions.tcc"
using namespace std;

bool inverted_players;
vector<vector<int> > pieces_mem; // pair white, odd black

void invert_players() {
    inverted_players = not inverted_players;
}

void reset() {
    inverted_players = false;
    pieces_mem = vector<vector<int> > (14, vector<int>());
    for (int j = 1; j < num_pieces; ++j) {
        int i = pieces[j];
        if (i == 0) continue; // no piece in cell -> dead piece
        switch (i) {
            case wpawn:
                pieces_mem[0].push_back(i); // white piece
                pieces_mem[2].push_back(i); // white pawn
                break;
            case bpawn:
                pieces_mem[1].push_back(i); // black piece
                pieces_mem[3].push_back(i); // black pawn
                break;
            case wbishop:
                pieces_mem[0].push_back(i); // white piece
                pieces_mem[4].push_back(i); // and so on...
                break;
            case bbishop:
                pieces_mem[1].push_back(i); 
                pieces_mem[5].push_back(i); 
                break;
            case wrook:
                pieces_mem[0].push_back(i); 
                pieces_mem[6].push_back(i); 
                break;
            case brook:
                pieces_mem[1].push_back(i); 
                pieces_mem[7].push_back(i); 
                break;
            case wknight:
                pieces_mem[0].push_back(i); 
                pieces_mem[8].push_back(i); 
                break;
            case bknight:
                pieces_mem[1].push_back(i); 
                pieces_mem[9].push_back(i); 
                break;
            case wking:
                pieces_mem[0].push_back(i); 
                pieces_mem[10].push_back(i);
                break;
            case bking:
                pieces_mem[1].push_back(i); 
                pieces_mem[11].push_back(i);
                break;
            case wqueen:
                pieces_mem[0].push_back(i); 
                pieces_mem[12].push_back(i);
                break;
            case bqueen:
                pieces_mem[1].push_back(i); 
                pieces_mem[13].push_back(i);
                break;
        }
    }
}

int self() { return (inverted_players ? 1 : 0); }
int rival() { return (inverted_players ? 0 : 1); }

int color(int player) { // 0 white, 1 black
    if (player == 1) // inverted player
        return white_to_move;
    return 1-white_to_move;
}

int rev_rank[9] = {
0,8,7,6,5,4,3,2,1};


string concat(string s0, string s1) {
    return s0 + s1;
}

vector<int> get_pieces(int player, int type) {
    int piece_code = type*2 + color(player); // mem_table is coded like 'type'
    return pieces_mem[piece_code];
}

int func_value(int piece) { // TODO
    switch (board[piece]) {
        case wpawn:
        case bpawn:
            return 100 * centipawn_value;
        case wbishop:
        case bbishop:

        case wrook:
        case brook:
        case wknight:
        case bknight:
        case wking:
        case bking:
        case wqueen:
        case bqueen:
            break;
    }
}


int func_cell(int piece) {
    return piece;
}

int func_rank(int piece){
    // If white (0), it's the same as row, if black, reverse it
    return (color(self()) == 0 ? rank(piece) : rev_rank[rank(piece)]);
}

int func_row(int piece) {
    return rank (piece); // in reality is the row
}

int func_file(int piece) {
    return file (piece) +1;
}

int func_player(int piece) {
    switch (board[piece]) {
        case wpawn:
        case wbishop:
        case wrook:
        case wknight:
        case wking:
        case wqueen:
            return (color(self()) == 0 ? self() : rival());
        case bpawn:
        case bbishop:
        case brook:
        case bknight:
        case bking:
        case bqueen:
            return (color(self()) == 1 ? self() : rival());
    }
}

int func_piece(int cell) {
    return cell;
}

bool func_castled(int player) { //?
}

int func_startingRow(int piece) { // wouldn't be better startingRank and/or conversion from rank to row and row to rank?
    
}
