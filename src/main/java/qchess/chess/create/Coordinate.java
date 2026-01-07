package qchess.chess.create;

import qchess.chess.logic.ChessBoard;

import java.util.HashMap;
import java.util.Objects;

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

    public Coordinate(int row, int col) {
        if (col < ALGEBRAIC.length && col >= 0) {
            algebraicName = String.format("%s%s", ALGEBRAIC[col], row + 1);
        }
        this.row = row;
        this.col = col;
        this.btnID = row * ChessBoard.width + col; // make sure this is correct
    }

    public Coordinate(int btnID) {
        this(btnID / ChessBoard.height, btnID % ChessBoard.width);
        this.btnID = btnID;
    }

    public Coordinate(String algebraicNotation) {
        this(
                Coordinate.ALGEBRAIC_MAP.get(algebraicNotation.substring(0, 1)),
                Integer.parseInt(algebraicNotation.substring(1, 2))
        );
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getAlgebraicName() {
        return algebraicName;
    }

    public int getBtnID() {
        return btnID;
    }

    public boolean isCanCapture() {
        return canCapture;
    }

    public void setCanCapture(boolean canCapture) {
        this.canCapture = canCapture;
    }

    private static HashMap<String, Integer> algebraicMapInit() {
        HashMap<String, Integer> map = new HashMap<>();

        for (int letterIndex = 0; letterIndex < ALGEBRAIC.length; letterIndex++) {
            map.put(ALGEBRAIC[letterIndex], letterIndex);
        }

        return map;
    }

    public String toString() {
        return "Col: " + col + ", Row: " + row;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Coordinate)) return false;
        return this.col == ((Coordinate) obj).col && this.row == ((Coordinate) obj).row;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
