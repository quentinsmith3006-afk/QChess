package qchess.chess.create.direction;

import javafx.scene.transform.Scale;
import org.controlsfx.control.tableview2.filter.filtereditor.SouthFilter;
import qchess.chess.create.Coordinate;
import qchess.chess.logic.ChessBoard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author Quentin Smith
 *
 * Each PieceScalar requires a {@code start} coordinate as the primary basis at which to compare other coordinates via
 * distance.
 *
 * The {@code start} coordinate essentially acts as an anchor.
 *
 * For QChess purposes, all scalars do is hold the coordinate values.
 *
 * A scalar is primarily defined by a single point's change in row and column with respect to the start position.
 */
public class PieceScalar extends ChessDirection {
    private final ArrayList<Coordinate> uncheckedCoordinates;

    /**
     * Generates empty piece scalar.
     * @param start anchor coordinate.
     */
    public PieceScalar(Coordinate start) {
        super(start);
        uncheckedCoordinates = new ArrayList<>();
    }

    /**
     * Generates a piece scalar filled with {@code coordinates}.
     * @param start anchor coordinate.
     */
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

    /**
     * Adds this coordinate to the PieceScalar.
     *
     * Adding coordinates to piece scalars creates a dependency:
     * if a coordinate closer to start has a piece on it, all other
     * coordinates after that one will not be displayed.
     */
    public boolean addCoordinate(Coordinate coordinate) {
        if (PieceScalar.isInBounds(coordinate)) {
            this.coordinates.add(coordinate);
            this.sort(this.coordinates);
            return true;
        }

        uncheckedCoordinates.add(coordinate);
        return false;
    }

    /**
     * Checks if a coordinate is in bounds of the chess board or not.
     * @param coordinate coordinate to be checked.
     * @return true if the coordinate is within the bounds of the chess board and false otherwise.
     */
    static boolean isInBounds(Coordinate coordinate) {
        boolean rowInBounds = coordinate.getRow() >= 0 && coordinate.getRow() < ChessBoard.height;
        boolean colInBounds = coordinate.getCol() >= 0 && coordinate.getCol() < ChessBoard.width;
        return rowInBounds && colInBounds;
    }

    /**
     * Divides a single PieceScalar into multiple PieceScalars with the same anchor point but different coordinates.
     * @param scalar scalar to be divided into other PieceScalars.
     * @return List of pieceScalars which are anchored at the same start and each have a single coordinate from the coordinates in {@code scalar}.
     */
    public List<PieceScalar> divideScalar(PieceScalar scalar) {
        List<PieceScalar> result = new ArrayList<>();
        for (Coordinate coordinate : scalar.coordinates) {
            result.add(new PieceScalar(scalar.start, coordinate));
        }

        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PieceScalar scalar = (PieceScalar) obj;
        return coordinates.equals(scalar.coordinates) && start == scalar.start;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(start, coordinates);
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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
