package qchess.chess.create.interfaces;

/**
 * @author Quentin Smith
 *
 * This interface changes <i>how</i> the chess piece is treated within {@link qchess.chess.logic.MoveLogic}.
 *
 * Checkables have several properties:
 * <ul>
 *     <li>cannot move through attacked squares</li>
 *     <li>When checked, they stop all other pieces from moving</li>
 *     <li>Can be checkmated</li>
 *     <li>
 *         <p>If there is 2 checkables then
 *            When in check, either checkable can move.
 *            Checkmate can only occur when there are no moves left
 *          </p>
 *     </li>
 * </ul>
 */
public interface Checkable {
}
