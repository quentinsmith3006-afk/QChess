package qchess.chess.chessmen;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;

import java.util.List;

public class King extends ChessPiece {
    public King(Coordinate position) {
        super(position);
    }

    @Override
    public List<String> getPlayableMoves() {
        return List.of();
    }
}
