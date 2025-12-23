package qchess.chess.create.direction;

import qchess.chess.create.Coordinate;
import qchess.chess.logic.ChessBoard;

import java.util.Arrays;
import java.util.function.Function;

/*
Problem this aims to solve:

Currently, even in a sorted list of coordinates, if a piece obstructs one from any direction,
it will stop rendering all coords.

PieceVector can work alongside Coordinate to act as a directional element that runs continuously-ish
 */

// Should represent a direction and magnitude || Starting point of the vector must be a coordinate
public class PieceVector extends ChessDirection {
    private final int deltaRow;
    private final int deltaCol;

    public PieceVector(Coordinate start, int deltaRow, int deltaCol, int magnitude) {
        super(start);
        // If magnitude is 0 then vector is infinite
        // deltaRow && deltaCol is 0 then vector is essentially a scalar
        this.deltaRow = deltaRow;
        this.deltaCol = deltaCol;

        if (deltaRow != 0 && deltaCol != 0) {
            int inf = ChessBoard.height * ChessBoard.width;
            int r = start.getRow() + deltaRow, c = start.getCol() + deltaCol;
            int m = (magnitude != 0 ? magnitude : inf);

            Function<Integer, Boolean> isRowInBounds = (g) -> g < ChessBoard.height && g >= 0;
            Function<Integer, Boolean> isColInBounds = (g) -> g < ChessBoard.width && g >= 0;

            for (;isRowInBounds.apply(r) && isColInBounds.apply(c) && m > 0; r += deltaRow,  c += deltaCol, m--) {
                coordinates.add(new Coordinate(r, c));
            }

            this.sort(this.coordinates, start);
        }
    }

    public PieceVector(Coordinate start, int deltaRow, int deltaCol) {
        this(start, deltaRow, deltaCol, 0);
    }

    public static PieceVector reverse(PieceVector vector) {
        int reversedDeltaRow = vector.deltaRow * -1;
        int reversedDeltaCol = vector.deltaCol * -1;
        return new PieceVector(vector.start, reversedDeltaRow, reversedDeltaCol);
    }
}
