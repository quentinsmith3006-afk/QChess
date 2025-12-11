package qchess.chess.logic.event;

import qchess.chess.logic.ChessBoard;

public class MovementEvent extends ChessEvent {

    public MovementEvent(ChessEventHandler<MovementEvent> handler) {

    }

    public MovementEvent() {
        chessEventHandler.handleChessEvent(this);
    }
}
