#ifndef _PREDEFINED_FUNC_H
#define _PREDEFINED_FUNC_H

#include <vector>
#include <string>
#include <stdexcept>
#include "faile.h"
#include "extvars.h"
#include "protos.h"

// Predefined functions
extern int func_value (int piece);
extern int func_cell(int piece);
extern int func_rank (int piece);
extern int func_row (int piece);
extern int func_file (int piece);
extern int func_player (int piece);
extern int func_piece (int cell);
extern bool func_castled (int player);
extern int func_startingRow(int piece);
extern bool func_check(int player);
extern int func_toRank(int row);
extern int func_toRow(int rank);
extern std::vector<int> func_coveredBy(int piece);
extern std::vector<int> func_attackedBy(int piece);
extern std::vector<int> func_coveredCells(int piece);
extern std::vector<int> func_visibleCells(int piece);

// Auxiliar functions
extern void reset();
extern int get_file(char file);
extern int get_row(int row); 
extern int get_rank(int rank); 
extern int get_cell(std::string cell); 
extern std::vector<int> get_rang_cell(std::string cell1, std::string cell2); 
extern std::vector<int> get_rang_row(int row1, int row2); 
extern std::vector<int> get_rang_file(int file1, int file2); 
extern std::vector<int> get_rang_rank(int rank1, int rank2); 
extern void invert_players(); 
extern int self(); 
extern int rival(); 
extern std::vector<int> cells(); 
extern std::vector<int> rows(); 
extern std::vector<int> files(); 
extern std::vector<int> ranks(); 
extern std::vector<int> get_pieces(int player, int type); 
extern std::string string_concat(std::string s0, std::string s1, bool string_first, std::string type);
extern std::string to_string(int x, std::string type);
extern std::string to_string(std::string s, std::string type);
extern std::string to_string(bool x, std::string type);
extern int incr_operation(int object, int incr, std::string type);
extern int arith_operation(int o0, int o1, char op, std::string type);
extern bool eq_rankrow (int rank, int row);

// Templated functions (implementation at predefined_functions.tcc)
// Predefined
template<typename T>
extern inline int func_len(const std::vector<T> &v);

// Auxiliar
template<typename T>
extern std::string string_concat(std::string s0, T s1, bool string_first, std::string type); 
template<typename T>
extern std::string to_string(std::vector<T> v, std::string type);
template<typename T>
extern bool in_expr(T elem, std::vector<T> list);
template<typename T>
extern inline bool array_equality(std::vector<T> v0, std::vector<T> v1);
template<typename T> 
extern inline T access_array(std::vector<T> arr, int pos);
template<typename T>
extern std::vector<T> concat(const std::vector<T> &v0, const std::vector<T> &v1);

#endif
