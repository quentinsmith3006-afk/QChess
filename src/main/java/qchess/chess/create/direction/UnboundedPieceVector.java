package qchess.chess.create.direction;

import qchess.chess.create.Coordinate;
import qchess.chess.logic.ChessBoard;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Quentin Smith
 * <br>
 * This vector is <b>not restrained</b> by the sides of the board.
 * This vector also is <b>not</b> continuous.
 */
public class UnboundedPieceVector extends PieceVector {
    public UnboundedPieceVector(Coordinate start, int deltaRow, int deltaCol, int magnitude) {
        super(start, deltaRow, deltaCol, magnitude);

        int deltaBtnID = Coordinate.getBtnID(deltaRow, deltaCol);
        int btnID = start.getBtnID() + deltaBtnID;

        int m = magnitude;

        int pastBtnID = start.getBtnID();

        for (; UnboundedPieceVector.isBtnIDOnePlaceRemovedFromOutOfBounds(btnID) && m > 0; btnID += deltaBtnID, m--) {
            if ((pastBtnID % 8 != (btnID % 8) + 1 && pastBtnID % 8 != (btnID % 8) - 1)) {
                int totalBtnIDs = ChessBoard.width * ChessBoard.height;
                int startOfLastRowBtnID = ChessBoard.width * ChessBoard.height - ChessBoard.width;
                boolean btnIDIsOnLastRow = btnID > startOfLastRowBtnID && btnID < totalBtnIDs;

                if (btnID % ChessBoard.height == 7) {
                    btnID += 8;
                } else if (btnID < ChessBoard.width && btnID >= 0) {
                    btnID -= 1;
                } else if (btnIDIsOnLastRow) {
                    btnID += 1;
                } else if (btnID % ChessBoard.height == 0) {
                    btnID -= 8;
                } // else
            } // if

            if (Coordinate.isBtnIDInBounds(btnID)) {
                coordinates.add(new Coordinate(btnID));
            }
            pastBtnID = btnID;
        } // for

    }

    /**
     * @param btnID btn id to be checked.
     * @return true if the btnID is one place removed from out of bounds and false otherwise.
     * For a btn id to be 1 place removed from out of bounds, it must be either -1 or chessboard height * width.
     */
    private static boolean isBtnIDOnePlaceRemovedFromOutOfBounds(int btnID) {
        return btnID >= -1 && btnID <= ChessBoard.width * ChessBoard.height;
    }


    /** {@inheritDoc} */
    @Override
    public UnboundedPieceVector inverse() {
        int reversedDeltaRow = deltaRow * -1;
        int reversedDeltaCol = deltaCol * -1;
        return new UnboundedPieceVector(start, reversedDeltaRow, reversedDeltaCol, magnitude);
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> verticalReflection() {
        ArrayList<ChessDirection> result = new ArrayList<>();

        UnboundedPieceVector vector = new UnboundedPieceVector(start, deltaRow, -deltaCol, this.magnitude);

        result.add(vector);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public List<ChessDirection> horizontalReflection() {
        ArrayList<ChessDirection> result = new ArrayList<>();

        UnboundedPieceVector vector = new UnboundedPieceVector(this.start, -deltaRow, deltaCol, this.magnitude);

        result.add(vector);
        return result;
    }
}
