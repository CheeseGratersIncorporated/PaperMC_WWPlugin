package io.github.gegagegagega.werewolf.core.role;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.Judgment;
import io.github.gegagegagega.werewolf.core.Team;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.core.night.ActionPriority;
import io.github.gegagegagega.werewolf.core.night.Night;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * The town fool (jester). Wins by getting executed, then haunts one player who
 * did not vote innocent that night. If they don't pick, a random eligible
 * player is chosen.
 */
public class Fool extends Role {
    private int kills = 1;

    @Override
    public String id() {
        return "fool";
    }

    @Override
    public Team team() {
        return Team.NEUTRAL;
    }

    @Override
    public boolean winsWhenLynched() {
        return true;
    }

    @Override
    public Component title() {
        return Component.text("TOWN FOOL", NamedTextColor.LIGHT_PURPLE);
    }

    @Override
    public Component subtitle() {
        return Component.text("Get yourself executed by the town", NamedTextColor.GRAY);
    }

    @Override
    public String description() {
        return "TOWN FOOL. Get yourself executed in front of the town. You have no abilities.";
    }

    @Override
    public void onNight(Night night) {
        if (!player.wasLynched() || kills <= 0) return;
        Game game = night.game();
        if (player.getTarget() == null) {
            List<WWPlayer> candidates = new ArrayList<>(game.getPlayers());
            Collections.shuffle(candidates, game.random());
            for (WWPlayer candidate : candidates) {
                if (canTarget(game, candidate)) {
                    player.setTarget(candidate);
                    break;
                }
            }
        }
        if (player.getTarget() == null) {
            game.listener().broadcast(Component.text("funny jester error just occurred and im sad :(", NamedTextColor.RED));
            return;
        }
        night.schedule(ActionPriority.FOOL_HAUNT, () -> {
            WWPlayer target = player.getTarget();
            if (target == null) return;
            target.addCauseOfDeath(Component.text("died of guilt", NamedTextColor.RED));
            game.kill(target);
            kills--;
        });
    }

    @Override
    public boolean canTarget(Game game, WWPlayer target) {
        if (!player.wasLynched()) return false;
        if (kills <= 0) return false;
        if (player.isAlive()) return false;
        if (target == null) return false;
        if (!target.isAlive()) return false;
        return target.getJudgment() != Judgment.INNOCENT;
    }

    @Override
    public Component targetResponse(Game game, WWPlayer target) {
        if (!player.wasLynched()) return Component.text("get lynched", NamedTextColor.RED);
        if (kills <= 0) return Component.text("you used your kill already", NamedTextColor.RED);
        if (player.isAlive()) return Component.text("you're alive and also if you're getting this msg smth went wrong", NamedTextColor.RED);
        if (target == null) return Component.text("choose someone.", NamedTextColor.GREEN);
        if (!target.isAlive()) return Component.text(target.getName() + " is already dead.", NamedTextColor.RED);
        if (target.getJudgment() == Judgment.INNOCENT) return Component.text(target.getName() + " judged you innocent. They won't die of guilt tonight.", NamedTextColor.RED);
        return Component.text("You've decided to target " + target.getName(), NamedTextColor.GREEN);
    }
}
