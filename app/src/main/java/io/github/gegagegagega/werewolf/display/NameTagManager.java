package io.github.gegagegagega.werewolf.display;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public final class NameTagManager {

    private static final String TEAM_NAME = "hidden_nametags";

    private final Team hiddenTeam;

    public NameTagManager() {
        Scoreboard scoreboard =
                Bukkit.getScoreboardManager().getMainScoreboard();

        Team existing = scoreboard.getTeam(TEAM_NAME);

        hiddenTeam = existing != null
                ? existing
                : scoreboard.registerNewTeam(TEAM_NAME);

        hiddenTeam.setOption(
                Team.Option.NAME_TAG_VISIBILITY,
                Team.OptionStatus.NEVER
        );
        hiddenTeam.setOption(
    Team.Option.COLLISION_RULE,
    Team.OptionStatus.NEVER
);
    }

    public void hideNameTag(Player player) {
        hiddenTeam.addEntry(player.getName());
    }

    public void showNameTag(Player player) {
        hiddenTeam.removeEntry(player.getName());
    }

    public boolean isNameTagHidden(Player player) {
        return hiddenTeam.hasEntry(player.getName());
    }
}