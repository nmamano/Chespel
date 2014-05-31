#ifndef _PREDEFINED_FUNCTIONS_TCC
#define _PREDEFINED_FUNCTIONS_TCC

#include "predefined_functions.h"
#include <cstdlib>
using namespace std;

template<typename T>
int inline func_len(const std::vector<T> &v) { return v.size() * 1000; }

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
vector<T> concat(const vector<T> & v0, const vector<T> & v1) {
    vector<T> result = vector<T>(v0);
    for (int i = 0; i < v1.size(); ++i) {
        result.push_back(v1[i]);
    }
    return result;
}

template<typename T>
string string_concat(string s0, T s1, bool string_first, string type) {
    if (string_first) return s0 + to_string(s1,type);
    return to_string(s1,type) + s0;
}

template<typename T>
string to_string (vector<T> v, string type) {
    string new_type = type.substr(1, type.size()-2 );
    string result = "[";
    typename vector<T>::iterator it = v.begin();
    if (it != v.end()) // not empty
        while (true) {
            T value = *it;
            result += to_string(value, new_type);
            ++it;
            if (it == v.end()) break;
            result += ", ";
        }
    return result + "]";
}

#endif
