package qchess.chess.create.direction;

import org.jetbrains.annotations.ApiStatus;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;

import java.util.Arrays;
import java.util.List;

/**
 * @author Quentin Smith
 *
 * A castlevector is a piecevector, but its terminal point is used to identify the co-castling piece--rook in normal chess--.
 */
public class CastleVector extends PieceVector {
    ChessPiece castleDependent;
    static int id = 0;
    String name;

    /**
     * Generates a new vector with a length of {@code magnitude}, direction of {@code deltaRow} and {@code deltaCol} and
     * a non-inclusive starting coordinate of {@code start}.
     * @param start coordinate where the vector generates from.
     * @param deltaRow change in row as the vector moves along the chess board.
     * @param deltaCol change in column as the vector moves along the chess board.
     * @param magnitude length of the vector.
     */
    public CastleVector(Coordinate start, int deltaRow, int deltaCol, int magnitude, String name) {
        super(start, deltaRow, deltaCol, magnitude);
        this.name = name;
        id = ++id;
        if (name == null) {
            this.name = Integer.toString(id);
        }
    }


    /**
     * Generates a new vector with a length of {@code magnitude}, direction of {@code deltaRow} and {@code deltaCol} and
     * a non-inclusive starting coordinate of {@code start}.
     * @param start coordinate where the vector generates from.
     * @param deltaRow change in row as the vector moves along the chess board.
     * @param deltaCol change in column as the vector moves along the chess board.
     * @param magnitude length of the vector.
     */
    public CastleVector(Coordinate start, int deltaRow, int deltaCol, int magnitude) {
        this(start, deltaRow, deltaCol, magnitude, null);
    }

    /**
     * @return name of the vector/castle movement.
     */
    public String getName() {
        return name;
    }

    /**
     * @return co-castling piece (in normal chess this would be the rook).
     */
    @ApiStatus.Internal
    public ChessPiece getCastleDependent() {
        return castleDependent;
    }

    /**
     * Sets the co-castling piece at the terminal point of the vector.
     * @param castleDependent the chess piece at the terminal point of the vector.
     */
    @ApiStatus.Internal
    public void setCastleDependent(ChessPiece castleDependent) {
        this.castleDependent = castleDependent;
    }
}
