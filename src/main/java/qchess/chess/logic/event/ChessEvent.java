package qchess.chess.logic.event;

import qchess.chess.logic.ChessBoard;

public class ChessEvent {
    public ChessEventHandler<ChessEvent> chessEventHandler;

    public ChessEvent() {

    }

    public static void fireChessEvent(ChessBoard chessBoard) {

    }
}
