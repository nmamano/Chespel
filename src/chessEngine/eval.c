/**************************************************
 * File: eval.c                                   *
 * Purpose: functions for evaluating positions    *
 **************************************************/

#include "faile.h"
#include "extvars.h"
#include "protos.h"

/* utility array to reverse rank: */
int rev_rank[9] = {
0,8,7,6,5,4,3,2,1};


long int eval(void) {
	int i, j;
	long int score = 0;

	if (eval_debug) {
		printf("eval_debug activated\n");
	}

	for (j = 1; j <= num_pieces; j++) {
	    i = pieces[j];
	    if (!i) {
	      continue;	    	
	    }
	    switch (board[i]) {
	      case (wpawn):
		score += 100;
		break;

	      case (bpawn):
		score -= 100;
		break;

	      case (wrook):
		score += 500;
		break;

	      case (brook):
		score -= 500;
		break;

	      case (wbishop):
		score += 325;
		break;

	      case (bbishop):
		score -= 325;
		break;

	      case (wknight):
		score += 310;
		break;

	      case (bknight):
		score -= 310;
		break;

	      case (wqueen):
		score += 900;
		break;

	      case (bqueen):
		score -= 900;
		break;

	      case (wking):
		break;

	      case (bking):
		break;
		}
	}
	if (white_to_move == 1) {
    	return score;
  	}
  	else {
    	return -score;
 	}
}