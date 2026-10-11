package io.github.gegagegagega.werewolf.core;

/**
 * Who a player's chat messages reach. Computed from game state by
 * {@link Game#chatChannel(WWPlayer)}; the Minecraft layer only applies it.
 */
public enum ChatChannel {
    /** Normal chat, everyone sees it. */
    PUBLIC,
    /** Nobody sees it (sleeping townsfolk, the audience during a trial). */
    MUTED,
    /** Only dead players and spectators see it. */
    DEAD,
    /** Only werewolves see it. */
    WEREWOLVES
}
