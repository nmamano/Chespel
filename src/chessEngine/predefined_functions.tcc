#ifndef _PREDEFINED_FUNCTIONS_TCC
#define _PREDEFINED_FUNCTIONS_TCC

#include "predefined_functions.h"
using namespace std;

template<typename T>
int inline func_len(const std::vector<T> &v) { return v.size(); }

template<typename T>
bool in_expr(T elem, vector<T> list) {
    for (int i = 0; i < list.size(); ++i) { if (elem == list[i]) return true; }
    return false;
}

template<typename T>
inline bool array_equality(vector<T> v0, vector<T> v1) { return v0 == v1; }

template<typename T>
inline T access_array(vector<T> arr, int pos) { return arr[pos/1000]; }

template<typename T>
vector<T> concat(vector<T> v0, vector<T> v1) {
    for (int i = 0; i < v1.size(); ++i) {
        v0.push_back(v1[i]);
    }
    return v0;
}

#endif
