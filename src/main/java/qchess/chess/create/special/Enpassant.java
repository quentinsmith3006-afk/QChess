package qchess.chess.create.special;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.Team;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.interfaces.SpecialPiece;

import java.util.List;

public class Enpassant extends ChessPiece implements SpecialPiece {

    public Enpassant(Coordinate coordinate, Team team) {
        super(coordinate, team, null, null);
    }

    @Override
    public List<ChessDirection> getRawPlayableDirections() {
        return List.of();
    }

    @Override
    public int deltaRow() {
        return 1;
    }
    @Override
    public int deltaCol() {
        return 0;
    }
}
