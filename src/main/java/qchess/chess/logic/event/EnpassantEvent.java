package qchess.chess.logic.event;

import javafx.event.EventType;
import qchess.chess.create.ChessPiece;

/*
Despite my wishes, I feel as though it is better to program enpassant in such a way that would make it
impossible to add without this event. Ideally the user could program enpassant functionality inside the
pawn structure, but they are simply not given enough information.

A possible solution:

Allow a user
 */
public class EnpassantEvent extends ChessEvent {
    public final static EventType<ChessEvent> ENPASSANT = new EventType<>(ChessEvent.ANY, "ENPASSANT");

    public EnpassantEvent(ChessPiece instigator) {
        super(ENPASSANT, instigator);
    }
}
