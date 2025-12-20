package qchess.chess.logic;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.Coordinate;
import qchess.chess.create.annotations.HorizonalSymmetry;
import qchess.chess.create.interfaces.Operable;
import qchess.chess.create.interfaces.SymmetryOperation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public class ChessAnnotation {
    final SymmetryOperation<Coordinate> operation;
    final Class<? extends Annotation> annotate;
    static ArrayList<ChessAnnotation> chessAnnotations;

    public ChessAnnotation(Class<? extends Annotation> annotate, SymmetryOperation<Coordinate> operation) {
        this.operation = operation;
        this.annotate = annotate;
    }

    static void chessAnnotationsInit() {
        chessAnnotations = new ArrayList<ChessAnnotation>();

        SymmetryOperation<Coordinate> operation = (Coordinate focal, List<Coordinate> playableMoves) -> {
            System.out.println("chessAnnotationsInit");
        };
        ChessAnnotation horizontalSym = new ChessAnnotation(HorizonalSymmetry.class, operation);

        chessAnnotations.add(horizontalSym);
    }

    public void applyAnnotation(ChessPiece chessPiece, List<Coordinate> playableMoves) {
        Class<? extends ChessPiece> chessPieceClass = chessPiece.getClass();
        Annotation[] annotations = chessPieceClass.getAnnotations();
        Annotation anno = chessPiece.getClass().getAnnotation(annotate);

        for (Annotation annotation : annotations) {
            if (anno.equals(annotation)) {
                this.operation.operate(chessPiece.getCoordinate(), playableMoves);
            }
        }
    }

    public static void addAnnotation(ChessAnnotation chessAnnotation) {
        chessAnnotations.add(chessAnnotation);
    }

    static List<Coordinate> applyAnnotations(ChessPiece chessPiece, ArrayList<ChessAnnotation> annotations) {
        List<Coordinate> playableMoves = chessPiece.getPlayableMoves();

        for (ChessAnnotation annotation : annotations) {
            annotation.applyAnnotation(chessPiece, playableMoves);
        }

        return playableMoves;
    }
}
