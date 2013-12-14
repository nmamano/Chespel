Chespel
=======

Chespel is a high-level programming language (intended for non-programmers too) to design chess AIs.
It abstracts the programmer from the board representation and search techniques,
allowing him to focus on the creative part: board evaluation

Chespel allows...
- new user to create a chess AI in 5 minutes capable of beating average human players.
- expert users to compare and analyze board evaluation techniques



Code example (which generates an AI that beats an average player):
=========================================================

module search

maxTime = 4 // the search algorithm has 4 seconds to decide a move


module evaluation

//the aggregate value of my pieces minus the aggregate value of the opponents pieces
sym rule pieceValues:
    piece p with player = me:
        score p.value;


=========================================================


Contact info: tehotserver@gmail.com
