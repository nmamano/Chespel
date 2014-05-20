/**************************************************
 * File: eval.c                                   *
 * Purpose: functions for evaluating positions    *
 **************************************************/

#include "faile.h"
#include "extvars.h"
#include "protos.h"
#include "generated_eval.c"

/* Piece square tables */
/* these tables will be used for positional bonuses: */

int bishop[144] = {
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
0, 0,-5,-5,-5,-5,-5,-5,-5,-5, 0, 0,
0, 0,-5,10, 5,10,10, 5,10,-5, 0, 0,
0, 0,-5, 5, 3,12,12, 3, 5,-5, 0, 0,
0, 0,-5, 3,12, 3, 3,12, 3,-5, 0, 0,
0, 0,-5, 3,12, 3, 3,12, 3,-5, 0, 0,
0, 0,-5, 5, 3,12,12, 3, 5,-5, 0, 0,
0, 0,-5,10, 5,10,10, 5,10,-5, 0, 0,
0, 0,-5,-5,-5,-5,-5,-5,-5,-5, 0, 0,
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

int knight[144] = {
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0,0,-10,-5,-5,-5,-5,-5,-5,-10, 0,0, 
0, 0,-5, 0, 0, 3, 3, 0, 0,-5, 0, 0, 
0, 0,-5, 0, 5, 5, 5, 5, 0,-5, 0, 0, 
0, 0,-5, 0, 5,10,10, 5, 0,-5, 0, 0, 
0, 0,-5, 0, 5,10,10, 5, 0,-5, 0, 0, 
0, 0,-5, 0, 5, 5, 5, 5, 0,-5, 0, 0, 
0, 0,-5, 0, 0, 3, 3, 0, 0,-5, 0, 0, 
0,0,-10,-5,-5,-5,-5,-5,-5,-10, 0,0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

long int white_pawn[144] = {
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
0, 0, 0, 0, 0,-5,-5, 0, 0, 0, 0, 0,
0, 0, 1, 2, 3, 4, 4, 3, 2, 1, 0, 0,
0, 0, 2, 4, 6, 8, 8, 6, 4, 2, 0, 0,
0, 0, 3, 6, 9,12,12, 9, 6, 3, 0, 0,
0, 0, 4, 8,12,16,16,12, 8, 4, 0, 0,
0, 0, 5,10,15,20,20,15,10, 5, 0, 0,
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

int black_pawn[144] = {
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,5,10,15,20,20,15,10,5,0,0,
0,0,4,8,12,16,16,12,8,4,0,0,
0,0,3,6,9,12,12,9,6,3,0,0,
0,0,2,4,6,8,8,6,4,2,0,0,
0,0,1,2,3,4,4,3,2,1,0,0,
0,0,0,0,0,-5,-5,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0};

/* to be used during opening and middlegame for white king positioning: */
int white_king[144] = {
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,2,10,4,0,0,7,10,2,0,0,
0,0,-3,-3,-5,-5,-5,-5,-3,-3,0,0,
0,0,-5,-5,-8,-8,-8,-8,-5,-5,0,0,
0,0,-8,-8,-13,-13,-13,-13,-8,-8,0,0,
0,0,-13,-13,-21,-21,-21,-21,-13,-13,0,0,
0,0,-21,-21,-34,-34,-34,-34,-21,-21,0,0,
0,0,-34,-34,-55,-55,-55,-55,-34,-34,0,0,
0,0,-55,-55,-89,-89,-89,-89,-55,-55,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0};

/* to be used during opening and middlegame for black king positioning: */
int black_king[144] = {
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,-55,-55,-89,-89,-89,-89,-55,-55,0,0,
0,0,-34,-34,-55,-55,-55,-55,-34,-34,0,0,
0,0,-21,-21,-34,-34,-34,-34,-21,-21,0,0,
0,0,-13,-13,-21,-21,-21,-21,-13,-13,0,0,
0,0,-8,-8,-13,-13,-13,-13,-8,-8,0,0,
0,0,-5,-5,-8,-8,-8,-8,-5,-5,0,0,
0,0,-3,-3,-5,-5,-5,-5,-3,-3,0,0,
0,0,2,10,4,0,0,7,10,2,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0};

/* to be used for positioning of both kings during the endgame: */
int end_king[144] = {
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,-5,-3,-1,0,0,-1,-3,-5,0,0,
0,0,-3,5,5,5,5,5,5,-3,0,0,
0,0,-1,5,10,10,10,10,5,-1,0,0,
0,0,0,5,10,15,15,10,5,0,0,0,
0,0,0,5,10,15,15,10,5,0,0,0,
0,0,-1,5,10,10,10,10,5,-1,0,0,
0,0,-3,5,5,5,5,5,5,-3,0,0,
0,0,-5,-3,-1,0,0,-1,-3,-5,0,0,
0,0,0,0,0,0,0,0,0,0,0,0,
0,0,0,0,0,0,0,0,0,0,0,0};

long int eval (void) {
  long int score = 0;
  /* select the appropriate eval() routine: */
  if (piece_count > 11) {
    score = score + opn_eval_tables();
    score = score + opn_eval();
    return score;
  }
  else if (piece_count < 5) {
    score = score + end_eval_tables();
    score = score + end_eval();
    return score;
  }
  else {
    score = score + mid_eval_tables();
    score = score + mid_eval();
    return score;
  }
}

long int opn_eval_tables (void) {
  int i, j;
  long int score = 0;
  for (j = 1; j <= num_pieces; j++) {
    i = pieces[j];
    if (!i)
      continue;
    switch (board[i]) {
      case (wpawn):
	score += white_pawn[i];
	break;

      case (bpawn):
	score -= black_pawn[i];
	break;

      case (wrook):
	if (rank == 7)
	  score += 8;
	break;

      case (brook):
	if (rank == 2)
	  score -= 8;
	break;

      case (wbishop):
	score += bishop[i];
	break;

      case (bbishop):
	score -= bishop[i];
	break;

      case (wknight):
	score += knight[i];
	break;

      case (bknight):
	score -= knight[i];
	break;

      case (wqueen):
	if (i != 29)
	  if (!moved[28] || !moved[27] || !moved[31] || !moved[32])
	    score -= 4;
	break;

      case (bqueen):
	if (i != 113)
	  if (!moved[112] || !moved[111] || !moved[115] || !moved[116])
	    score += 4;
	break;

      case (wking):
	score += white_king[i];
	break;

      case (bking):
	score -= black_king[i];
	break;
    }
  }
  /* adjust for color: */
  if (white_to_move == 1) {
    return score;
  }
  else {
    return -score;
  }
}

long int mid_eval_tables (void) {
  int j, i;
  for (j = 1; j <= num_pieces; j++) {
    i = pieces[j];
    if (!i)
      continue;
    switch (board[i]) {
      case (wpawn):
	score += white_pawn[i];
	break;

      case (bpawn):
	score -= black_pawn[i];
	break;

      case (wrook):
	/* bonus for being on the 7th: */
	if (rank == 7)
	  score += 8;
	break;

      case (brook):
	/* bonus for being on the 7th: */
	if (rank == 2)
	  score -= 8;
	break;

      case (wbishop):
	score += bishop[i];
	break;

      case (bbishop):
	score -= bishop[i];
	break;

      case (wknight):
	score += knight[i];
	break;

      case (bknight):
	score -= knight[i];
	break;

      case (wking):
	score += white_king[i];
	break;

      case (bking):
	score -= black_king[i];
	break;
    }
  }

}

long int end_eval_tables (void) {
  int i, j;
  for (j = 1; j <= num_pieces; j++) {
    i = pieces[j];
    if (!i)
      continue;
    switch (board[i]) {
      case (wpawn):
	score += white_pawn[i];
	break;

      case (bpawn):
	score -= black_pawn[i];
	break;

      case (wrook):
	/* bonus for being on the 7th (a bit bigger bonus in the endgame, b/c
	   a rook on the 7th can be a killer in the endgame): */
	if (rank == 7)
	  score += 12;
	break;

      case (brook):
	/* bonus for being on the 7th (a bit bigger bonus in the endgame, b/c
	   a rook on the 7th can be a killer in the endgame): */
	if (rank == 2)
	  score -= 12;
	break;

      case (wbishop):
	score += bishop[i];
	break;

      case (bbishop):
	score -= bishop[i];
	break;

      case (wknight):
	score += knight[i];
	break;

      case (bknight):
	score -= knight[i];
	break;

      case (wking):
	/* the king is safe to come out in the endgame, so we don't check for
	   king safety anymore, and encourage centralization of the king */
	score += end_king[i];
	break;

      case (bking):
	/* the king is safe to come out in the endgame, so we don't check for
	   king safety anymore, and encourage centralization of the king */
	score -= end_king[i];
	break;
    }
  }

}