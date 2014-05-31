#include "predefined_functions.h"
#include "generated_eval.h"
#include "predefined_functions.tcc"
using namespace std;

#define WHITE 1
#define BLACK 0
#define SELF (inverted_players ? 0 : 1)
#define RIVAL (inverted_players ? 1 : 0)

// -------------------------------------------------------------------- //
//  ** Auxiliar functions (not directly called by chp)                  //
// -------------------------------------------------------------------- //

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
        switch (board[i]) {
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

int self() { return SELF; }
int rival() { return RIVAL; }

int color(int player) {
    if (inverted_players) { // inverted player
        if (player == SELF) return (white_to_move == TRUE ? BLACK : WHITE);
        else return (white_to_move == TRUE ? WHITE : BLACK);
    } 
    else { // normal configuration
        if (player == SELF) return (white_to_move == TRUE ? WHITE : BLACK);
        else return (white_to_move == TRUE ? BLACK : WHITE);
    }
}

int pieceColor(int piece) {
    assert (piece != 0);
    switch (board[piece]) {
        case wpawn:
        case wbishop:
        case wknight:
        case wrook:
        case wking:
        case wqueen:
            return WHITE;
        default:
            return BLACK;
    }
}

void check_valid_file(int x) {
    if (x < 1 || x > 8) throw out_of_range("File went out of range.");
}

void check_valid_rank(int x) {
    if (x < 1 || x > 8) throw out_of_range("Rank went out of range.");
}

void check_valid_row(int x) {
    if (x < 1 || x > 8) throw out_of_range("Row went out of range.");
}

void check_valid_cell(int x) {
    if (x < 26 || x > 117) throw out_of_range("Cell went out of range.");
}

int to_cell (int r, int f) {
    return (r+1) * 12 + f + 1;
}

int incr_operation(int object, int incr, string type) {
    incr /= 1000; // Rescale the value
    if (type == "CELL") {
        int r = rank (object);
        int f = file (object);
        f += incr;
        while (f < 1) { f += 8; --r; }
        while (f > 8) { f -= 8; ++r; }
        int c = to_cell(r, f);
        check_valid_cell(c);
        return c;
    }
    check_valid_file(object + incr); // all do the same
    return object + incr;
}

int arith_operation(int o0, int o1, char op, string type) {
    if (type == "CELL") {
        int r0 = rank (o0) - 1;
        int f0 = file (o0) - 1;
        int r1 = rank (o1) - 1;
        int f1 = file (o1) - 1;
        int f, r;
        if (op == '+') {
            f = f0 + f1;
            r = r0 + r1;
            if (f > 7) { f-=8; ++r; }
        }
        else {
            f = f0 - f1;
            r = r0 - r1;
            if (f < 0) { f+=8; --r; }

        }
        return (r*8+f)*1000;
    }
    return (o0 + (op == '+' ? o1 : (-o1)))*1000;
}

string string_concat(string s0, string s1, bool first_string, string type) { return s0 + s1; }

string to_string(bool x, string type) { return (x ? "true" : "false"); }

string to_string(string x, string type) { return x; }

string to_string(int x, string type) {
    if (type ==  "NUM") {
        char buff[40];
        char buff2[4];
        sprintf(buff, "%d", x/1000);
        sprintf(buff2, "%03d", x%1000);
        return string(buff) + "." + string(buff2);
    }
    else if (type == "PIECE") {
        string cell = to_string(x, "CELL");
        string piece;
        switch (board[x]) {
            case wpawn:
                piece = "wP";
                break;
            case wbishop:
                piece = "wB";
                break;
            case wknight:
                piece = "wN";
                break;
            case wrook:
                piece = "wR";
                break;
            case wking:
                piece = "wK";
                break;
            case wqueen:
                piece = "wQ";
                break;
            case bpawn:
                piece = "bP";
                break;
            case bbishop:
                piece = "bB";
                break;
            case bknight:
                piece = "bN";
                break;
            case brook:
                piece = "bR";
                break;
            case bking:
                piece = "bK";
                break;
            case bqueen:
                piece = "bQ";
                break;
        }
        return piece + cell;
    }
    else if (type == "CELL") {
        char str[3] = { file(x)-1 + 'a', rank(x) + '0', 0 }; 
        return string(str) ;
    }
    else if (type == "ROW") {
        return "$" + string(1, x+'0');
    }
    else if (type == "RANK") {
        return "$r" + string(1, x+'0');
    }
    else if (type == "FILE") {
        return "$" + string(1, x-1 +'a');
    }
    else { // PLAYER
        return (x == self() ? "SELF" : "RIVAL");
    }
}

int rev_rank[9] = {
0,8,7,6,5,4,3,2,1};

vector<int> get_pieces(int player, int type) {
    int piece_code = type*2 + (color(player) == WHITE ? 0 : 1); // mem_table is coded like 'type'
    return pieces_mem[piece_code];
}

vector<int> cells() {
    vector<int> result = vector<int>();
    for (int i = 2; i < 10; ++i) 
        for (int j = 2; j < 10; ++j) 
            result.push_back(i*12 + j);
    return result;
}

vector<int> rows() {
    vector<int> result = vector<int>();
    for (int i = 1; i <= 8; ++i) result.push_back(i);
    return result;
}

vector<int> ranks() {
    vector<int> result = vector<int>();
    for (int i = 1; i <= 8; ++i) result.push_back(i);
    return result;
}

vector<int> files() {
    vector<int> result = vector<int>();
    for (int i = 1; i <= 8; ++i) result.push_back(i);
    return result;
}

int get_file(char file) {
    return file - 'a' + 1;
}

int get_row(int row) {
    return row;
}

int get_rank(int rank) {
    return rank;
}

int get_cell(string cell) {
    return (cell[0] - 'a' + 2) + (cell[1] - '1' + 2)*12;
}

vector<int> get_rang_cell(string cell0, string cell1) {
    int c0 = get_cell(cell0);
    int c1 = get_cell(cell1);
    int r0 = rank (c0);
    int r1 = rank (c1);
    int f0 = file (c0);
    int f1 = file (c1);
    vector<int> result = vector<int>();
    if (c0 <= c1) { // c0 closer to top-left border
        if (r0 == r1) // both in same row
            for (int f = f0; f <= f1; ++f) result.push_back(to_cell(r0, f));
        else {
            // first row 
            for (int f = f0; f <= 8; ++f) result.push_back(to_cell(r0, f));
            // rows between r0 and r1
            for (int r = r0+1; r < r1; ++r)
                for (int f = 1; f <= 8; ++f) result.push_back(to_cell(r,f));
            // last row
            for (int f = 1; f <= f1; ++f) result.push_back(to_cell(r1,f));
        }
    }
    else {
        if (r0 == r1) // both in same row
            for (int f = f0; f >= f1; --f) result.push_back(to_cell(r0, f));
        else {
            // first row 
            for (int f = f0; f >= 1; --f) result.push_back(to_cell(r0, f));
            // rows between r0 and r1
            for (int r = r0-1; r > r1; --r)
                for (int f = 8; f >= 1; --f) result.push_back(to_cell(r,f));
            // last row
            for (int f = 8; f >= f1; --f) result.push_back(to_cell(r1,f));
        }
    }
    return result;
}

vector<int> get_rang_row(int row1, int row2) {
    vector<int> result = vector<int>();
    if (row1 <= row2)
        for (int i = row1; i <= row2; ++i) result.push_back(i);
    else 
        for (int i = row1; i >= row2; --i) result.push_back(i);
    return result;
}

vector<int> get_rang_file(int file1, int file2) {
    vector<int> result = vector<int>();
    if (file1 <= file2)
        for (int i = file1; i <= file2; ++i) result.push_back(i);
    else
        for (int i = file1; i >= file2; --i) result.push_back(i);
    return result;
}

vector<int> get_rang_rank(int rank1, int rank2) {
    vector<int> result = vector<int>();
    if (rank1 <= rank2)
        for (int i = rank1; i <= rank2; ++i) result.push_back(i);
    else
        for (int i = rank1; i >= rank2; --i) result.push_back(i);
    return result;
}

bool eq_rankrow (int rank, int row) {
    if (color(SELF) == WHITE) return rank == row;
    return rev_rank[rank] == row;
}

// -------------------------------------------------------------------- //
//  ** Predefined functions called by chp code                          //
// -------------------------------------------------------------------- //

int func_value(int piece) {
    switch (board[piece]) {
        case wpawn:
        case bpawn:
            return 100 * _centipawn_value;
        case wbishop:
        case bbishop:
        case wknight:
        case bknight:
            return 300 * _centipawn_value;
        case wrook:
        case brook:
            return 500 * _centipawn_value;
        case wking:
        case bking:
            return 0 * _centipawn_value;
        case wqueen:
        case bqueen:
            return 900 * _centipawn_value;
            break;
    }
}


int func_cell(int piece) {
    return piece;
}

int func_rank(int piece){
    // If white, it's the same as row, if black, reverse it
    return (color(SELF) == WHITE ? rank(piece) : rev_rank[rank(piece)]);
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
            return (color(SELF) == WHITE ? SELF : RIVAL);
        case bpawn:
        case bbishop:
        case brook:
        case bknight:
        case bking:
        case bqueen:
            return (color(SELF) == BLACK ? SELF : RIVAL);
    }
}

