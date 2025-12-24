package qchess.chess.create.direction;

import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;
import qchess.chess.create.Coordinate;
import qchess.chess.logic.ChessBoard;

import java.util.Arrays;

public class PieceScalar extends ChessDirection {
    public PieceScalar(Coordinate start) {
        super(start);
    }

    public PieceScalar(Coordinate start, Coordinate... coordinates) {
        super(start);

        for (Coordinate coordinate : coordinates) {
            if (PieceScalar.isInBounds(coordinate)) {
                this.coordinates.add(coordinate);
            }
        }

        this.sort(this.coordinates, start);
    }

    public boolean addCoordinate(Coordinate coordinate) {
        if (PieceScalar.isInBounds(coordinate)) {
            this.coordinates.add(coordinate);
            this.sort(this.coordinates, start);
            return true;
        }
        return false;
    }

    public static boolean isInBounds(Coordinate coordinate) {
        boolean rowInBounds = coordinate.getRow() >= 0 && coordinate.getRow() < ChessBoard.height;
        boolean colInBounds = coordinate.getCol() >= 0 && coordinate.getCol() < ChessBoard.width;
        return rowInBounds && colInBounds;
    }
}
