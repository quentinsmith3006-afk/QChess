package qchess.chess.create.direction;

import javafx.scene.transform.Scale;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;
import qchess.chess.create.Coordinate;
import qchess.chess.logic.ChessBoard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class PieceScalar extends ChessDirection {
    private final ArrayList<Coordinate> uncheckedCoordinates;

    public PieceScalar(Coordinate start) {
        super(start);
        uncheckedCoordinates = new ArrayList<>();
    }

    public PieceScalar(Coordinate start, Coordinate... coordinates) {
        super(start);
        uncheckedCoordinates = new ArrayList<>(Arrays.asList(coordinates));

        for (Coordinate coordinate : coordinates) {
            if (PieceScalar.isInBounds(coordinate)) {
                this.coordinates.add(coordinate);
            }
            uncheckedCoordinates.add(coordinate);
        }

        this.sort(this.coordinates);
    }

    public boolean addCoordinate(Coordinate coordinate) {
        if (PieceScalar.isInBounds(coordinate)) {
            this.coordinates.add(coordinate);
            this.sort(this.coordinates);
            return true;
        }

        uncheckedCoordinates.add(coordinate);
        return false;
    }

    public static boolean isInBounds(Coordinate coordinate) {
        boolean rowInBounds = coordinate.getRow() >= 0 && coordinate.getRow() < ChessBoard.height;
        boolean colInBounds = coordinate.getCol() >= 0 && coordinate.getCol() < ChessBoard.width;
        return rowInBounds && colInBounds;
    }

    public List<PieceScalar> divideScalar(PieceScalar scalar) {
        List<PieceScalar> result = new ArrayList<>();
        for (Coordinate coordinate : scalar.coordinates) {
            result.add(new PieceScalar(coordinate));
        }

        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PieceScalar scalar = (PieceScalar) obj;
        return coordinates.equals(scalar.coordinates) && start == scalar.start;
    }

    @Override
    public int hashCode() {
        return Objects.hash(start, coordinates);
    }

    @Override
    public PieceScalar inverse() {
        PieceScalar scalar = new PieceScalar(this.start);
        for (Coordinate coordinate : this.coordinates) {
            int row = coordinate.getRow();
            int col = coordinate.getCol();

            int rowDist = row - start.getRow();
            int colDist = col - start.getCol();

            scalar.addCoordinate(new Coordinate(row + rowDist * -1, col + colDist * -1));
        }

        return scalar;
    }

    public static void addAll(List<PieceScalar> directions, PieceScalar... chessScalars) {
        directions.addAll(Arrays.asList(chessScalars));
    }

    @Override
    public List<ChessDirection> horizontalReflection() {
        List<ChessDirection> result = new ArrayList<>();
        ArrayList<Coordinate> additionalUncheckedCoordinates = new ArrayList<>();

        for (Coordinate coordinate : this.uncheckedCoordinates) {
            int row = coordinate.getRow();
            int col = coordinate.getCol();
            int rowDist = row - start.getRow();

            Coordinate reflectedCoord = new Coordinate(start.getRow() + rowDist * -1, col);
            result.add(new PieceScalar(this.start, reflectedCoord));
            additionalUncheckedCoordinates.add(reflectedCoord);
        }

        uncheckedCoordinates.addAll(additionalUncheckedCoordinates);
        return result;
    }

    @Override
    public List<ChessDirection> verticalReflection() {
        List<ChessDirection> result = new ArrayList<>();
        ArrayList<Coordinate> additionalUncheckedCoordinates = new ArrayList<>();

        for (Coordinate coordinate : this.uncheckedCoordinates) {
            int row = coordinate.getRow();
            int col = coordinate.getCol();
            int colDist = col - start.getCol();

            Coordinate reflectedCoord = new Coordinate(row, start.getCol() + colDist * -1);
            result.add(new PieceScalar(this.start, reflectedCoord));
            additionalUncheckedCoordinates.add(reflectedCoord);
        }

        uncheckedCoordinates.addAll(additionalUncheckedCoordinates);
        return result;
    }
}
