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
public class PieceVector extends ChessDirection {
    public final static int INF = ChessBoard.width * ChessBoard.height;
    private final int deltaRow;
    private final int deltaCol;
    private final int magnitude;

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

        this.sort(this.coordinates, start);
    }

    public PieceVector(Coordinate start, int deltaRow, int deltaCol) {
        this(start, deltaRow, deltaCol, PieceVector.INF);
    }

    public Coordinate getTerminalPoint() {
        return coordinates.getLast();
    }

    public Coordinate getInitialPoint() {
        return coordinates.getFirst();
    }

    public static void addAll(List<PieceVector> directions, PieceVector... chessVectors) {
        directions.addAll(Arrays.asList(chessVectors));
    }

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

    @Override
    public int hashCode() {
        return Objects.hash(start, deltaRow, deltaCol, magnitude);
    }

    @Override
    public PieceVector inverse() {
        int reversedDeltaRow = deltaRow * -1;
        int reversedDeltaCol = deltaCol * -1;
        return new PieceVector(start, reversedDeltaRow, reversedDeltaCol);
    }

    @Override
    public List<ChessDirection> verticalReflection() {
        ArrayList<ChessDirection> result = new ArrayList<>();

        PieceVector vector = new PieceVector(start, deltaRow, deltaCol * -1, this.magnitude);

        result.add(vector);
        return result;
    }

    @Override
    public List<ChessDirection> horizontalReflection() {
        ArrayList<ChessDirection> result = new ArrayList<>();

        PieceVector vector = new PieceVector(this.start, deltaRow * -1, deltaCol, this.magnitude);

        result.add(vector);
        return result;

    }
}
