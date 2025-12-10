package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;

import java.util.List;

public class King extends ChessPiece {
    public King(Coordinate position, Team team) {
        super(position, team);
    }

    @Override
    public List<Coordinate> getPlayableMoves(Coordinate startingPosition) {
        return List.of();
    }
}
