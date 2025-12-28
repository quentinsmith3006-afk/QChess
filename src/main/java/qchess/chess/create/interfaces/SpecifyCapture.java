package qchess.chess.create.interfaces;

import qchess.chess.create.direction.ChessDirection;

import java.util.List;

public interface SpecifyCapture {
    public List<ChessDirection> getCapturableMoves();
}
