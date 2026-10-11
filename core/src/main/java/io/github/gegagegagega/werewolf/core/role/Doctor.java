package io.github.gegagegagega.werewolf.core.role;

import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.core.night.ActionPriority;
import io.github.gegagegagega.werewolf.core.night.Night;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Doctor extends Role {
    private int selfHeals = 1;

    @Override
    public String id() {
        return "doctor";
    }

    @Override
    public Component title() {
        return Component.text("DOCTOR", NamedTextColor.GREEN);
    }

    @Override
    public Component subtitle() {
        return Component.text("Save someone from the wolves every night", NamedTextColor.GRAY);
    }

    @Override
    public String description() {
        return "DOCTOR. Use /ww target [player] to choose a player to heal. If that player is attacked by the werewolves, they will survive the night. You have one self heal per game.";
    }

    public int selfHealsLeft() {
        return selfHeals;
    }

    @Override
    public void onNight(Night night) {
        if (!player.isAlive() || player.getTarget() == null) return;
        if (isSelf(player.getTarget())) {
            if (selfHeals <= 0) return;
            selfHeals--;
        } else {
            night.schedule(ActionPriority.ABSENCE, () -> player.setAbsent(true));
        }
        night.schedule(ActionPriority.DOCTOR_PROTECT, () -> {
            if (player.getTarget() != null) {
                player.getTarget().setDoctored(true);
            }
        });
        night.schedule(ActionPriority.DOCTOR_REPORT, () -> {
            WWPlayer target = player.getTarget();
            if (target == null) return;
            target.setDoctored(false);
            if (target.wasAttacked()) {
                night.tell(player, Component.text("Your target was attacked!", NamedTextColor.GREEN));
            }
        });
    }

    @Override
    public boolean canTarget(Game game, WWPlayer target) {
        if (!player.isAlive()) return false;
        if (target == null) return true;
        if (!target.isAlive()) return false;
        return !(isSelf(target) && selfHeals <= 0);
    }

    @Override
    public Component targetResponse(Game game, WWPlayer target) {
        if (!player.isAlive()) return Component.text("你死了", NamedTextColor.RED);
        if (target == null) return Component.text("You decided not to heal anyone", NamedTextColor.GREEN);
        if (isSelf(target)) {
            if (selfHeals > 0) return Component.text("You decide to hole up at home and heal yourself tonight", NamedTextColor.GREEN);
            return Component.text("A sudden sense of moral duty surges through your body. Seems you can't keep being selfish and healing yourself.", NamedTextColor.RED);
        }
        if (!target.isAlive()) return Component.text("Trying to treat a corpse may not be the brightest of ideas.", NamedTextColor.RED);
        return Component.text("You decided to heal " + target.getName(), NamedTextColor.GREEN);
    }
}
