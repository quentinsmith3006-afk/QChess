package qchess.chess.create;

import org.jetbrains.annotations.ApiStatus;

/**
 * @author Quentin Smith
 *
 * Enum for the type of moves.
 * Each moves collection has one of 2 types, either specify capture or playables.
 *
 * If a chess piece is NOT an instance of specify capture, then playables are assumed to contain both.
 * If captures are specified then this becomes nessesary for <b>internal</b> clarity.
 */
@ApiStatus.Internal
public enum MovesType {
    SPECIFY_CAPTURE,
    PLAYABLES
}
