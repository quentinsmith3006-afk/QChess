package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;

import java.util.List;

public class Knight extends ChessPiece {
    public Knight(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WKnight.png","/ChessAssets/BKnight.png");
        this.pieceValue = 3;
    }

    @Override
    public List<ChessDirection> getPlayableMoves() {
        return List.of();
    }
}
