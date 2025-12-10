package qchess.chess.create;

import java.util.HashMap;

public class Coordinate {
    int row;
    int col;
    int btnID;
    String algebraicName;

    static String[] ALGEBRAIC = new String[]{
            "a", "b", "c", "d", "e", "f", "g", "h"
    };
    static HashMap<String, Integer> ALGEBRAIC_MAP = algebraicMapInit();

    public Coordinate(int row, int col) {
        algebraicName = String.format("%s%s", ALGEBRAIC[col], row);
        this.row = row;
        this.col = col;
        this.btnID = row * 8 + col; // make sure this is correct
    }

    public Coordinate(int btnID) {
        this.btnID = btnID;
        this.row = btnID / 8;
        this.col = btnID % 8;
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

    private static HashMap<String, Integer> algebraicMapInit() {
        ALGEBRAIC_MAP = new HashMap<>();

        for (int letter = 0; letter < ALGEBRAIC.length; letter++) {
            ALGEBRAIC_MAP.put(ALGEBRAIC[letter], letter);
        }

        return ALGEBRAIC_MAP;
    }
}
