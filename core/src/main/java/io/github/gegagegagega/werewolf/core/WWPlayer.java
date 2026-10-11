package io.github.gegagegagega.werewolf.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.github.gegagegagega.werewolf.core.role.Role;
import net.kyori.adventure.text.Component;

/**
 * One participant in the werewolf game. Pure game state: nothing in here
 * touches Minecraft. Players are identified by their Minecraft name.
 */
public final class WWPlayer {

    private final String name;
    private Role role;

    private boolean alive = true;
    /**
     * Set by the Minecraft layer once the player's character has actually died
     * and turned into a ghost. A player can be dead in the game ({@link #isAlive()}
     * is false) while their body is still walking around, e.g. the werewolves'
     * victim during {@link GamePhase#POST_NIGHT} or the accused on the stage.
     */
    private boolean ghost = false;
    /** Whether the town knows this player is dead (greyed out in the tab list). */
    private boolean deathRevealed = false;
    private final List<Component> causesOfDeath = new ArrayList<>();

    private WWPlayer votedFor;
    private Judgment judgment = Judgment.ABSTAIN;
    private WWPlayer target;
    private boolean lynched = false;

    // Per-night flags, reset after every night resolves.
    private boolean doctored = false;
    private boolean attacked = false;
    private boolean absent = false;

    public WWPlayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public Role getRole() {
        return role;
    }

    /** Assigns a role and binds it to this player. */
    public void setRole(Role role) {
        this.role = role;
        if (role != null) {
            role.bind(this);
        }
    }

    public boolean isWerewolf() {
        return role != null && role.team() == Team.WEREWOLF;
    }

    public boolean isSpectator() {
        return role != null && role.team() == Team.SPECTATOR;
    }

    public boolean isAlive() {
        return alive;
    }

    void setAlive(boolean alive) {
        this.alive = alive;
    }

    public boolean isGhost() {
        return ghost;
    }

    public void setGhost(boolean ghost) {
        this.ghost = ghost;
    }

    public boolean isDeathRevealed() {
        return deathRevealed;
    }

    void setDeathRevealed(boolean deathRevealed) {
        this.deathRevealed = deathRevealed;
    }

    public void addCauseOfDeath(Component cause) {
        causesOfDeath.add(cause);
    }

    public List<Component> getCausesOfDeath() {
        return Collections.unmodifiableList(causesOfDeath);
    }

    public WWPlayer getVotedFor() {
        return votedFor;
    }

    void setVotedFor(WWPlayer votedFor) {
        this.votedFor = votedFor;
    }

    public Judgment getJudgment() {
        return judgment;
    }

    void setJudgment(Judgment judgment) {
        this.judgment = judgment;
    }

    public WWPlayer getTarget() {
        return target;
    }

    public void setTarget(WWPlayer target) {
        this.target = target;
    }

    public boolean wasLynched() {
        return lynched;
    }

    void setLynched(boolean lynched) {
        this.lynched = lynched;
    }

    public boolean isDoctored() {
        return doctored;
    }

    public void setDoctored(boolean doctored) {
        this.doctored = doctored;
    }

    public boolean wasAttacked() {
        return attacked;
    }

    public void setAttacked(boolean attacked) {
        this.attacked = attacked;
    }

    public boolean wasAbsent() {
        return absent;
    }

    public void setAbsent(boolean absent) {
        this.absent = absent;
    }

    void resetNightFlags() {
        target = null;
        attacked = false;
        absent = false;
        doctored = false;
    }

    @Override
    public String toString() {
        return name;
    }
}
