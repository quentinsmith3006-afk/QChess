package qchess.chess.create.direction;

import org.jetbrains.annotations.ApiStatus;
import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;

import java.util.NoSuchElementException;

/**
 * @author Quentin Smith
 *
 * A castlevector is a bounded piecevector, but its terminal point is used to identify the co-castling piece--rook in normal chess--.
 */
public class CastleVector extends BoundedPieceVector {
    ChessPiece castleDependent;
    static int id = 0;
    String name;
    Coordinate kingTo;
    Coordinate dependentTo;


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
        } // if

        try {
            int determinant = this.getTerminalPoint().getCol() - this.start.getCol();

            if (determinant < 0) {
                this.kingTo = new Coordinate(start.getRow(), 2);
                this.dependentTo = new Coordinate(start.getRow(), 3);
            } else {
                this.kingTo = new Coordinate(start.getRow(), 6);
                this.dependentTo = new Coordinate(start.getRow(), 5);
            } // else
        } catch (NoSuchElementException nsee) {
        } // catch
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
     * @return the coordinate where the  piece goes to.
     */
    public Coordinate getCastlableTo() {
        return kingTo;
    }

    /**
     * @return the coordinate where the co-castling piece goes to.
     */
    public Coordinate getDependentTo() {
        return dependentTo;
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
