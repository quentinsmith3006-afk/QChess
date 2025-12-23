package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;

import java.util.List;

public class Rook extends ChessPiece {
    public Rook(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WRook.png","/ChessAssets/BRook.png");
        this.pieceValue = 5;
    }

    @Override
    public List<ChessDirection> getPlayableMoves() {
        return List.of();
    }
}
