package io.github.gegagegagega.werewolf.core.role;

import io.github.gegagegagega.werewolf.core.Team;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/** Given to anyone who was not in the game when it started. Not in the {@link Roles} registry. */
public class Spectator extends Role {
    @Override
    public String id() {
        return "spectator";
    }

    @Override
    public Team team() {
        return Team.SPECTATOR;
    }

    @Override
    public Component title() {
        return Component.text("SPECTATOR", NamedTextColor.GRAY);
    }

    @Override
    public Component subtitle() {
        return Component.text("Watch the game unfold.", NamedTextColor.GRAY);
    }

    @Override
    public String description() {
        return "spectator";
    }
}
