#ifndef _EXTERNAL_FUNC_H
#define _EXTERNAL_FUNC_H

#include <vector>
#include <string>
using namespace std;

// Predefined functions
extern int func_value (int piece);
extern int func_row (int piece);
extern int func_file (int piece);
extern int func_player (int piece);
extern int func_piece (int cell);
extern bool func_castled (int player);
extern int func_startingRow(int piece);
extern vector<int> func_coveredBy(int piece);
extern vector<int> func_attackedBy(int piece);
extern bool func_check(int piece);
extern vector<int> func_visibleCells(int piece);
extern int func_cell(int piece);
extern vector<int> func_coveredCells(int piece);
extern int func_rank (int piece);
template<typename T>
extern int func_len(vector<T> v);

// Auxiliar functions
extern int get_file(int file);
extern int get_row(int row);
extern int get_rank(int rank);
extern int get_cell(string cell);
extern vector<int> get_rang_cell(string cell1, string cell2);
extern vector<int> get_rang_row(int row1, int row2);
extern vector<int> get_rang_file(int file1, int file2);
extern vector<int> get_rang_rank(int rank1, int rank2);
template<typename T>
extern bool in_expr(T elem, vector<T> list);
template<typename T>
extern bool array_equality(vector<T> v0, vector<T> v1);
template<typename T> 
extern T access_array(vector<T> arr, int pos);
template<typename T>
extern vector<T> concat(vector<T> v0, vector<T> v1);
extern void invert_players();
extern int self();
extern int rival();
extern vector<int> cells();
extern vector<int> rows();
extern vector<int> files();
extern vector<int> ranks();
extern vector<int> rpieces();
extern vector<int> rpawns();
extern vector<int> rbishops();
extern vector<int> rrooks();
extern vector<int> rknights();
extern vector<int> rkings();
extern vector<int> rqueens();
extern vector<int> spieces();
extern vector<int> spawns();
extern vector<int> sbishops();
extern vector<int> srooks();
extern vector<int> sknights();
extern vector<int> skings();
extern vector<int> squeens();
extern vector<int> patatesidracs();

#endif
