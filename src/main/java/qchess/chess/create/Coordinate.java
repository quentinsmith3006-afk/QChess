package qchess.chess.create;

import qchess.chess.logic.ChessBoard;

import java.util.HashMap;
import java.util.Objects;

/**
 * @author Quentin Smith
 * <br>
 * <p>
 * {@code Coordinate} represents a actual coordinate (e.g. (0, 4), (1, 2) ... etc.), a btnID location (e.g. (0, 1, 2 ... etc.))
 * and algebraic notation (e.g. a1, b4, g3 ... etc.).
 * </p>
 * <br>
 * <p>
 * Coordinates are not bounded to a chess board and are supposed to be very disposable.
 * Though, if a coordinate is out of bounds, it will not have a defined {@code algebraicName}.
 * </p>
 */
public class Coordinate {
    int row;
    int col;
    int btnID;
    boolean canCapture;
    String algebraicName;

    static final String[] ALGEBRAIC = new String[]{
            "a", "b", "c", "d", "e", "f", "g", "h"
    };
    static final HashMap<String, Integer> ALGEBRAIC_MAP = algebraicMapInit();

    /**
     * Creates all 3 types of coordinates if applicable using {@code row} and {@code col}.
     * @param row row of choice.
     * @param col column of choice.
     */
    public Coordinate(int row, int col) {
        if (col < ALGEBRAIC.length && col >= 0) {
            algebraicName = String.format("%s%s", ALGEBRAIC[col], row + 1);
        }
        this.row = row;
        this.col = col;
        this.btnID = Coordinate.getBtnID(row, col); // make sure this is correct
    }

    /**
     * Creates all 3 types of coordinates if applicable using {@code btnID}.
     * @param btnID the id location of the coordinate.
     */
    public Coordinate(int btnID) {
        this(btnID / ChessBoard.height, btnID % ChessBoard.width);
        this.btnID = btnID;
    }

    /**
     * Creates all 3 types of coordinates if applicable using {@code btnID}.
     * @param algebraicNotation the algebraic notation location of the coordinate.
     */
    public Coordinate(String algebraicNotation) {
        this(
                Integer.parseInt(algebraicNotation.substring(1, 2))-1,
                Coordinate.ALGEBRAIC_MAP.get(algebraicNotation.substring(0, 1))
        );
    }

    /**
     * @return row of the coordinate.
     */
    public int getRow() {
        return row;
    }

    /**
     * @return col of the coordinate.
     */
    public int getCol() {
        return col;
    }

    /**
     * @return algebraic name of the coordinate.
     */
    public String getAlgebraicName() {
        return algebraicName;
    }

    /**
     * @return single number representation of the coordinate.
     */
    public int getBtnID() {
        return btnID;
    }

    /**
     * Generates the id position from {@code row} and {@code col} on the board.
     * @param row row.
     * @param col column.
     * @return the id position on the board.
     */
    public static int getBtnID(int row, int col) {
        return row * ChessBoard.width + col;
    }

    /**
     * @param row row to check if in bounds.
     * @return true if the given row is in bounds and false otherwise.
     */
    public static boolean isRowInBounds(int row) {
        return row >= 0 && row < ChessBoard.height;
    }

    /**
     * @param col col to check if in bounds.
     * @return true if the given row is in bounds and false otherwise.
     */
    public static boolean isColInBounds(int col) {
        return col >= 0 && col < ChessBoard.width;
    }

    /**
     * @param btnID btnID to check if in bounds.
     * @return true if the given row is in bounds and false otherwise.
     */
    public static boolean isBtnIDInBounds(int btnID) {
        return btnID >= 0 && btnID < ChessBoard.height * ChessBoard.width;
    }

    /**
     * Chess has algebraic notation and this method returns a representation of that.
     * @return an initialized algebraic map of letters and their corresponding integer.
     */
    private static HashMap<String, Integer> algebraicMapInit() {
        HashMap<String, Integer> map = new HashMap<>();

        for (int letterIndex = 0; letterIndex < ALGEBRAIC.length; letterIndex++) {
            map.put(ALGEBRAIC[letterIndex], letterIndex);
        }

        return map;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "Col: " + col + ", Row: " + row;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Coordinate)) return false;
        return this.col == ((Coordinate) obj).col && this.row == ((Coordinate) obj).row;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
