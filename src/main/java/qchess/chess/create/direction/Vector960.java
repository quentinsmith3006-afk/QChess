package qchess.chess.create.direction;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;

import java.util.ArrayList;
import java.util.List;

/**
 *  NEW FEATURE
 */
public class Vector960 extends CastleVector {

    public Vector960(Coordinate start, ChessPiece dependent, Coordinate kingTo, Coordinate dependentTo) {
        super(start, getDeltaRow(start, dependent), getDeltaCol(start, dependent), getMagnitude(start, dependent));

        this.kingTo = kingTo;
        this.dependentTo = dependentTo;

        this.setCastleDependent(dependent);
    }

    public Vector960(Coordinate start, ChessPiece dependent) {
        super(start, getDeltaRow(start, dependent), getDeltaCol(start, dependent), getMagnitude(start, dependent));

        this.setCastleDependent(dependent);
    }

    static int getMagnitude(Coordinate start, ChessPiece dependent) {
        int deltaRow = Math.abs(start.getRow() - dependent.getRow());
        int deltaCol = Math.abs(start.getCol() - dependent.getCol());

        return (int) Math.ceil(Math.sqrt(Math.pow(deltaRow, 2) + Math.pow(deltaCol, 2)));
    }

    static int getDeltaRow(Coordinate start, ChessPiece dependent) {
        int deltaRow = dependent.getRow() - start.getRow();
        int deltaCol = dependent.getCol() - start.getCol();


        return simplify(deltaRow, deltaCol).getFirst();
    }

    static int getDeltaCol(Coordinate start, ChessPiece dependent) {
        int deltaRow = dependent.getRow() - start.getRow();
        int deltaCol = dependent.getCol() - start.getCol();

        return simplify(deltaRow, deltaCol).get(1);
    }

    static List<Integer> simplify(int r, int c) {
        ArrayList<Integer> result = new ArrayList<>();

        if (r == 0) {
            result.add(r);
            result.add(c < 0 ? -1 : 1);
            return result;
        } else if (c == 0) {
            result.add(r < 0 ? -1 : 1);
            result.add(c);
            return result;
        }

        while ((r / c) > 1) {
            if (r > c) {
                r -= c;
            } else if (r < c) {
                c -= r;
            } else {
                break;
            }
        }

        result.add(r);
        result.add(c);
        return result;
    }
}
