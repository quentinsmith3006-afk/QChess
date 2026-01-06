package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.create.interfaces.SpecifyCapture;
import qchess.chess.create.piecemodifiers.HorizonalSymmetry;
import qchess.chess.create.piecemodifiers.VerticalSymmetry;

import java.util.ArrayList;
import java.util.List;

@VerticalSymmetry
@HorizonalSymmetry
public class Bishop extends ChessPiece {
    public Bishop(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WBishop.png", "/ChessAssets/BBishop.png");
        this.pieceValue = 3;
    }

    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        List<ChessDirection> moves = new ArrayList<>();

        PieceVector bottomRightDiagonal = new PieceVector(this.coordinate, 1, 1, PieceVector.INF);

        moves.add(bottomRightDiagonal);
        return moves;
    }
}
