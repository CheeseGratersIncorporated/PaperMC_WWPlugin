package io.github.gegagegagega.werewolf.core.night;

/**
 * Ordering of night actions. <b>Higher numbers run first.</b> Actions with the
 * same priority run in the order they were scheduled.
 */
public final class ActionPriority {
    private ActionPriority() {
    }

    public static final int ABSENCE = 1000;
    public static final int DOCTOR_PROTECT = 100;
    public static final int WEREWOLF_VOTE = 100;
    public static final int SHERIFF_INVESTIGATE = 70;
    public static final int WEREWOLVES_PICK_VICTIM = 60;
    public static final int MILKMAN_DELIVER = 15;
    public static final int DOCTOR_REPORT = 0;
    public static final int FOOL_HAUNT = 0;

    public static final int RESET_NIGHT_STATE = -9999;
}
