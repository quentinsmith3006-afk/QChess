package qchess.chess.logic.event;

public interface ChessEventHandler <T extends ChessEvent> {
    public void handleChessEvent(T event) ;
}
