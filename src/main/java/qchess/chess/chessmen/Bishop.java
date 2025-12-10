package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;

import java.util.List;

public class Bishop extends ChessPiece {
    public Bishop(Coordinate position, Team team) {
        super(position, team, "ChessAssets/WBishop.png","ChessAssets/BBishop.png");
    }

    @Override
    public List<Coordinate> getPlayableMoves(Coordinate startingPosition) {
        return List.of();
    }
}
