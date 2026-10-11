package io.github.gegagegagega.werewolf.core.view;

import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.GamePhase;
import io.github.gegagegagega.werewolf.core.WWPlayer;

/**
 * The rules for what players see. Pure functions of game state, so they are
 * unit tested and shared by phase changes and rejoins alike.
 */
public final class Views {
    private Views() {
    }

    /** How {@code player} should look right now. */
    public static PlayerView of(Game game, WWPlayer player) {
        if (!game.isActive() || player == null) {
            return PlayerView.LOBBY;
        }
        GamePhase phase = game.getPhase();
        boolean ghost = player.isGhost();
        boolean accused = player == game.getAccused();
        boolean trial = phase != null && phase.isTrial();
        boolean sentence = phase == GamePhase.SENTENCE;

        boolean nameTag = player.isAlive() && (phase == null
                || phase == GamePhase.FIRST_DAY
                || phase == GamePhase.DISCUSSION
                || phase == GamePhase.VOTING
                || (trial && accused));

        return new PlayerView(
                ghost,
                ghost || sentence,
                !(ghost || sentence || (trial && !accused)),
                nameTag,
                game.canJudge(player),
                player.getJudgment(),
                phase == GamePhase.POST_NIGHT && player.isAlive() && player.isWerewolf()
                        && game.getWolfVictim() != null,
                !player.isAlive() && player.isDeathRevealed()
        );
    }

    /**
     * Whether {@code viewer} should see {@code target}'s body.
     *
     * @param viewer        the viewing participant, or null if they aren't in the game
     * @param wolvesHaveDen whether a wolf den location exists (wolves leave town at night)
     */
    public static boolean canSee(Game game, WWPlayer viewer, WWPlayer target, boolean wolvesHaveDen) {
        if (!game.isActive() || target == null || viewer == target) {
            return true;
        }
        if (target.isGhost()) {
            return false;
        }
        GamePhase phase = game.getPhase();
        if (phase == GamePhase.SENTENCE) {
            return false;
        }
        if (phase != null && phase.isNight()) {
            if (phase == GamePhase.POST_NIGHT && target == game.getWolfVictim()) {
                return true;
            }
            if (target.isAlive() && target.isWerewolf() && wolvesHaveDen) {
                return true;
            }
            // Townsfolk are shut in their houses; only the dead watch them.
            return viewer == null || !viewer.isAlive();
        }
        return true;
    }

    /**
     * Whether a house's doors should be open. Returns null outside a game,
     * meaning "leave them as they are".
     *
     * @param owner the house owner, or null if nobody claimed it
     */
    public static Boolean doorsOpen(Game game, WWPlayer owner) {
        if (!game.isActive()) {
            return null;
        }
        if (owner == null) {
            return false;
        }
        GamePhase phase = game.getPhase();
        if (phase == GamePhase.FIRST_DAY || phase == GamePhase.DUSK || phase == GamePhase.DAWN) {
            return true;
        }
        // Houses of the dead stay open so the town can read their will.
        return !owner.isAlive();
    }

    /** Whether the vote count sidebar is shown. */
    public static boolean votingBoardVisible(Game game) {
        return game.canVote();
    }
}
