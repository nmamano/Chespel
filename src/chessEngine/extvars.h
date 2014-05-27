/**************************************************
 * File: extvars.h                                *
 * Purpose: holds global variables                *
 **************************************************/

extern char divider[50];

extern int board[144], moved[144], ep_square, white_to_move, wking_loc,
  bking_loc, white_castled, black_castled, result, ply, pv_length[PV_BUFF],
  history_h[144][144], pieces[33], squares[144], num_pieces, i_depth, fifty,
  fifty_move[PV_BUFF], game_ply;

extern long int nodes, raw_nodes, qnodes, piece_count, killer_scores[PV_BUFF],
  killer_scores2[PV_BUFF], moves_to_tc, min_per_game, inc, time_left,
  opp_time, time_cushion, time_for_move, cur_score, start_piece_count,
  last_root_score;

extern cbool xb_mode, captures, searching_pv, post, time_exit, time_failure,
  allow_more_time, bad_root_score;

extern cbool eval_debug;

extern move_s pv[PV_BUFF][PV_BUFF], dummy, killer1[PV_BUFF], killer2[PV_BUFF],
  killer3[PV_BUFF];

extern rtime_t start_time;

extern d_long h_values[14][144], ep_h_values[144], wck_h_values[2],
  wcq_h_values[2], bck_h_values[2], bcq_h_values[2], color_h_values[2],
  cur_pos, rep_history[PV_BUFF];

extern hash_s *hash_table;

extern unsigned long int hash_mask, hash_max_mb;
