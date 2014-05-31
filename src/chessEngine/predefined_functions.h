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
extern int func_toRank(int row); // to be added
extern int func_toRow(int rank); // to be added
extern std::vector<int> func_coveredBy(int piece);
extern std::vector<int> func_attackedBy(int piece);
extern std::vector<int> func_coveredCells(int piece);
extern std::vector<int> func_visibleCells(int piece);

// Auxiliar functions
extern void reset(); // done?
extern int get_file(char file); // done
extern int get_row(int row); // done
extern int get_rank(int rank); // done
extern int get_cell(std::string cell); // done
extern std::vector<int> get_rang_cell(std::string cell1, std::string cell2); // done
extern std::vector<int> get_rang_row(int row1, int row2); // done
extern std::vector<int> get_rang_file(int file1, int file2); // done
extern std::vector<int> get_rang_rank(int rank1, int rank2); // done
extern void invert_players(); // done
extern int self(); // done
extern int rival(); // done
extern std::vector<int> cells(); // done
extern std::vector<int> rows(); // done
extern std::vector<int> files(); // done
extern std::vector<int> ranks(); // done
extern std::vector<int> get_pieces(int player, int type); // done
extern int incr_operation(int object, int incr, std::string type);
extern int arith_operation(int o0, int o1, char op, std::string type);

// Templated functions (implementation at predefined_functions.tcc)

template<typename T>
extern std::string string_concat(std::string s0, T s1, bool string_first, std::string type); 

extern std::string string_concat(std::string s0, std::string s1, bool string_first, std::string type);

extern std::string to_string(int x, std::string type);
extern std::string to_string(std::string s, std::string type);
extern std::string to_string(bool x, std::string type);

template<typename T>
extern std::string to_string(std::vector<T> v, std::string type);

template<typename T>
extern inline int func_len(const std::vector<T> &v);
template<typename T>
extern bool in_expr(T elem, std::vector<T> list);
template<typename T>
extern inline bool array_equality(std::vector<T> v0, std::vector<T> v1);
template<typename T> 
extern inline T access_array(std::vector<T> arr, int pos);
template<typename T>
extern std::vector<T> concat(const std::vector<T> &v0, const std::vector<T> &v1);

#endif
