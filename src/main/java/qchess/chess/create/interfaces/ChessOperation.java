package qchess.chess.create.interfaces;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.direction.ChessDirection;

import java.util.List;

/**
 * @author Quentin Smith
 *
 * Functional interface which operates on a list of chess direction.
 *
 * -> So far, used only for {@link qchess.chess.logic.ChessAnnotation}
 */
public interface ChessOperation {
    public void operate(ChessPiece chessPiece, List<ChessDirection> playableMoves);
}
