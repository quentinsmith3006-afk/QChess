package qchess.chess.create;

/**
 * @author Quentin Smith
 *
 * Enum for specifing how to apply reflection annotations:
 * CAPTURE -> Only apply annotations on capturables.
 * PLAYABLES -> Only apply annotations on playables.
 * BOTH -> apply annotations on both
 */
public enum SpecificReflection {
    CAPTURE,
    PLAYABLES,
    BOTH
}
