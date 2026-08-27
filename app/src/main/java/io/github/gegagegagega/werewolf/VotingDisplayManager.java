package io.github.gegagegagega.werewolf;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.ScoreboardManager;

import java.util.Objects;

public class VotingDisplayManager {

    private static final String OBJECTIVE_NAME = "votes";

    private final Scoreboard scoreboard;
    private final Objective votesObjective;

    public VotingDisplayManager() {
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

        show();
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
            score.resetScore();
            return;
        }

        score.setScore(votes);
    }

    /**
     * Displays the voting objective in the sidebar.
     */
    public void show() {
        votesObjective.setDisplaySlot(DisplaySlot.SIDEBAR);
    }

    /**
     * Hides the voting objective without deleting its scores.
     */
    public void hide() {
        votesObjective.setDisplaySlot(null);
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
