package qchess.chess.create.direction;

import qchess.chess.create.Coordinate;
import qchess.chess.logic.ChessBoard;

import java.util.*;
import java.util.function.Function;

/*
Problem this aims to solve:

Currently, even in a sorted list of coordinates, if a piece obstructs one from any direction,
it will stop rendering all coords.

PieceVector can work alongside Coordinate to act as a directional element that runs continuously-ish
 */


// Should represent a direction and magnitude || Starting point of the vector must be a coordinate


/*
Make piece vector abstract and then make Bounded and Unbounded versions
 */
/**
 * @author Quentin Smith
 * <br>
 * This vector is <b>restrained</b> by the sides of the board.
 * This vector is also continuous.
 */
public class BoundedPieceVector extends PieceVector {
    public final static int INF = ChessBoard.width * ChessBoard.height;

    /**
     * Generates a new vector with a length of {@code magnitude}, direction of {@code deltaRow} and {@code deltaCol} and
     * a non-inclusive starting coordinate of {@code start}.
     * @param start coordinate where the vector generates from.
     * @param deltaRow change in row as the vector moves along the chess board.
     * @param deltaCol change in column as the vector moves along the chess board.
     * @param magnitude length of the vector.
     */
    public BoundedPieceVector(Coordinate start, int deltaRow, int deltaCol, int magnitude) {
        super(start, deltaRow, deltaCol, magnitude);
        // If magnitude is 0 then vector is infinite
        // deltaRow && deltaCol is 0 then vector is essentially a scalar
        if (deltaRow == 0 && deltaCol == 0) {
            throw new IllegalArgumentException("deltaRow and deltaCol cannot be 0 at the same time.");
        } // if

        // DEPRECATED VECTOR GENERATION. KEEPING IT HERE FOR TESTING REASONS
        /*
         * double r = start.getRow() + (deltaRow < 0 ? Math.floor(deltaRow) : deltaRow);
         *  double c = start.getCol() + (deltaCol < 0 ? Math.floor(deltaCol) : deltaCol);
         *
         * double m = magnitude;
         *
         * Function<Integer, Boolean> isRowInBounds = (g) -> g < ChessBoard.height && g >= 0;
         * Function<Integer, Boolean> isColInBounds = (g) -> g < ChessBoard.width && g >= 0;
         *
         * for (;isRowInBounds.apply((int) Math.ceil(r)) && isColInBounds.apply((int) Math.ceil(c)) && m > 0; r += deltaRow,  c += deltaCol, m--) {
         *   int appliedR = (int) Math.ceil(r);
         *   int appliedC = (int) Math.ceil(c);
         *
         *   coordinates.add(new Coordinate(appliedR, appliedC));
         * }
         *
         */

        if (Math.abs(deltaRow) > Math.abs(deltaCol)) {
            generateColVector(deltaRow, deltaCol, magnitude);
        } else {
            generateRowVector(deltaRow, deltaCol, magnitude);
        } // else

        this.sort(this.coordinates);
    }

    /**
     * Generates a new vector with an unbounded length, direction of {@code deltaRow} and {@code deltaCol} and
     * a non-inclusive starting coordinate of {@code start}.
     * @param start coordinate where the vector generates from.
     * @param deltaRow change in row as the vector moves along the chess board.
     * @param deltaCol change in column as the vector moves along the chess board.
     */
    public BoundedPieceVector(Coordinate start, int deltaRow, int deltaCol) {
        this(start, deltaRow, deltaCol, PieceVector.INF);
    }

    /**
     * @param deltaRow Change in row.
     * @param deltaCol Change in col.
     * @param magnitude Length of the line.
     */
    void generateRowVector(int deltaRow, int deltaCol, int magnitude) {
        Function<Integer, Boolean> isRowInBounds = (g) -> g < ChessBoard.height && g >= 0;
        Function<Integer, Boolean> isColInBounds = (g) -> g < ChessBoard.width && g >= 0;

        int rowDirection = deltaRow / (Math.abs(deltaRow) == 0 ? 1 : Math.abs(deltaRow));
        int colDirection = deltaCol / (Math.abs(deltaCol) == 0 ? 1 : Math.abs(deltaCol));

        int r = start.getRow() + rowDirection;
        int c = start.getCol() + colDirection;
        int m = magnitude;

        int cM = Math.abs(deltaCol); // Column magnitude

        for (; isRowInBounds.apply(r) && isColInBounds.apply(c) && m > 0; r+=rowDirection, cM = Math.abs(deltaCol)) {
            for (; isColInBounds.apply(c) && m > 0 && cM > 0; c += colDirection, m--, cM--) {
                coordinates.add(new Coordinate(r, c));
            } // for
        } // for
    }

    /**
     * @param deltaRow Change in row.
     * @param deltaCol Change in col.
     * @param magnitude Length of the line.
     */
    void generateColVector(int deltaRow, int deltaCol, int magnitude) {

        Function<Integer, Boolean> isRowInBounds = (g) -> g < ChessBoard.height && g >= 0;
        Function<Integer, Boolean> isColInBounds = (g) -> g < ChessBoard.width && g >= 0;

        int rowDirection = deltaRow / (Math.abs(deltaRow) == 0 ? 1 : Math.abs(deltaRow));
        int colDirection = deltaCol / (Math.abs(deltaCol) == 0 ? 1 : Math.abs(deltaCol));

        int r = start.getRow() + rowDirection;
        int c = start.getCol() + colDirection;
        int m = magnitude;

        int rM = Math.abs(deltaRow); // row magnitude

        for (; isColInBounds.apply(c) && isRowInBounds.apply(r) && m > 0; c+=colDirection, rM = Math.abs(deltaRow)) {
            for (; isRowInBounds.apply(r) && m > 0 && rM > 0; r += rowDirection, m--, rM--) {
                coordinates.add(new Coordinate(r, c));
            } // for
        } // for
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        BoundedPieceVector vector = (BoundedPieceVector) obj;
        boolean deltaRowsEqual = deltaRow == vector.deltaRow;
        boolean deltaColsEqual = deltaCol == vector.deltaCol;
        boolean magnitudeEqual = magnitude == vector.magnitude;
        boolean startEqual = start.equals(vector.start);

        return deltaRowsEqual && deltaColsEqual && magnitudeEqual && startEqual;

    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(start, deltaRow, deltaCol, magnitude);
    }

    /** {@inheritDoc} */
    @Override
    public BoundedPieceVector inverse() {
        int reversedDeltaRow = deltaRow * -1;
        int reversedDeltaCol = deltaCol * -1;
        return new BoundedPieceVector(start, reversedDeltaRow, reversedDeltaCol);
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> verticalReflection() {
        ArrayList<ChessDirection> result = new ArrayList<>();

        BoundedPieceVector vector = new BoundedPieceVector(start, deltaRow, deltaCol * -1, this.magnitude);

        result.add(vector);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> horizontalReflection() {
        ArrayList<ChessDirection> result = new ArrayList<>();

        BoundedPieceVector vector = new BoundedPieceVector(this.start, deltaRow * -1, deltaCol, this.magnitude);

        result.add(vector);
        return result;
    }
}
