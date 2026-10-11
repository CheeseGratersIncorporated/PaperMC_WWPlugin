package io.github.gegagegagega.werewolf.core;

/** Which side a role is counted on for win conditions. */
public enum Team {
    TOWN,
    WEREWOLF,
    /** Plays for itself (the fool). Counts as a non-werewolf for the wolves' win check. */
    NEUTRAL,
    SPECTATOR
}
