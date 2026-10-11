package io.github.gegagegagega.werewolf.core.view;

import io.github.gegagegagega.werewolf.core.Judgment;

/**
 * Everything about how one player should currently look and behave in
 * Minecraft, derived purely from game state by {@link Views#of}. The
 * Minecraft layer applies this; it never decides these things itself.
 *
 * @param ghost           dead and physically killed: flies, can't be hurt, makes no sound, picks nothing up
 * @param invisible       has the invisibility flag
 * @param collidable      other players bump into them
 * @param nameTagVisible  name tag shows above their head
 * @param judgmentButtons holds the innocent/guilty buttons
 * @param judgment        which button is highlighted
 * @param wolfDisguise    wears the werewolf skin
 * @param shownAsDead     greyed out in the tab list
 */
public record PlayerView(
        boolean ghost,
        boolean invisible,
        boolean collidable,
        boolean nameTagVisible,
        boolean judgmentButtons,
        Judgment judgment,
        boolean wolfDisguise,
        boolean shownAsDead
) {
    /** How everyone looks outside a game. */
    public static final PlayerView LOBBY =
            new PlayerView(false, false, true, true, false, Judgment.ABSTAIN, false, false);
}
