package qchess.chess.create.direction;

import qchess.chess.create.Coordinate;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class ChessDirection implements Iterable<Coordinate> {
    final ArrayList<Coordinate> coordinates;
    final Coordinate start;

    public ChessDirection(Coordinate start) {
        coordinates = new ArrayList<>();
        this.start = start;
    }

    void sort(List<Coordinate> coords, Coordinate focal) {
        for (int i = 0; i < coords.size(); i++) {
            int smallestCoordIndex = i;
            for (int j = i + 1; j < coords.size(); j++) {
                if (distance(coords.get(smallestCoordIndex)) > distance(coords.get(j))) {
                    smallestCoordIndex = j;
                }
            }

            Coordinate temp = coords.get(i);
            coords.set(i, coords.get(smallestCoordIndex));
            coords.set(smallestCoordIndex, temp);
        }
    }

    public int distance(Coordinate other) {
        int originalRow = start.getRow();
        int originalCol = start.getCol();
        int otherRow = other.getRow();
        int otherCol = other.getCol();
        return Math.abs(originalRow - otherRow) + Math.abs(originalCol - otherCol);
    }

    public int getSize() {
        return coordinates.size();
    }

    @Override
    public Iterator<Coordinate> iterator() {
        return coordinates.iterator();
    }
}
