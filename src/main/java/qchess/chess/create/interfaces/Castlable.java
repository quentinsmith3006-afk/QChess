package qchess.chess.create.interfaces;

import qchess.chess.create.direction.CastleVector;
import qchess.chess.create.direction.PieceScalar;

import java.util.HashMap;

/**
 * @author Quentin Smith
 *
 * Allows a chess piece which implements {@code Castlable} have the ability to castle.
 *
 * {@link qchess.chess.create.ChessPiece}
 * {@link qchess.chess.chessmen.King}
 */
public interface Castlable {

    /**
     * The CastleVector inside of Castle directions refers to the vector position where castling occurs. For a normal chess game,
     * this would be a vector from the King to the Rook (at the end of the board).
     *
     * The PieceScalar inside of castle directions refers to the position a user clicks in order to castle.
     *
     * Thus, each entry of the returned HashMap must reflect:
     * (Location where user can click to castle : The range from the Castlable to the co-castling chess piece.)
     *
     * @return directions on how to castle the castlable chess piece.
     */
    public HashMap<PieceScalar, CastleVector> getCastleDirections();

    /**
     * @return true if this chess piece has castled before and false otherwise.
     */
    public boolean hasCastled();

    /**
     * @param hasCastled sets new state of castling.
     */
    public void setHasCastled(boolean hasCastled);

    /**
     * Since castling is entirely determined by the castle directions, the chess piece
     * at the terminal point of the CastleVector becomes the co-castling piece.
     *
     * This piece must be initialized before castling can occur.
     *
     * @param initializedCastleDirections castle directions with its co-castling piece identified.
     */
    public void setInitializedCastleDirections(HashMap<PieceScalar, CastleVector> initializedCastleDirections);

    /**
     * @return castle directions with its co-castling piece identified for the calling Castlable.
     */
    public HashMap<PieceScalar, CastleVector> getInitializedCastleDirections();
}
