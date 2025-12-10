package qchess.chess.create;

import java.util.List;

public abstract class ChessPiece {
    Coordinate position;
    boolean pinned;
    String name;

    public ChessPiece(Coordinate position) {
        this.position = position;
        this.name = this.getClass().getSimpleName();
    }

    public Coordinate getPosition() {
        return position;
    }

    public void setPosition(Coordinate position) {
        this.position = position;
    }

    public boolean isPinned() {
        return pinned;
    }

    public int getBtnID() {
        return this.position.btnID;
    }

    public int getRow() {
        return this.position.row;
    }

    public int getCol() {
        return this.position.col;
    }

    public void setBtnID(int btnID) {
        this.position.btnID = btnID;
    }

    public void setRow(int row) {
        this.position.row = row;
    }

    public void setCol(int col) {
        this.position.col = col;
    }


    public abstract List<String> getPlayableMoves();

    @Override
    public String toString() {
        return name;
    }
}
