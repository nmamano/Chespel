Chespel
=======

Chespel is a high-level programming language (intended for non experienced programmers too) to design chess AIs.

The design of a chess AI has a more technical part (board representation and search techniques), and a more creative side, Leaf evaluation (how to evaluate the *goodness* of a board position for a given player). Chespel abstracts the user from the more technical part, allowing him to focus on the creative part.

A Chespel program is essentially a set of rules for leaf evaluation. It can be compiled into C++, where the rules are translated and integrated into a full chess engine with board representation and search techniques. The quality of the resulting chess engine will depend on the quality of the leaf evaluation.

Code example:
-------------

    //value of my pieces minus value of opponent pieces
    sym rule totalPieceValue:
      piece p with player == me:
      	score p.type.value;
      
    //0.1 points for each pawn advancement
    sym rule forwardPawnLine:
      piece p with player == me, type == pawn:
      	score abs(p.cell.row - p.startingRow) * 0.1;
      	
    //-1 point for each non-covered piece
    sym rule nonCovered:
      piece p with player == me:
      	if (p.coveredBy == []) score -1;


Chespel goals:
--------------
- Allow users to create a non-trivial chess engine in as little as 10 minutes.
- Allow people without programming experience to build their own chess engine in an easy way.
- Allow more advanced users to compare and analyze different leaf evaluation strategies.
