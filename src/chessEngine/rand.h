/**************************************************
 * File: rand.h                                   *
 * Purpose: provide function and macro defn's     *
 * for rand.c                                     *
 **************************************************/

#ifndef RAND_H
#define RAND_H

#include <stdio.h>
#include <stdlib.h>
#include <math.h>

#define LIST_BUFF 25000

void init_simple_rand (unsigned long int);
unsigned long int rand_32 (void);
int random_255 (void);
void rand_test (long int max);
unsigned long int simple_rand (void);

#endif
