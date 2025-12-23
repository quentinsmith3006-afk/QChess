package qchess.chess.create.direction;

import qchess.chess.create.Coordinate;

import java.util.Arrays;

public class PieceScalar extends ChessDirection {
    public PieceScalar(Coordinate start) {
        super(start);
    }

    public PieceScalar(Coordinate start, Coordinate... coordinates) {
        super(start);

        this.coordinates.addAll(Arrays.asList(coordinates));

        this.sort(this.coordinates, start);
    }

    public void addCoordinate(Coordinate coordinate) {
        this.coordinates.add(coordinate);
        this.sort(this.coordinates, start);
    }
}
