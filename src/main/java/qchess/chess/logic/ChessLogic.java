package qchess.chess.logic;

public abstract class ChessLogic {
    protected ChessBoard chessBoard;

    public ChessLogic(ChessBoard chessBoard) {
        this.chessBoard = chessBoard;
    }
}
