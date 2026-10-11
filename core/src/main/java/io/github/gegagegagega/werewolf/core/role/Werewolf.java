package io.github.gegagegagega.werewolf.core.role;

import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.Team;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.core.night.ActionPriority;
import io.github.gegagegagega.werewolf.core.night.Night;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Werewolf extends Role {
    @Override
    public String id() {
        return "werewolf";
    }

    @Override
    public Team team() {
        return Team.WEREWOLF;
    }

    @Override
    public Component title() {
        return Component.text("WEREWOLF", NamedTextColor.RED);
    }

    @Override
    public Component subtitle() {
        return Component.text("Murder >:3", NamedTextColor.GRAY);
    }

    @Override
    public String description() {
        return "WEREWOLF. At night you're teleported to the wolf den with your fellow wolves. Use /ww target [player] to choose a player to kill. The votes of your fellow wolves are counted up and the person with the most votes is killed. Tie votes are decided randomly.";
    }

    @Override
    public void onNight(Night night) {
        // Werewolves always leave home at night, even dead ones.
        night.schedule(ActionPriority.ABSENCE, () -> player.setAbsent(true));
        if (!player.isAlive() || player.getTarget() == null) {
            return;
        }
        night.schedule(ActionPriority.WEREWOLF_VOTE, () -> {
            if (player.getTarget() != null) {
                night.addWolfVote(player.getTarget());
            }
        });
    }

    @Override
    public boolean canTarget(Game game, WWPlayer target) {
        if (!player.isAlive()) return false;
        if (target == null) return true;
        if (target.isWerewolf()) return false;
        return target.isAlive();
    }

    @Override
    public Component targetResponse(Game game, WWPlayer target) {
        if (!player.isAlive()) return Component.text("你死了", NamedTextColor.RED);
        if (target == null) return Component.text("You've decided not to target anyone", NamedTextColor.GREEN);
        if (target.isWerewolf()) return Component.text("You can't target a fellow werewolf", NamedTextColor.RED);
        if (!target.isAlive()) return Component.text(target.getName() + " is already dead.", NamedTextColor.RED);
        return Component.text("You've decided to target " + target.getName(), NamedTextColor.GREEN);
    }
}
