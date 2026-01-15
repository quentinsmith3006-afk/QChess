package qchess.chess.create.direction;

import qchess.chess.create.Coordinate;
import qchess.chess.logic.ChessBoard;

/**
 * @author Quentin Smith
 * <br>
 * For QChess purposes, all vectors do is hold the coordinate values.
 * <br>
 * A vector is defined by its magnitude and direction where its direction isn't an angle but
 * the rate of change of the row and column with respect to the start position.
 *
 */
public abstract class PieceVector extends ChessDirection {
    public final static int INF = ChessBoard.width * ChessBoard.height;
    protected int magnitude;
    protected final int deltaRow;
    protected final int deltaCol;

    /**
     * @param start anchor point.
     */
    public PieceVector(Coordinate start, int deltaRow, int deltaCol, int magnitude) {
        super(start);
        this.deltaRow = deltaRow;
        this.deltaCol = deltaCol;
        this.magnitude = magnitude;
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
}
