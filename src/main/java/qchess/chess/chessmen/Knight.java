package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;

import java.util.List;

public class Knight extends ChessPiece {
    public Knight(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WKnight.png","/ChessAssets/BKnight.png");
    }

    @Override
    public List<Coordinate> getPlayableMoves() {
        return List.of();
    }
}
