package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;

import java.util.List;

public class Bishop extends ChessPiece {
    public Bishop(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WBishop.png", "/ChessAssets/BBishop.png");
        this.pieceValue = 3;
    }

    @Override
    public List<ChessDirection> getPlayableMoves() {
        return List.of();
    }
}