int func_piece(int cell) {
    return cell;
}

bool func_castled(int player) {
    return color(player) == WHITE ? white_castled : black_castled;
}

extern bool func_check(int player) {
    if (color(player) == WHITE) return is_attacked(wking_loc, BLACK);
    return is_attacked(bking_loc, WHITE);
}

int func_startingRow(int piece) { // wouldn't be better startingRank and/or conversion from rank to row and row to rank?
    
}

int func_toRank(int row) {
    if (color(SELF) == WHITE) return row;
    else return rev_rank[row];
}

vector<int> attackedBy(int square, int col) {

 /* this function will return a list with the squares of all the pieces
    of color 'color' that could attack an enemy piece in the square
    'square' */
 vector<int> res = vector<int>(0);

 int rook_o[4] = {12, -12, 1, -1};
 int bishop_o[4] = {11, -11, 13, -13};
 int knight_o[8] = {10, -10, 14, -14, 23, -23, 25, -25};
 int a_sq, i;

 /* white attacker: */
 if (col == WHITE) {
   /* rook-style moves: */
   for (i = 0; i < 4; i++) {
     a_sq = square + rook_o[i];
     /* the king can attack from one square away: */
     if (board[a_sq] == wking) res.push_back(a_sq);
     /* otherwise, check for sliding pieces: */
     while (board[a_sq] != frame) {
       if (board[a_sq] == wrook || board[a_sq] == wqueen) res.push_back(a_sq);
       if (board[a_sq] != npiece) res.push_back(a_sq);
       a_sq += rook_o [i];
     }
   }

   /* bishop-style moves: */
   for (i = 0; i < 4; i++) {
     a_sq = square + bishop_o[i];
     /* check for pawn attacks: */
     if (board[a_sq] == wpawn && i%2) res.push_back(a_sq);
     /* the king can attack from one square away: */
     if (board[a_sq] == wking) res.push_back(a_sq);
     while (board[a_sq] != frame) {
   if (board[a_sq] == wbishop || board[a_sq] == wqueen) res.push_back(a_sq);
   if (board[a_sq] != npiece) break;
   a_sq += bishop_o [i];
     }
   }

   /* knight-style moves: */
   for (i = 0; i < 8; i++) {
     a_sq = square + knight_o[i];
     if (board[a_sq] == wknight) res.push_back(a_sq);
   }

   return res;

 }

 /* black attacker: */
 else {
   /* rook-style moves: */
   for (i = 0; i < 4; i++) {
     a_sq = square + rook_o[i];
     /* the king can attack from one square away: */
     if (board[a_sq] == bking) res.push_back(a_sq);
     /* otherwise, check for sliding pieces: */
     while (board[a_sq] != frame) {
   if (board[a_sq] == brook || board[a_sq] == bqueen) res.push_back(a_sq);
   if (board[a_sq] != npiece) break;
   a_sq += rook_o [i];
     }
   }

   /* bishop-style moves: */
   for (i = 0; i < 4; i++) {
     a_sq = square + bishop_o[i];
     /* check for pawn attacks: */
     if (board[a_sq] == bpawn && !(i%2)) res.push_back(a_sq);
     /* the king can attack from one square away: */
     if (board[a_sq] == bking) res.push_back(a_sq);
     while (board[a_sq] != frame) {
       if (board[a_sq] == bbishop || board[a_sq] == bqueen) res.push_back(a_sq);
       if (board[a_sq] != npiece) break;
       a_sq += bishop_o [i];
     }
   }

   /* knight-style moves: */
   for (i = 0; i < 8; i++) {
     a_sq = square + knight_o[i];
     if (board[a_sq] == bknight) res.push_back(a_sq);
   }

   return res;

 }
}


