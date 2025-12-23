package qchess.chess.logic;

import qchess.chess.create.ChessPiece;
import qchess.chess.create.annotations.HorizonalSymmetry;
import qchess.chess.create.annotations.VerticalSymmetry;
import qchess.chess.create.direction.ChessDirection;
import qchess.chess.create.direction.PieceVector;
import qchess.chess.create.interfaces.SymmetryOperation;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

public record ChessAnnotation(Class<? extends Annotation> annotate, SymmetryOperation<ChessDirection> operation) {
    static ArrayList<ChessAnnotation> chessAnnotations;

    void applyAnnotation(ChessPiece chessPiece, List<ChessDirection> playableMoves) {
        Class<? extends ChessPiece> chessPieceClass = chessPiece.getClass();
        Annotation[] annotations = chessPieceClass.getAnnotations();
        Annotation anno = chessPiece.getClass().getAnnotation(annotate);

        for (Annotation annotation : annotations) {
            if (anno.equals(annotation)) {
                this.operation.operate(playableMoves);
            }
        }
    }

    static List<ChessDirection> applyAnnotations(ChessPiece chessPiece) {
        List<ChessDirection> playableMoves = chessPiece.getPlayableMoves();

        for (ChessAnnotation annotation : chessAnnotations) {
            annotation.applyAnnotation(chessPiece, playableMoves);
        }

        return playableMoves;
    }

    static void chessAnnotationsInit() {
        chessAnnotations = new ArrayList<ChessAnnotation>();

        SymmetryOperation<ChessDirection> horOperation = (List<ChessDirection> playableMoves) -> {
            System.out.println("chessAnnotationsInit");
        };
        ChessAnnotation horizontalSym = new ChessAnnotation(HorizonalSymmetry.class, horOperation);

        SymmetryOperation<ChessDirection> verOperation = (List<ChessDirection> playableMoves) -> {
            System.out.println("chessAnnotationsInit");
        };
        ChessAnnotation verticalSym = new ChessAnnotation(VerticalSymmetry.class, verOperation);

        addAllAnnotations(horizontalSym, verticalSym);
    }

    public static void addAnnotation(ChessAnnotation chessAnnotation) {
        chessAnnotations.add(chessAnnotation);
    }

    public static void addAllAnnotations(ChessAnnotation... chessAnnotation) {
        for (ChessAnnotation annotation : chessAnnotations) {
            addAnnotation(annotation);
        }
    }

    public static boolean addChessAnnotation(ChessAnnotation chessAnnotation) {
        if (chessAnnotations == null) {
            chessAnnotations = new ArrayList<ChessAnnotation>();
        }
        return chessAnnotations.add(chessAnnotation);
    }

    public static boolean hasAnnotation(Class<? extends ChessPiece> clazz, Class<? extends Annotation> annotation) {
        return clazz.isAnnotationPresent(annotation);
    }
}
