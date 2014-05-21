#include "string.h"
#include "faile.h"
#include "extvars.h"
#include "protos.h"

//extern const int centipawn_value = 1;

// headers
long int rule1(void);
long int rule2(void);
long int rule3(void);

// code
int func1() {
    return 1;
}

extern long int opn_eval(void) {
   long int score = 0;
   score += rule1();
   return score;
}

extern long int mid_eval(void) {
    long int score = 0;
    score += rule2();
    return score;
}

extern long int end_eval(void) {
    long int score = 0;
    score += rule3();
    return score;
}

// Extres utilitzats inicialment per les funcions d'avaluació anteriors (segurament fora de taules, falta revisar).
long int white_pawn_extra[144] = {
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

int black_pawn_extra[144] = {
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

/* utility array to reverse rank: */
int rev_rank[9] = {
0,8,7,6,5,4,3,2,1};

long int rule1(void) {
/* return a score for the current opening position: */

  int i, pawn_file, pawns[2][11], white_back_pawn[11], black_back_pawn[11],
    rank, wking_pawn_file, bking_pawn_file, j;
  long int score = 0;
  bool isolated, backwards;

  /* initialize the pawns array, (files offset by one to use dummy files in
     order to easier determine isolated status) and also initialize the
     arrays keeping track of the rank of the most backward pawn: */
  memset (pawns, 0, sizeof (pawns));
  for (i = 0; i < 11; i++) {
    white_back_pawn[i] = 7;
    black_back_pawn[i] = 2;
  }
  for (j = 1; j <= num_pieces; j++) {
    i = pieces[j];
    if (!i)
      continue;
    pawn_file = file (i)+1;
    rank = rank (i);
    if (board[i] == wpawn) {
      pawns[1][pawn_file]++;
      if (rank < white_back_pawn[pawn_file]) {
	white_back_pawn[pawn_file] = rank;
      }
    }
    else if (board[i] == bpawn) {
      pawns[0][pawn_file]++;
      if (rank > black_back_pawn[pawn_file]) {
	black_back_pawn[pawn_file] = rank;
      }
    }
  }

  /* loop through the board, adding material value, as well as positional
     bonuses for all pieces encountered: */
  for (j = 1; j <= num_pieces; j++) {
    i = pieces[j];
    if (!i)
      continue;
    pawn_file = file (i)+1;
    rank = rank (i);
    switch (board[i]) {
      case (wpawn):
	isolated = FALSE;
	backwards = FALSE;
	score += 100;

	/* penalties / bonuses will be in general smaller in the opening,
	   in order to put an emphasis on piece development */

	/* check for backwards pawns: */
	if (white_back_pawn[pawn_file+1] > rank
	    && white_back_pawn[pawn_file-1] > rank) {
	  /* no penalty in the opening for having a backwards pawn that hasn't
	     moved yet! */
	  if (rank != 2)
	    score -= 3;
	  backwards = TRUE;
	  /* check to see if it is furthermore isolated: */
	  if (!pawns[1][pawn_file+1] && !pawns[1][pawn_file-1]) {
	    score -= 2;
	    isolated = TRUE;
	  }
	}

	/* give weak, exposed pawns a penalty: */
	if (!pawns[0][pawn_file]) {
	  if (backwards) score -= 3;
	  if (isolated) score -= 5;
	}

	/* give doubled, trippled, etc.. pawns a penalty: */
	if (pawns[1][pawn_file] > 1)
	  score -= 2*(pawns[1][pawn_file]-1);

	/* give bonuses for passed pawns: */
	if (!pawns[0][pawn_file] && rank >= black_back_pawn[pawn_file-1] &&
	    rank >= black_back_pawn[pawn_file+1]) {
	  score += white_pawn_extra[i];
	  /* give an extra bonus if a connected, passed pawn: */
	  if (!isolated)
	    score += 10;
	}

	break;

      case (bpawn):
	isolated = FALSE;
	backwards = FALSE;
	score -= 100;

	/* penalties / bonuses will be in general smaller in the opening,
	   in order to put an emphasis on piece development */

	/* check for backwards pawns: */
	if (black_back_pawn[pawn_file+1] < rank
	    && black_back_pawn[pawn_file-1] < rank) {
	  /* no penalty in the opening for having a backwards pawn that hasn't
	     moved yet! */
	  if (rank != 2)
	    score += 3;
	  backwards = TRUE;
	  /* check to see if it is furthermore isolated: */
	  if (!pawns[0][pawn_file+1] && !pawns[0][pawn_file-1]) {
	    score += 2;
	    isolated = TRUE;
	  }
	}

	/* give weak, exposed pawns a penalty: */
	if (!pawns[1][pawn_file]) {
	  if (backwards) score += 3;
	  if (isolated) score += 5;
	}

	/* give doubled, trippled, etc.. pawns a penalty: */
	if (pawns[0][pawn_file] > 1)
	  score += 2*(pawns[0][pawn_file]-1);

	/* give bonuses for passed pawns: */
	if (!pawns[1][pawn_file] && rank <= white_back_pawn[pawn_file-1] &&
	    rank <= white_back_pawn[pawn_file+1]) {
	  score -= black_pawn_extra[i];
	  /* give an extra bonus if a connected, passed pawn: */
	  if (!isolated)
	    score -= 10;
	}

	break;

      case (wrook):
	score += 500;

	/* give bonuses depending on how open the rook's file is: */
	if (!pawns[1][pawn_file]) {
	  /* half open file */
	  score += 5;
	  if (!pawns[0][pawn_file]) {
	    /* open file */
	    score += 3;
	  }
	}

	break;

      case (brook):
	score -= 500;

	/* give bonuses depending on how open the rook's file is: */
	if (!pawns[0][pawn_file]) {
	  /* half open file */
	  score -= 5;
	  if (!pawns[1][pawn_file]) {
	    /* open file */
	    score -= 3;
	  }
	}

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
	/* encourage castling, and give a penalty for moving the king without
	   castling */
	if (white_castled)
	  score += 12;
	else if (moved[30]) {
	  score -= 4;
	  /* make the penalty bigger if the king is open, leaving the other
	     side a chance to gain tempo with files along the file, as well
	     as building an attack: */
	  if (!pawns[1][pawn_file])
	    score -= 6;
	}

	/* in general, in the opening, don't worry quite so much about pawn
	   cover, because sometimes it isn't good for the king to castle */

	/* if the king is behind some pawn cover, give penalties for the pawn
	   cover being far from the king, else give a penalty for the king
	   not having any pawn cover: */
	if (rank < white_back_pawn[pawn_file] && pawns[1][pawn_file])
	  score -= 5*(white_back_pawn[pawn_file]-rank-1);
	else
	  score -= 8;
	if (rank < white_back_pawn[pawn_file+1] && pawns[1][pawn_file+1])
	  score -= 4*(white_back_pawn[pawn_file+1]-rank-1);
	else
	  score -= 8;
	if (rank < white_back_pawn[pawn_file-1] && pawns[1][pawn_file-1])
	  score -= 4*(white_back_pawn[pawn_file-1]-rank-1);
	else
	  score -= 8;

	break;

      case (bking):
	/* encourage castling, and give a penalty for moving the king without
	   castling */
	if (black_castled)
	  score -= 12;
	else if (moved[114]) {
	  score += 4;
	  /* make the penalty bigger if the king is open, leaving the other
	     side a chance to gain tempo with files along the file, as well
	     as building an attack: */
	  if (!pawns[0][pawn_file])
	    score += 6;
	}

	/* in general, in the opening, don't worry quite so much about pawn
	   cover, because sometimes it isn't good for the king to castle */

	/* if the king is behind some pawn cover, give penalties for the pawn
	   cover being far from the king, else give a penalty for the king
	   not having any pawn cover: */
	if (rank > black_back_pawn[pawn_file] && pawns[0][pawn_file])
	  score += 5*(rank-black_back_pawn[pawn_file]-1);
	else
	  score += 8;
	if (rank > black_back_pawn[pawn_file+1] && pawns[0][pawn_file+1])
	  score += 4*(rank-black_back_pawn[pawn_file+1]-1);
	else
	  score += 8;
	if (rank > black_back_pawn[pawn_file-1] && pawns[0][pawn_file-1])
	  score += 4*(rank-black_back_pawn[pawn_file-1]-1);
	else
	  score += 8;

	break;
    }
  }

  /* give bigger penalties for blocking the e/d pawns in the opening, as
     we want to develop quickly: */
  if (!moved[41] && board[53] != npiece)
    score -= 7;
  if (!moved[42] && board[54] != npiece)
    score -= 7;
  if (!moved[101] && board[89] != npiece)
    score += 7;
  if (!moved[102] && board[90] != npiece)
    score += 7;

  /* to be used for pawn storm code: */
  wking_pawn_file = file (wking_loc)+1;
  bking_pawn_file = file (bking_loc)+1;

  /* if the kings are on opposite wings, or far apart, check for pawn
     storms, and open lines for heavy pieces (bonuses/penalties brought
     down a bit in the opening, as it isn't a good idea to start pawn
     storming when the position is still fluid): */
  if ((wking_pawn_file-bking_pawn_file) > 2 ||
      (bking_pawn_file-wking_pawn_file) > 2) {
    /* black pawn storms: */
    score -= rev_rank[black_back_pawn[wking_pawn_file]]-2;
    score -= rev_rank[black_back_pawn[wking_pawn_file+1]]-2;
    score -= rev_rank[black_back_pawn[wking_pawn_file-1]]-2;

    /* white pawn storms: */
    score += white_back_pawn[bking_pawn_file]-2;
    score += white_back_pawn[bking_pawn_file+1]-2;
    score += white_back_pawn[bking_pawn_file-1]-2;

    /* black opening up lines: */
    if (!pawns[0][wking_pawn_file])
      score -= 6;
    if (!pawns[0][wking_pawn_file+1])
      score -= 4;
    if (!pawns[0][wking_pawn_file-1])
      score -= 4;

    /* white opening up lines: */
    if (!pawns[1][bking_pawn_file])
      score += 6;
    if (!pawns[1][bking_pawn_file+1])
      score += 4;
    if (!pawns[1][bking_pawn_file-1])
      score += 4;

  }

  /* don't adjust for trading pieces when ahead quite yet .. leave that until
     the middlegame and endgame */

  /* adjust for color: */
  if (white_to_move == 1) {
    return score;
  }
  else {
    return -score;
  }
}

long int rule2 (void) {
  
  /* return a score for the current middlegame position: */

  int i, pawn_file, pawns[2][11], white_back_pawn[11], black_back_pawn[11],
    rank, wking_pawn_file, bking_pawn_file, j;
  long int score = 0;
  bool isolated, backwards;

  /* initialize the pawns array, (files offset by one to use dummy files in
     order to easier determine isolated status) and also initialize the
     arrays keeping track of the rank of the most backward pawn: */
  memset (pawns, 0, sizeof (pawns));
  for (i = 0; i < 11; i++) {
    white_back_pawn[i] = 7;
    black_back_pawn[i] = 2;
  }
  for (j = 1; j <= num_pieces; j++) {
    i = pieces[j];
    if (!i)
      continue;
    pawn_file = file (i)+1;
    rank = rank (i);
    if (board[i] == wpawn) {
      pawns[1][pawn_file]++;
      if (rank < white_back_pawn[pawn_file]) {
	white_back_pawn[pawn_file] = rank;
      }
    }
    else if (board[i] == bpawn) {
      pawns[0][pawn_file]++;
      if (rank > black_back_pawn[pawn_file]) {
	black_back_pawn[pawn_file] = rank;
      }
    }
  }

  /* loop through the board, adding material value, as well as positional
     bonuses for all pieces encountered: */
  for (j = 1; j <= num_pieces; j++) {
    i = pieces[j];
    if (!i)
      continue;
    pawn_file = file (i)+1;
    rank = rank (i);
    switch (board[i]) {
      case (wpawn):
	isolated = FALSE;
	backwards = FALSE;
	score += 100;

	/* check for backwards pawns: */
	if (white_back_pawn[pawn_file+1] > rank
	    && white_back_pawn[pawn_file-1] > rank) {
	  score -= 5;
	  backwards = TRUE;
	  /* check to see if it is furthermore isolated: */
	  if (!pawns[1][pawn_file+1] && !pawns[1][pawn_file-1]) {
	    score -= 3;
	    isolated = TRUE;
	  }
	}

	/* give weak, exposed pawns a penalty: */
	if (!pawns[0][pawn_file]) {
	  if (backwards) score -= 4;
	  if (isolated) score -= 8;
	}

	/* give doubled, trippled, etc.. pawns a penalty: */
	if (pawns[1][pawn_file] > 1)
	  score -= 2*(pawns[1][pawn_file]-1);

	/* give bonuses for passed pawns: */
	if (!pawns[0][pawn_file] && rank >= black_back_pawn[pawn_file-1] &&
	    rank >= black_back_pawn[pawn_file+1]) {
	  score += 2*white_pawn_extra[i];
	  /* give an extra bonus if a connected, passed pawn: */
	  if (!isolated)
	    score += 15;
	}

	break;

      case (bpawn):
	isolated = FALSE;
	backwards = FALSE;
	score -= 100;

	/* check for backwards pawns: */
	if (black_back_pawn[pawn_file+1] < rank
	    && black_back_pawn[pawn_file-1] < rank) {
	  score += 5;
	  backwards = TRUE;
	  /* check to see if it is furthermore isolated: */
	  if (!pawns[0][pawn_file+1] && !pawns[0][pawn_file-1]) {
	    score += 3;
	    isolated = TRUE;
	  }
	}

	/* give weak, exposed pawns a penalty: */
	if (!pawns[1][pawn_file]) {
	  if (backwards) score += 4;
	  if (isolated) score += 8;
	}

	/* give doubled, trippled, etc.. pawns a penalty: */
	if (pawns[0][pawn_file] > 1)
	  score += 2*(pawns[0][pawn_file]-1);

	/* give bonuses for passed pawns: */
	if (!pawns[1][pawn_file] && rank <= white_back_pawn[pawn_file-1] &&
	    rank <= white_back_pawn[pawn_file+1]) {
	  score -= 2*black_pawn_extra[i];
	  /* give an extra bonus if a connected, passed pawn: */
	  if (!isolated)
	    score -= 15;
	}

	break;

      case (wrook):
	score += 500;

	/* give bonuses depending on how open the rook's file is: */
	if (!pawns[1][pawn_file]) {
	  /* half open file */
	  score += 5;
	  if (!pawns[0][pawn_file]) {
	    /* open file */
	    score += 3;
	  }
	}

	break;

      case (brook):
	score -= 500;

	/* give bonuses depending on how open the rook's file is: */
	if (!pawns[0][pawn_file]) {
	  /* half open file */
	  score -= 5;
	  if (!pawns[1][pawn_file]) {
	    /* open file */
	    score -= 3;
	  }
	}

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

	/* encourage castling, and give a penalty for moving the king without
	   castling */
	if (white_castled)
	  score += 20;
	else if (moved[30]) {
	  score -= 7;
	  /* make the penalty bigger if the king is open, leaving the other
	     side a chance to gain tempo with files along the file, as well
	     as building an attack: */
	  if (!pawns[1][pawn_file])
	    score -= 8;
	}

	/* if the king is behind some pawn cover, give penalties for the pawn
	   cover being far from the king, else give a penalty for the king
	   not having any pawn cover: */
	if (rank < white_back_pawn[pawn_file] && pawns[1][pawn_file])
	  score -= 8*(white_back_pawn[pawn_file]-rank-1);
	else
	  score -= 12;
	if (rank < white_back_pawn[pawn_file+1] && pawns[1][pawn_file+1])
	  score -= 7*(white_back_pawn[pawn_file+1]-rank-1);
	else
	  score -= 12;
	if (rank < white_back_pawn[pawn_file-1] && pawns[1][pawn_file-1])
	  score -= 7*(white_back_pawn[pawn_file-1]-rank-1);
	else
	  score -= 12;	  

	break;

      case (bking):

	/* encourage castling, and give a penalty for moving the king without
	   castling */
	if (black_castled)
	  score -= 20;
	else if (moved[114]) {
	  score += 7;
	  /* make the penalty bigger if the king is open, leaving the other
	     side a chance to gain tempo with files along the file, as well
	     as building an attack: */
	  if (!pawns[0][pawn_file])
	    score += 8;
	}

	/* if the king is behind some pawn cover, give penalties for the pawn
	   cover being far from the king, else give a penalty for the king
	   not having any pawn cover: */
	if (rank > black_back_pawn[pawn_file] && pawns[0][pawn_file])
	  score += 8*(rank-black_back_pawn[pawn_file]-1);
	else
	  score += 12;
	if (rank > black_back_pawn[pawn_file+1] && pawns[0][pawn_file+1])
	  score += 7*(rank-black_back_pawn[pawn_file+1]-1);
	else
	  score += 12;
	if (rank > black_back_pawn[pawn_file-1] && pawns[0][pawn_file-1])
	  score += 7*(rank-black_back_pawn[pawn_file-1]-1);
	else
	  score += 12;

	break;
    }
  }

  /* give penalties for blocking the e/d pawns: */
  if (!moved[41] && board[53] != npiece)
    score -= 5;
  if (!moved[42] && board[54] != npiece)
    score -= 5;
  if (!moved[101] && board[89] != npiece)
    score += 5;
  if (!moved[102] && board[90] != npiece)
    score += 5;

  /* to be used for pawn storm code: */
  wking_pawn_file = file (wking_loc)+1;
  bking_pawn_file = file (bking_loc)+1;

  /* if the kings are on opposite wings, or far apart, check for pawn
     storms, and open lines for heavy pieces: */
  if ((wking_pawn_file-bking_pawn_file) > 2 ||
      (bking_pawn_file-wking_pawn_file) > 2) {
    /* black pawn storms: */
    score -= 3*(rev_rank[black_back_pawn[wking_pawn_file]]-2);
    score -= 3*(rev_rank[black_back_pawn[wking_pawn_file+1]]-2);
    score -= 3*(rev_rank[black_back_pawn[wking_pawn_file-1]]-2);

    /* white pawn storms: */
    score += 3*(white_back_pawn[bking_pawn_file]-2);
    score += 3*(white_back_pawn[bking_pawn_file+1]-2);
    score += 3*(white_back_pawn[bking_pawn_file-1]-2);

    /* black opening up lines: */
    if (!pawns[0][wking_pawn_file])
      score -= 8;
    if (!pawns[0][wking_pawn_file+1])
      score -= 6;
    if (!pawns[0][wking_pawn_file-1])
      score -= 6;

    /* white opening up lines: */
    if (!pawns[1][bking_pawn_file])
      score += 8;
    if (!pawns[1][bking_pawn_file+1])
      score += 6;
    if (!pawns[1][bking_pawn_file-1])
      score += 6;

  }

  /* encourage trading off material when one side has a material advantage
     (note that we don't need to worry about whether material score dropped or
     not, because search will naturally choose a variation where it keeps its
     material score over one with a small positional bonus): */
  if (score > 90 && start_piece_count < piece_count) {
    score += 10;
  }
  else if (score < 90 && start_piece_count < piece_count) {
    score -= 10;
  }

  /* adjust for color: */
  if (white_to_move == 1) {
    return score;
  }
  else {
    return -score;
  }
}  

long int rule3 (void) {

  /* return a score for the current endgame position: */

  int i, pawn_file, pawns[2][11], white_back_pawn[11], black_back_pawn[11],
    rank, j;
  long int score = 0;
  bool isolated, backwards;

  /* initialize the pawns array, (files offset by one to use dummy files in
     order to easier determine isolated status) and also initialize the
     arrays keeping track of the rank of the most backward pawn: */
  memset (pawns, 0, sizeof (pawns));
  for (i = 0; i < 11; i++) {
    white_back_pawn[i] = 7;
    black_back_pawn[i] = 2;
  }
  for (j = 1; j <= num_pieces; j++) {
    i = pieces[j];
    if (!i)
      continue;
    pawn_file = file (i)+1;
    rank = rank (i);
    if (board[i] == wpawn) {
      pawns[1][pawn_file]++;
      if (rank < white_back_pawn[pawn_file]) {
	white_back_pawn[pawn_file] = rank;
      }
    }
    else if (board[i] == bpawn) {
      pawns[0][pawn_file]++;
      if (rank > black_back_pawn[pawn_file]) {
	black_back_pawn[pawn_file] = rank;
      }
    }
  }

  /* loop through the board, adding material value, as well as positional
     bonuses for all pieces encountered: */
  for (j = 1; j <= num_pieces; j++) {
    i = pieces[j];
    if (!i)
      continue;
    pawn_file = file (i)+1;
    rank = rank (i);
    switch (board[i]) {
      case (wpawn):
	isolated = FALSE;
	backwards = FALSE;
	score += 100;

	/* in general, bonuses/penalties in the endgame evaluation will be
	   higher, since pawn structure becomes more important for the
	   creation of passed pawns */

	/* check for backwards pawns: */
	if (white_back_pawn[pawn_file+1] > rank
	    && white_back_pawn[pawn_file-1] > rank) {
	  score -= 8;
	  backwards = TRUE;
	  /* check to see if it is furthermore isolated: */
	  if (!pawns[1][pawn_file+1] && !pawns[1][pawn_file-1]) {
	    score -= 5;
	    isolated = TRUE;
	  }
	}

	/* give weak, exposed pawns a penalty (not as much as in the midgame,
	   since there may be no pieces to take advantage of it): */
	if (!pawns[0][pawn_file]) {
	  if (backwards) score -= 3;
	  if (isolated) score -= 5;
	}

	/* give doubled, trippled, etc.. pawns a penalty (bigger in the
	   endgame, since they will become big targets): */
	if (pawns[1][pawn_file] > 1)
	  score -= 3*(pawns[1][pawn_file]-1);

	/* give bonuses for passed pawns (bigger in the endgame since passed
	   pawns are what wins the endgame): */
	if (!pawns[0][pawn_file] && rank >= black_back_pawn[pawn_file-1] &&
	    rank >= black_back_pawn[pawn_file+1]) {
	  score += 3*white_pawn_extra[i];
	  /* give an extra bonus if a connected, passed pawn: */
	  if (!isolated)
	    score += 18;
	}

	break;

      case (bpawn):
	isolated = FALSE;
	backwards = FALSE;
	score -= 100;

	/* in general, bonuses/penalties in the endgame evaluation will be
	   higher, since pawn structure becomes more important for the
	   creation of passed pawns */

	/* check for backwards pawns: */
	if (black_back_pawn[pawn_file+1] < rank
	    && black_back_pawn[pawn_file-1] < rank) {
	  score += 8;
	  backwards = TRUE;
	  /* check to see if it is furthermore isolated: */
	  if (!pawns[0][pawn_file+1] && !pawns[0][pawn_file-1]) {
	    score += 5;
	    isolated = TRUE;
	  }
	}

	/* give weak, exposed pawns a penalty (not as much as in the midgame,
	   since there may be no pieces to take advantage of it): */
	if (!pawns[1][pawn_file]) {
	  if (backwards) score += 3;
	  if (isolated) score += 5;
	}

	/* give doubled, trippled, etc.. pawns a penalty (bigger in the
	   endgame, since they will become big targets): */
	if (pawns[0][pawn_file] > 1)
	  score += 3*(pawns[0][pawn_file]-1);

	/* give bonuses for passed pawns (bigger in the endgame since passed
	   pawns are what wins the endgame): */
	if (!pawns[1][pawn_file] && rank <= white_back_pawn[pawn_file-1] &&
	    rank <= white_back_pawn[pawn_file+1]) {
	  score -= 3*black_pawn_extra[i];
	  /* give an extra bonus if a connected, passed pawn: */
	  if (!isolated)
	    score -= 18;
	}

	break;

      case (wrook):
	score += 500;

	/* give bonuses depending on how open the rook's file is: */
	if (!pawns[1][pawn_file]) {
	  /* half open file */
	  score += 5;
	  if (!pawns[0][pawn_file]) {
	    /* open file */
	    score += 3;
	  }
	}

	break;

      case (brook):
	score -= 500;

	/* give bonuses depending on how open the rook's file is: */
	if (!pawns[0][pawn_file]) {
	  /* half open file */
	  score -= 5;
	  if (!pawns[1][pawn_file]) {
	    /* open file */
	    score -= 3;
	  }
	}

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
    }
  }

  /* the e/d pawn blockage is not relevant in the endgame, and we don't need
     to check for king safety due to pawn storms / heavy piece infiltration */

  /* encourage trading off material when one side has a material advantage
     (note that we don't need to worry about whether material score dropped or
     not, because search will naturally choose a variation where it keeps its
     material score over one with a small positional bonus): */
  if (score > 90 && start_piece_count < piece_count) {
    score += 10;
  }
  else if (score < 90 && start_piece_count < piece_count) {
    score -= 10;
  }

  /* adjust for color: */
  if (white_to_move == 1) {
    return score;
  }
  else {
    return -score;
  }

}