std::vector<int> func_coveredBy(int piece) {
 int col = pieceColor(piece);
 return attackedBy(piece, col);
}

std::vector<int> func_attackedBy(int piece) {
 int col = pieceColor(piece);
 col = (col+1)%2;
 int square = piece;
 return attackedBy(piece, col);
}

std::vector<int> func_coveredCells(int piece) {

}

std::vector<int> func_visibleCells(int piece) {

}

int func_toRow(int rank) {
    if (color(SELF) == WHITE) return rank;
    else return rev_rank[rank];
}

vector<int> cells() {
    vector<int> result = vector<int>();
    for (int i = 2; i < 10; ++i) 
        for (int j = 2; j < 10; ++j) 
            result.push_back(i*12 + j);
    return result;
}

vector<int> rows() {
    vector<int> result = vector<int>();
    for (int i = 1; i <= 8; ++i) result.push_back(i);
    return result;
}

vector<int> ranks() {
    vector<int> result = vector<int>();
    for (int i = 1; i <= 8; ++i) result.push_back(i);
    return result;
}

vector<int> files() {
    vector<int> result = vector<int>();
    for (int i = 1; i <= 8; ++i) result.push_back(i);
    return result;
}

int get_file(char file) {
    return file - 'a' + 1;
}

int get_row(int row) {
    return row;
}

