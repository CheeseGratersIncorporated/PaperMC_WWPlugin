package io.github.gegagegagega.werewolf.core;

import java.util.List;

import net.kyori.adventure.text.Component;

/**
 * How the game core talks to the outside world. The core only changes state
 * and calls these hooks; the Minecraft layer decides what each one looks like
 * (titles, teleports, sounds) and then reconciles every player with the new
 * state. All methods default to doing nothing so tests can implement just
 * what they need.
 */
public interface GameListener {

    GameListener NONE = new GameListener() {
    };

    /** Something about the game changed; re-sync whatever depends on state. */
    default void onStateChanged() {
    }

    /** Roles were assigned and the first day begins. */
    default void onGameStarted() {
    }

    /** A new phase began. Entry logic for the phase has already run. */
    default void onPhaseStarted(GamePhase phase) {
    }

    /** A player was marked dead. Their body may still be alive in Minecraft. */
    default void onPlayerKilled(WWPlayer player) {
    }

    /** The accused was found guilty. Fires after {@link #onPhaseStarted} for {@link GamePhase#SENTENCE}. */
    default void onLynched(WWPlayer player) {
    }

    /** The game is over. {@code results} are the lines to announce, in order. */
    default void onGameEnded(List<Component> results) {
    }

    /** A private message for one player. */
    default void tell(WWPlayer player, Component message) {
    }

    /** A chat message for everyone. */
    default void broadcast(Component message) {
    }
}
