//source: http://www.daniweb.com/software-development/c/threads/377149/how-could-i-implement-a-vector-like-container-by-pure-c

#ifndef GUARD_GENERIC_VECTOR_H
#define GUARD_GENERIC_VECTOR_H
#include <stddef.h>
/**
 * @brief Returns the value of an item at the specified index
 * @remarks This is a convenience wrapper around vector_at() for the average use case
 */
#define VECTOR_AT(T,v,i) *((T*)vector_at((v), (i)))
/**
 * @brief Inserts a value of type T into a vector at the specified index
 * @remarks This is a convenience wrapper to support rvalues.
 *          Note that VECTOR_INSERT() cannot be used as an expression
 */
#define VECTOR_INSERT(T, v, x, i) do {        \
    T __anon_var19781111 = x;                 \
    vector_insert(v, &__anon_var19781111, i); \
} while (0)
/**
 * @brief A structure representing the vector object
 */
typedef struct generic_vector {
    void   *base;      /**< Raw memory for items */
    size_t  size;      /**< The number of inserted items */
    size_t  capacity;  /**< The number of potential items before a resize */
    size_t  item_size; /**< The number of bytes occupied by an item */
} generic_vector;

extern generic_vector *vector_create(size_t item_size, size_t capacity);
extern generic_vector *vector_clone(generic_vector *v);
extern void    vector_clear(generic_vector *v);
extern int     vector_resize(generic_vector *v, size_t capacity);
extern size_t  vector_size(generic_vector *v);
extern void   *vector_at(generic_vector *v, size_t index);
extern int     vector_insert(generic_vector *v, void *item, size_t index);
extern int     vector_remove(generic_vector *v, size_t index);
extern int     vector_remove_if(generic_vector *v, int (*pred)(void *item));
extern size_t  vector_find(generic_vector *v, void *value);
extern size_t  vector_find_if(generic_vector *v, int (*pred)(void *item));
#endif