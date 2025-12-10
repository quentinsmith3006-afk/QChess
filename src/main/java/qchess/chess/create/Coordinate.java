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
    static HashMap<String, Integer> ALGEBRAICMAP = algebraicMapInit();

    public Coordinate(int row, int col) {
        algebraicName = String.format("%s%s", ALGEBRAIC[col], row);
        this.row = row;
        this.col = col;
        this.btnID = col * 8 + row;
    }

    public Coordinate(int btnID) {
        this.btnID = btnID;
        this.row = btnID / 8;
        this.col = btnID % 8;
    }

    public Coordinate(String algebraicNotation) {
        this(
                Coordinate.ALGEBRAICMAP.get(algebraicNotation.substring(0, 1)),
                Integer.parseInt(algebraicNotation.substring(1, 2))
        );

    }

    private static HashMap<String, Integer> algebraicMapInit() {
        ALGEBRAICMAP = new HashMap<>();

        for (int letter = 0; letter < ALGEBRAIC.length; letter++) {
            ALGEBRAICMAP.put(ALGEBRAIC[letter], letter);
        }

        return ALGEBRAICMAP;
    }
}
