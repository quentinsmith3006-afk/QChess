package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.create.piecemodifiers.HorizonalSymmetry;
import qchess.chess.create.piecemodifiers.VerticalSymmetry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Quentin Smith
 *
 * Creates the raw playables for the classic Rook chess piece.
 * A Rook has playables which encompass all squares on its left, right, top and bottom.
 */
@VerticalSymmetry
@HorizonalSymmetry
public class Rook extends ChessPiece {
    public Rook(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WRook.png","/ChessAssets/BRook.png");
        this.pieceValue = 5;
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        List<ChessDirection> moves = new ArrayList<>();

        PieceVector downwardVector = new PieceVector(this.coordinate, 1, 0, PieceVector.INF);
        PieceVector rightwardVector = new PieceVector(this.coordinate, 0, 1, PieceVector.INF);

        moves.add(downwardVector);
        moves.add(rightwardVector);

        return moves;

    }
}
