package io.github.gegagegagega.werewolf.core.role;

import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.Team;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.core.night.Night;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Base class for every role. A role only reads and changes game state; it
 * never touches Minecraft. To add a role, subclass this and register it in
 * {@link Roles}.
 */
public abstract class Role {

    /** The player holding this role. Set when the role is assigned. */
    protected WWPlayer player;

    public final void bind(WWPlayer player) {
        this.player = player;
    }

    public WWPlayer player() {
        return player;
    }

    /** Registry id, used in commands such as {@code /ww roles set <id> <count>}. */
    public abstract String id();

    public abstract Component title();

    public abstract Component subtitle();

    /** Longer explanation shown by {@code /ww role}. */
    public abstract String description();

    public Team team() {
        return Team.TOWN;
    }

    /** Attacks are blocked when defense is 1 or more. */
    public int defense() {
        return 0;
    }

    /** True for roles that win by being executed (the fool). */
    public boolean winsWhenLynched() {
        return false;
    }

    /** Whether {@code target} is a valid choice for {@code /ww target}. A null target means "nobody". */
    public boolean canTarget(Game game, WWPlayer target) {
        return false;
    }

    /** Feedback shown after {@code /ww target}. */
    public Component targetResponse(Game game, WWPlayer target) {
        return Component.text("You have no ability", NamedTextColor.RED);
    }

    /** Called when dusk falls, before the night. */
    public void onDusk(Game game) {
    }

    /** Queue this role's night actions. Called for every player, dead or alive. */
    public void onNight(Night night) {
    }

    protected boolean isSelf(WWPlayer target) {
        return target != null && target == player;
    }
}
