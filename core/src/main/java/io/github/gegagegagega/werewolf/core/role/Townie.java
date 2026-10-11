package io.github.gegagegagega.werewolf.core.role;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class Townie extends Role {
    @Override
    public String id() {
        return "townie";
    }

    @Override
    public Component title() {
        return Component.text("TOWNIE", NamedTextColor.GREEN);
    }

    @Override
    public Component subtitle() {
        return Component.text("Vote out the werewolves and survive.", NamedTextColor.GRAY);
    }

    @Override
    public String description() {
        return "TOWNIE. You do nothing L. Vote out the werewolves ig.";
    }
}
