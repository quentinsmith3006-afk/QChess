package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.interfaces.Checkable;

import java.util.List;

public class King extends ChessPiece implements Checkable {
    public King(Coordinate position, Team team) {
        super(position, team, "/ChessAssets/WKing.png","/ChessAssets/BKing.png");
    }

    @Override
    public List<Coordinate> getPlayableMoves() {
        return List.of();
    }
}
