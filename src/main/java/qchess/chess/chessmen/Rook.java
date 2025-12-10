package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;

import java.util.List;

public class Rook extends ChessPiece {
    public Rook(Coordinate position) {
        super(position);
    }

    @Override
    public List<String> getPlayableMoves() {
        return List.of();
    }
}
