/**************************************************
 * File: protos.h                                 *
 * Purpose: holds prototypes for functions        *
 **************************************************/

#ifndef PROTOSX_H
#define PROTOSX_H

long int allocate_time (void);
int bioskey (void);
move_s book_move (void);
cbool check_legal (move_s moves[], int m);
long int chk_hash (int alpha, int beta, int depth, int *type, move_s *move);
void comp_to_coord (move_s move, char str[]);
d_long compute_hash (void);
void display_board (FILE *stream, int color);
void hash_to_pv (int depth);
long int end_eval (void);
long int eval (void);
void gen (move_s moves[], int *num_moves);
void ics_game_end (void);
cbool in_check (void);
void init_book (void);
void init_game (void);
void init_hash_tables (void);
void init_hash_values (void);
cbool is_attacked (int square, int color);
cbool is_castle (char c);
cbool is_column (char c);
cbool is_comment (char *input);
cbool is_draw (void);
cbool is_move (char str[]);
cbool is_rank (char c);
cbool is_seperator (char c);
cbool is_trailing (char c);
cbool is_valid_comp (move_s in_move);
void make (move_s moves[], int i);
void make_book (char *file_name, int max_ply);
long int mid_eval (void);
long int opn_eval (void);
void order_moves (move_s moves[], long int move_ordering[], int num_moves,
		  move_s *h_move);
void perft (int depth);
void parse_cmdline (int argc, char *argv[]);
void perft_debug (void);
move_s pgn_to_comp (const char *input);
cbool possible_move (char *input);
void post_thinking (long int score);
void print_move (move_s moves[], int m, FILE *stream);
void push_king (move_s moves[], int *num_moves, int from, int target, 
		int castle_type);
void push_knight (move_s moves[], int *num_moves, int from, int target);
void push_pawn (move_s moves[], int *num_moves, int from, int target, 
		cbool is_ep);
void push_slide (move_s moves[], int *num_moves, int from, int target);
long int qsearch (int alpha, int beta, int depth);
void rdelay (int time_in_s);
long int rdifftime (rtime_t end, rtime_t start);
void refresh_hash (void);
cbool remove_one (int *marker, long int move_ordering[], int num_moves);
void reset_piece_square (void);
void rinput (char str[], int n, FILE *stream);
rtime_t rtime (void);
long int search (int alpha, int beta, int depth, cbool do_null);
move_s search_root (int alpha, int beta, int depth);
void shut_down (int status);
void show_counter (long int game_count);
void start_up (void);
void store_hash (int alpha, int depth, int score, int flag, move_s move);
move_s think (void);
void toggle_bool (cbool *var);
void tree (int depth, int indent, FILE *output, char *disp_b);
void tree_debug (void);
void unmake (move_s moves[], int i);
void u_killers (move_s move, long int score);
cbool verify_coord (char input[], move_s *move);
piece_t which_piece (char c);
void cxor (d_long *a, d_long b);

#endif
