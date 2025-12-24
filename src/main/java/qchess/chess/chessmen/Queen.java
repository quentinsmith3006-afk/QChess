package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;

import java.util.List;


public class Queen extends ChessPiece {
    public Queen(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WQueen.png","/ChessAssets/BQueen.png");
        this.pieceValue = 9;
    }

    @Override
    public List<ChessDirection> getPlayableMoves() {
        return List.of();
    }
}
