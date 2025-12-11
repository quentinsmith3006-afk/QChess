package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;

import java.util.List;

public class Queen extends ChessPiece {
    public Queen(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WQueen.png","/ChessAssets/BQueen.png");
    }

    @Override
    public List<Coordinate> getPlayableMoves() {
        return List.of();
    }
}
