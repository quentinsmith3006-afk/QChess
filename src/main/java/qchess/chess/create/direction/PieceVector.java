package qchess.chess.create.direction;

import qchess.chess.create.Coordinate;
import qchess.chess.logic.ChessBoard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;

/*
Problem this aims to solve:

Currently, even in a sorted list of coordinates, if a piece obstructs one from any direction,
it will stop rendering all coords.

PieceVector can work alongside Coordinate to act as a directional element that runs continuously-ish
 */


// Should represent a direction and magnitude || Starting point of the vector must be a coordinate

/**
 * @author Quentin Smith
 *
 * For QChess purposes, all vectors do is hold the coordinate values.
 *
 * A vector is defined by its magnitude and direction where its direction isn't an angle but
 * the rate of change of the row and column with respect to the start position.
 */
public class PieceVector extends ChessDirection {
    public final static int INF = ChessBoard.width * ChessBoard.height;
    private final int deltaRow;
    private final int deltaCol;
    private final int magnitude;

    /**
     * Generates a new vector with a length of {@code magnitude}, direction of {@code deltaRow} and {@code deltaCol} and
     * a non-inclusive starting coordinate of {@code start}.
     * @param start coordinate where the vector generates from.
     * @param deltaRow change in row as the vector moves along the chess board.
     * @param deltaCol change in column as the vector moves along the chess board.
     * @param magnitude length of the vector.
     */
    public PieceVector(Coordinate start, int deltaRow, int deltaCol, int magnitude) {
        super(start);
        // If magnitude is 0 then vector is infinite
        // deltaRow && deltaCol is 0 then vector is essentially a scalar
        if (deltaRow == 0 && deltaCol == 0) {
            throw new IllegalArgumentException("deltaRow and deltaCol cannot be 0 at the same time.");
        }

        this.deltaRow = deltaRow;
        this.deltaCol = deltaCol;
        this.magnitude = magnitude;

        int r = start.getRow() + deltaRow, c = start.getCol() + deltaCol;
        int m = magnitude;

        Function<Integer, Boolean> isRowInBounds = (g) -> g < ChessBoard.height && g >= 0;
        Function<Integer, Boolean> isColInBounds = (g) -> g < ChessBoard.width && g >= 0;

        for (;isRowInBounds.apply(r) && isColInBounds.apply(c) && m > 0; r += deltaRow,  c += deltaCol, m--) {
            coordinates.add(new Coordinate(r, c));
        }

        this.sort(this.coordinates);
    }

    /**
     * Generates a new vector with a unbounded length, direction of {@code deltaRow} and {@code deltaCol} and
     * a non-inclusive starting coordinate of {@code start}.
     * @param start coordinate where the vector generates from.
     * @param deltaRow change in row as the vector moves along the chess board.
     * @param deltaCol change in column as the vector moves along the chess board.
     */
    public PieceVector(Coordinate start, int deltaRow, int deltaCol) {
        this(start, deltaRow, deltaCol, PieceVector.INF);
    }

    /**
     * @return coordinate end point of the vector.
     */
    public Coordinate getTerminalPoint() {
        return coordinates.getLast();
    }

    /**
     * @return coordinate start point of the vector.
     */
    public Coordinate getInitialPoint() {
        return coordinates.getFirst();
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PieceVector vector = (PieceVector) obj;
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
    public PieceVector inverse() {
        int reversedDeltaRow = deltaRow * -1;
        int reversedDeltaCol = deltaCol * -1;
        return new PieceVector(start, reversedDeltaRow, reversedDeltaCol);
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> verticalReflection() {
        ArrayList<ChessDirection> result = new ArrayList<>();

        PieceVector vector = new PieceVector(start, deltaRow, deltaCol * -1, this.magnitude);

        result.add(vector);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> horizontalReflection() {
        ArrayList<ChessDirection> result = new ArrayList<>();

        PieceVector vector = new PieceVector(this.start, deltaRow * -1, deltaCol, this.magnitude);

        result.add(vector);
        return result;

    }
}
