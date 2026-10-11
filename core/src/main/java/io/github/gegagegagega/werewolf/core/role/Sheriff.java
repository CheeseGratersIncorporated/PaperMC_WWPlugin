package io.github.gegagegagega.werewolf.core.role;

import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.core.night.ActionPriority;
import io.github.gegagegagega.werewolf.core.night.Night;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Sheriff extends Role {
    @Override
    public String id() {
        return "sheriff";
    }

    @Override
    public Component title() {
        return Component.text("SHERIFF", NamedTextColor.GREEN);
    }

    @Override
    public Component subtitle() {
        return Component.text("Investigate others for signs of tomfoolery", NamedTextColor.GRAY);
    }

    @Override
    public String description() {
        return "SHERIFF. Use /ww target [player] to investigate a player. You will be told whether or not they are a werewolf.";
    }

    @Override
    public void onNight(Night night) {
        if (!player.isAlive() || player.getTarget() == null) return;
        night.schedule(ActionPriority.ABSENCE, () -> player.setAbsent(true));
        night.schedule(ActionPriority.SHERIFF_INVESTIGATE, () -> {
            WWPlayer target = player.getTarget();
            if (target == null) return;
            night.tell(player, target.isWerewolf()
                    ? Component.text("Your target is a sussy baka", NamedTextColor.AQUA)
                    : Component.text("Your target is not a sussy baka", NamedTextColor.AQUA));
        });
    }

    @Override
    public boolean canTarget(Game game, WWPlayer target) {
        if (!player.isAlive()) return false;
        if (target == null) return true;
        if (!target.isAlive()) return false;
        return !isSelf(target);
    }

    @Override
    public Component targetResponse(Game game, WWPlayer target) {
        if (!player.isAlive()) return Component.text("你死了", NamedTextColor.RED);
        if (target == null) return Component.text("You decided not to investigate anyone", NamedTextColor.GREEN);
        if (isSelf(target)) return Component.text("You realize that you are in fact NOT a sussy baka and stop wasting your time.", NamedTextColor.RED);
        if (!target.isAlive()) return Component.text("You feel that investigating " + target.getName() + " will come up as cold as their corpse.", NamedTextColor.RED);
        return Component.text("You decided to investigate " + target.getName(), NamedTextColor.GREEN);
    }
}
