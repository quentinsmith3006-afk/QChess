package qchess.chess.chessmen;

import qchess.chess.create.*;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.create.piecemodifiers.HorizonalSymmetry;
import qchess.chess.create.piecemodifiers.VerticalSymmetry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Quentin Smith
 *
 * Creates the raw playables for the classic Bishop chess piece.
 * A bishop has playables in all 4 diagnoles where each diagnole has a slope of 1.
 */
@VerticalSymmetry
@HorizonalSymmetry
public class Bishop extends ChessPiece {
    public Bishop(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WBishop.png", "/ChessAssets/BBishop.png");
        this.pieceValue = 3;
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        List<ChessDirection> moves = new ArrayList<>();

        PieceVector bottomRightDiagonal = new PieceVector(this.coordinate, 1, 1, PieceVector.INF);

        moves.add(bottomRightDiagonal);
        return moves;
    }
}
