package qchess.chess.create;

import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.List;

public abstract class ChessPiece {
    Coordinate position;
    boolean pinned;
    String name;
    Team team;
    ImageView graphic;

    public ChessPiece(Coordinate position, Team team, String WhiteTeamGraphic, String BlackTeamGraphic) {
        this.position = position;
        this.name = this.getClass().getSimpleName();
        this.team = team;

        if (WhiteTeamGraphic != null && BlackTeamGraphic != null) {
            if (team == Team.WHITE) {
                Image img = new Image(WhiteTeamGraphic);
            } else if (team == Team.BLACK) {
                Image img = new Image(BlackTeamGraphic);
            }
            ImageView imgView = new ImageView();
            this.setGraphic(imgView);
        }
    }

    public ChessPiece(Coordinate position, Team team) {
        this(position, team, null, null);
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

    public ImageView getGraphic() {
        return this.graphic;
    }

    public String getName() {
        return name;
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

    public void setGraphic(ImageView graphic) {
        this.graphic = graphic;
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

    public abstract List<Coordinate> getPlayableMoves(Coordinate startingPosition);

    @Override
    public String toString() {
        return name;
    }
}
