package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;

import java.util.List;

public class Pawn extends ChessPiece {
    public Pawn(Coordinate position) {
        super(position);
    }

    @Override
    public List<String> getPlayableMoves() {
        return List.of();
    }
}
