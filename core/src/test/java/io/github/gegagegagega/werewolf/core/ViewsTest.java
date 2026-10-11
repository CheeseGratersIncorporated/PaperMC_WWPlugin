package io.github.gegagegagega.werewolf.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Random;

import org.junit.jupiter.api.Test;

import io.github.gegagegagega.werewolf.core.role.Role;
import io.github.gegagegagega.werewolf.core.role.Townie;
import io.github.gegagegagega.werewolf.core.role.Werewolf;
import io.github.gegagegagega.werewolf.core.view.PlayerView;
import io.github.gegagegagega.werewolf.core.view.Views;

class ViewsTest {

    private final TestClock clock = new TestClock();
    private final Game game = new Game(clock, new RecordingListener(), new Random(42));

    private void startWith(Object... nameRolePairs) {
        for (int i = 0; i < nameRolePairs.length; i += 2) {
            game.addPlayer((String) nameRolePairs[i]);
        }
        game.start();
        for (int i = 0; i < nameRolePairs.length; i += 2) {
            game.getPlayer((String) nameRolePairs[i]).setRole((Role) nameRolePairs[i + 1]);
        }
    }

    private WWPlayer p(String name) {
        return game.getPlayer(name);
    }

    private void advanceTo(GamePhase phase) {
        for (int i = 0; i < 20 && game.getPhase() != phase; i++) {
            clock.finish();
        }
        assertEquals(phase, game.getPhase());
    }

    private void accuse(String name) {
        advanceTo(GamePhase.VOTING);
        for (WWPlayer voter : game.getPlayers()) {
            if (voter != p(name) && game.getPhase() == GamePhase.VOTING) {
                game.vote(voter, p(name));
            }
        }
        assertEquals(GamePhase.DEFENSE, game.getPhase());
    }

    @Test
    void lobbyLooksNormal() {
        game.addPlayer("a");
        assertEquals(PlayerView.LOBBY, Views.of(game, p("a")));
        assertNull(Views.doorsOpen(game, p("a")));
    }

    @Test
    void ghostsAreInvisibleAndHiddenFromEveryone() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        game.kill(p("b"));
        p("b").setGhost(true);

        PlayerView view = Views.of(game, p("b"));
        assertTrue(view.ghost());
        assertTrue(view.invisible());
        assertFalse(view.collidable());
        assertFalse(view.nameTagVisible());
        assertTrue(view.shownAsDead());
        assertFalse(Views.canSee(game, p("c"), p("b"), true));
    }

    @Test
    void duringTrialOnlyTheAccusedShowsTheirNameAndCollides() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        accuse("b");

        assertTrue(Views.of(game, p("b")).nameTagVisible());
        assertTrue(Views.of(game, p("b")).collidable());
        assertFalse(Views.of(game, p("c")).nameTagVisible());
        assertFalse(Views.of(game, p("c")).collidable());

        advanceTo(GamePhase.JUDGMENT);
        assertTrue(Views.of(game, p("c")).judgmentButtons());
        assertFalse(Views.of(game, p("b")).judgmentButtons());
    }

    @Test
    void sentenceHidesEveryone() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        accuse("b");
        advanceTo(GamePhase.JUDGMENT);
        game.judge(p("a"), Judgment.GUILTY);
        game.judge(p("c"), Judgment.GUILTY);
        clock.finish();

        assertEquals(GamePhase.SENTENCE, game.getPhase());
        assertTrue(Views.of(game, p("c")).invisible());
        assertFalse(Views.canSee(game, p("c"), p("d"), true));
    }

    @Test
    void atNightTownsfolkAreHiddenFromTheLivingButWolvesAreSeen() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        advanceTo(GamePhase.NIGHT);

        assertFalse(Views.canSee(game, p("c"), p("b"), true));
        assertTrue(Views.canSee(game, p("c"), p("a"), true));
        assertFalse(Views.canSee(game, p("c"), p("a"), false));
        assertTrue(Views.canSee(game, null, p("b"), true));
    }

    @Test
    void wolvesWearTheirDisguiseOnlyWhenHunting() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        advanceTo(GamePhase.NIGHT);
        p("a").setTarget(p("b"));
        advanceTo(GamePhase.POST_NIGHT);

        assertSame(p("b"), game.getWolfVictim());
        assertTrue(Views.of(game, p("a")).wolfDisguise());
        assertFalse(Views.of(game, p("c")).wolfDisguise());
        assertTrue(Views.canSee(game, p("c"), p("b"), true));
    }

    @Test
    void doorsOpenAtDuskAndDawnAndForTheDead() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        assertEquals(Boolean.TRUE, Views.doorsOpen(game, p("b")));
        assertEquals(Boolean.FALSE, Views.doorsOpen(game, null));

        advanceTo(GamePhase.NIGHT);
        assertEquals(Boolean.FALSE, Views.doorsOpen(game, p("b")));
        game.kill(p("c"));
        assertEquals(Boolean.TRUE, Views.doorsOpen(game, p("c")));
    }

    @Test
    void votingBoardShowsOnlyWhileVoting() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        assertFalse(Views.votingBoardVisible(game));
        advanceTo(GamePhase.VOTING);
        assertTrue(Views.votingBoardVisible(game));
    }
}
