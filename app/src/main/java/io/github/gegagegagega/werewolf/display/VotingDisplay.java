package io.github.gegagegagega.werewolf.display;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.Map;
import java.util.Objects;

public class VotingDisplay {

    private static final String OBJECTIVE_NAME = "votes";

    private final Scoreboard scoreboard;
    private final Objective votesObjective;

    public VotingDisplay() {
        ScoreboardManager scoreboardManager = Objects.requireNonNull(
                Bukkit.getScoreboardManager(),
                "Scoreboard manager is unavailable"
        );

        scoreboard = scoreboardManager.getMainScoreboard();

        Objective existingObjective =
                scoreboard.getObjective(OBJECTIVE_NAME);

        if (existingObjective != null) {
            votesObjective = existingObjective;
        } else {
            votesObjective = scoreboard.registerNewObjective(
                    OBJECTIVE_NAME,
                    Criteria.DUMMY,
                    Component.text("Votes", NamedTextColor.GOLD)
            );
        }

        hide();
    }

    /** Shows or hides the sidebar and sets every listed player's vote count. */
    public void sync(boolean visible, Map<String, Integer> votes) {
        votes.forEach(this::setPlayer);
        if (visible) {
            show();
        } else {
            hide();
        }
    }

    /**
     * Sets the number of votes displayed beside a player's name.
     * A vote count of zero removes the player from the display.
     */
    public void setPlayer(String name, int votes) {
        Objects.requireNonNull(name, "name cannot be null");

        // if (votes < 0) {
        //     throw new IllegalArgumentException(
        //             "votes cannot be negative"
        //     );
        // }

        Score score = votesObjective.getScore(name);

        if (votes == 0) {
            if (score.isScoreSet()) {
                score.resetScore();
            }
            return;
        }

        if (!score.isScoreSet() || score.getScore() != votes) {
            score.setScore(votes);
        }
    }

    /**
     * Displays the voting objective in the sidebar.
     */
    public void show() {
        if (votesObjective.getDisplaySlot() != DisplaySlot.SIDEBAR) {
            votesObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
        }
    }

    /**
     * Hides the voting objective without deleting its scores.
     */
    public void hide() {
        if (votesObjective.getDisplaySlot() != null) {
            votesObjective.setDisplaySlot(null);
        }
    }

    /**
     * Removes every score belonging to the voting objective.
     */
    public void clear() {
        for (String entry : scoreboard.getEntries()) {
            Score score = votesObjective.getScore(entry);

            if (score.isScoreSet()) {
                score.resetScore();
            }
        }
    }
}
