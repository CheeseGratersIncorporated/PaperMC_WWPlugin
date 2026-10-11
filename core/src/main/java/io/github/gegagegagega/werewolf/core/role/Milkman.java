package io.github.gegagegagega.werewolf.core.role;

import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.core.night.ActionPriority;
import io.github.gegagegagega.werewolf.core.night.Night;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Milkman extends Role {
    @Override
    public String id() {
        return "milkman";
    }

    @Override
    public Component title() {
        return Component.text("MILKMAN", NamedTextColor.GREEN);
    }

    @Override
    public Component subtitle() {
        return Component.text("Deliver morning milk and peep through windows", NamedTextColor.GRAY);
    }

    @Override
    public String description() {
        return "MILKMAN. Use /ww target [player] to choose a player to deliver milk to. You will be told whether or not they were home last night(Werewolves are always absent, Acting Town roles are absent if they targeted someone that wasn't themselves that night).";
    }

    @Override
    public void onNight(Night night) {
        if (!player.isAlive() || player.getTarget() == null) return;
        night.schedule(ActionPriority.ABSENCE, () -> player.setAbsent(true));
        night.schedule(ActionPriority.MILKMAN_DELIVER, () -> {
            WWPlayer target = player.getTarget();
            if (target == null) return;
            night.tell(player, target.wasAbsent()
                    ? Component.text("Your target was absent from their home", NamedTextColor.AQUA)
                    : Component.text("Your target was not absent from their home", NamedTextColor.AQUA));
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
        if (target == null) return Component.text("You decided not to deliver anything", NamedTextColor.GREEN);
        if (isSelf(target)) return Component.text("You have all the milk you need!", NamedTextColor.RED);
        if (!target.isAlive()) return Component.text("You feel that " + target.getName() + " might not need milk for a very long time", NamedTextColor.RED);
        return Component.text("You decided to deliver milk to " + target.getName(), NamedTextColor.GREEN);
    }
}
