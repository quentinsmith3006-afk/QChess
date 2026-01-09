package qchess.chess.create.interfaces;

import org.jetbrains.annotations.ApiStatus;

/**
 * @author Quentin Smith
 *
 * A special chess piece is one whose sole purpose is making the chess game function.
 *
 * A special piece is not a playable piece and is removed after every move on the board.
 */
@ApiStatus.Internal
public interface SpecialPiece {

    /**
     * @return The change in row of the location that this special piece affects.
     */
    public int deltaRow();

    /**
     * @return The change in column of the location that this special piece affects.
     */
    public int deltaCol();
}
