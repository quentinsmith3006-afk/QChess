package qchess.chess.create.interfaces;

import qchess.chess.create.direction.ChessDirection;

import java.util.List;

/**
 * @author Quentin Smith
 *
 * Specify capturable chess directions.
 *
 * Capturable chess directions represent positions/locations where a chess piece can capture.
 * They only appear as a possible move when a chess piece from a different team occupies the square.
 *
 * Capturable directions do <b>NOT</b> xray through chess pieces.
 */
public interface SpecifyCapture {
    public List<ChessDirection> getCapturableMoves();
}