int get_rank(int rank) {
    return rank;
}

int get_cell(string cell) {
    return (cell[0] - 'a' + 2) + (cell[1] - '1' + 2)*12;
}

vector<int> get_rang_cell(string cell0, string cell1) {
    int c0 = get_cell(cell0);
    int c1 = get_cell(cell1);
    int r0 = rank (c0);
    int r1 = rank (c1);
    int f0 = file (c0);
    int f1 = file (c1);
    vector<int> result = vector<int>();
    if (c0 <= c1) { // c0 closer to top-left border
        if (r0 == r1) // both in same row
            for (int f = f0; f <= f1; ++f) result.push_back(to_cell(r0, f));
        else {
            // first row 
            for (int f = f0; f <= 8; ++f) result.push_back(to_cell(r0, f));
            // rows between r0 and r1
            for (int r = r0+1; r < r1; ++r)
                for (int f = 1; f <= 8; ++f) result.push_back(to_cell(r,f));
            // last row
            for (int f = 1; f <= f1; ++f) result.push_back(to_cell(r1,f));
        }
    }
    else {
        if (r0 == r1) // both in same row
            for (int f = f0; f >= f1; --f) result.push_back(to_cell(r0, f));
        else {
            // first row 
            for (int f = f0; f >= 1; --f) result.push_back(to_cell(r0, f));
            // rows between r0 and r1
            for (int r = r0-1; r > r1; --r)
                for (int f = 8; f >= 1; --f) result.push_back(to_cell(r,f));
            // last row
            for (int f = 8; f >= f1; --f) result.push_back(to_cell(r1,f));
        }
    }
    return result;
}

vector<int> get_rang_row(int row1, int row2) {
    vector<int> result = vector<int>();
    if (row1 <= row2)
        for (int i = row1; i <= row2; ++i) result.push_back(i);
    else 
        for (int i = row1; i >= row2; --i) result.push_back(i);
    return result;
}

vector<int> get_rang_file(int file1, int file2) {
    vector<int> result = vector<int>();
    if (file1 <= file2)
        for (int i = file1; i <= file2; ++i) result.push_back(i);
    else
        for (int i = file1; i >= file2; --i) result.push_back(i);
    return result;
}

vector<int> get_rang_rank(int rank1, int rank2) {
    vector<int> result = vector<int>();
    if (rank1 <= rank2)
        for (int i = rank1; i <= rank2; ++i) result.push_back(i);
    else
        for (int i = rank1; i >= rank2; --i) result.push_back(i);
    return result;
}

bool func_inCenter(int cell) {
    return cell == 65 || cell == 66 || cell == 77 || cell == 78;
}

bool func_inExpandedCenter(int cell) {
    return (cell >= 52 && cell <= 55) ||
            (cell >= 64 && cell <= 67) ||
            (cell >= 76 && cell <= 79) ||
            (cell >= 88 && cell <= 91);
}

int func_startingRank(int piece) {
    switch (board[piece]) {
        case wpawn:
            return (color(SELF) == WHITE ? 2 : 7);
        case wbishop:
        case wrook:
        case wknight:
        case wking:
        case wqueen:
            return (color(SELF) == WHITE ? 1 : 8);
        case bpawn:
            return (color(SELF) == WHITE ? 7 : 2);
        case bbishop:
        case brook:
        case bknight:
        case bking:
        case bqueen:
            return (color(SELF) == WHITE ? 8 : 1);
    }    
}