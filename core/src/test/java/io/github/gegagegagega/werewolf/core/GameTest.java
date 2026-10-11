package io.github.gegagegagega.werewolf.core;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Random;

import org.junit.jupiter.api.Test;

import io.github.gegagegagega.werewolf.core.role.Doctor;
import io.github.gegagegagega.werewolf.core.role.Fool;
import io.github.gegagegagega.werewolf.core.role.Milkman;
import io.github.gegagegagega.werewolf.core.role.Sheriff;
import io.github.gegagegagega.werewolf.core.role.Townie;
import io.github.gegagegagega.werewolf.core.role.Werewolf;
import io.github.gegagegagega.werewolf.core.role.Role;

class GameTest {

    private final TestClock clock = new TestClock();
    private final RecordingListener listener = new RecordingListener();
    private final Game game = new Game(clock, listener, new Random(42));

    /** Starts a game and then overrides roles so tests are deterministic. */
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

    /** Advances phases until {@code phase} has started. */
    private void advanceTo(GamePhase phase) {
        for (int i = 0; i < 20 && game.getPhase() != phase; i++) {
            clock.finish();
        }
        assertEquals(phase, game.getPhase());
    }

    @Test
    void assignsConfiguredRolesAndFillsWithTownies() {
        game.setRoleCount("werewolf", 2);
        game.setRoleCount("doctor", 1);
        for (int i = 0; i < 6; i++) game.addPlayer("p" + i);
        game.start();

        long wolves = game.getPlayers().stream().filter(WWPlayer::isWerewolf).count();
        long doctors = game.getPlayers().stream().filter(p -> p.getRole() instanceof Doctor).count();
        long townies = game.getPlayers().stream().filter(p -> p.getRole() instanceof Townie).count();
        assertEquals(2, wolves);
        assertEquals(1, doctors);
        assertEquals(3, townies);
        assertEquals(GamePhase.FIRST_DAY, game.getPhase());
        assertEquals(60, clock.seconds);
    }

    @Test
    void firstDayLeadsToDuskThenNight() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        clock.finish();
        assertEquals(GamePhase.DUSK, game.getPhase());
        clock.finish();
        assertEquals(GamePhase.NIGHT, game.getPhase());
    }

    @Test
    void lateJoinersAreDeadSpectatorsAndDoNotLowerTheMajority() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie(), "e", new Townie());
        game.addPlayer("late1");
        game.addPlayer("late2");
        assertTrue(p("late1").isSpectator());
        assertFalse(p("late1").isAlive());
        assertEquals(5, game.aliveCount());

        advanceTo(GamePhase.VOTING);
        // 5 alive: a majority needs 3 votes.
        assertEquals(Game.VoteResult.CHANGED, game.vote(p("a"), p("b")));
        assertEquals(Game.VoteResult.CHANGED, game.vote(p("c"), p("b")));
        assertEquals(GamePhase.VOTING, game.getPhase());
        assertEquals(Game.VoteResult.MAJORITY, game.vote(p("d"), p("b")));
        assertEquals(GamePhase.DEFENSE, game.getPhase());
        assertSame(p("b"), game.getAccused());
    }

    @Test
    void withdrawingAVoteWorks() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        advanceTo(GamePhase.VOTING);
        game.vote(p("a"), p("b"));
        assertEquals(1, game.votesFor(p("b")));
        assertEquals(Game.VoteResult.CHANGED, game.vote(p("a"), null));
        assertEquals(0, game.votesFor(p("b")));
        assertEquals(Game.VoteResult.UNCHANGED, game.vote(p("a"), null));
    }

    @Test
    void guiltyVerdictExecutesTheAccused() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie(), "e", new Werewolf());
        advanceTo(GamePhase.VOTING);
        game.vote(p("b"), p("a"));
        game.vote(p("c"), p("a"));
        game.vote(p("d"), p("a"));
        assertEquals(GamePhase.DEFENSE, game.getPhase());
        clock.finish();
        assertEquals(GamePhase.JUDGMENT, game.getPhase());
        assertFalse(game.judge(p("a"), Judgment.INNOCENT), "the accused can't judge");
        game.judge(p("b"), Judgment.GUILTY);
        game.toggleJudgment(p("c"), Judgment.GUILTY);
        game.toggleJudgment(p("d"), Judgment.INNOCENT);
        clock.finish();

        assertEquals(GamePhase.SENTENCE, game.getPhase());
        assertFalse(p("a").isAlive());
        assertTrue(p("a").isDeathRevealed());
        assertTrue(p("a").wasLynched());
        assertTrue(listener.broadcasts.contains("b voted GUILTY"));
        assertTrue(listener.broadcasts.contains("d voted INNOCENT"));
        assertTrue(listener.broadcasts.contains("e ABSTAINED"));
        clock.finish();
        assertEquals(GamePhase.DUSK, game.getPhase());
    }

    @Test
    void innocentVerdictResumesVotingWithTheTimeThatWasLeft() {
        startWith("a", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        advanceTo(GamePhase.VOTING);
        clock.ticksLeftOnStop = 25 * 20 + 3;
        game.vote(p("b"), p("a"));
        game.vote(p("c"), p("a"));
        game.vote(p("d"), p("a"));
        clock.finish(); // defense -> judgment
        game.judge(p("b"), Judgment.INNOCENT);
        clock.finish();

        assertEquals(GamePhase.VOTING, game.getPhase());
        assertTrue(game.isVotingResumed());
        assertEquals(26, clock.seconds);
        assertTrue(p("a").isAlive());
        assertEquals(0, game.votesFor(p("a")), "votes reset when voting resumes");
    }

    @Test
    void werewolvesKillTheirTargetAndTheDeathIsAnnouncedNextMorning() {
        startWith("w", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        advanceTo(GamePhase.NIGHT);
        game.setTarget(p("w"), p("b"));
        clock.finish();

        assertEquals(GamePhase.POST_NIGHT, game.getPhase());
        assertSame(p("b"), game.getWolfVictim());
        assertFalse(p("b").isAlive());
        assertFalse(p("b").isDeathRevealed(), "night deaths stay secret until morning");
        assertNull(p("w").getTarget(), "targets reset after the night");

        List<WWPlayer> deaths = game.takePendingDeathAnnouncements();
        assertEquals(List.of(p("b")), deaths);
        assertEquals("b(Bakery) was mauled by werewolves",
                RecordingListener.plain(Game.deathMessage(p("b"), "Bakery")));
    }

    @Test
    void doctorSavesTheWolvesTargetAndHearsAboutIt() {
        startWith("w", new Werewolf(), "doc", new Doctor(), "c", new Townie(), "d", new Townie(), "e", new Townie());
        advanceTo(GamePhase.NIGHT);
        game.setTarget(p("w"), p("c"));
        game.setTarget(p("doc"), p("c"));
        clock.finish();

        assertTrue(p("c").isAlive());
        assertTrue(game.getKnockedOut().contains(p("c")));
        assertSame(p("c"), game.getWolfVictim());
        assertEquals(List.of("Your target was attacked!"), listener.tells.get("doc"));
    }

    @Test
    void doctorHasOneSelfHeal() {
        startWith("w", new Werewolf(), "doc", new Doctor(), "c", new Townie(), "d", new Townie());
        Doctor doctor = (Doctor) p("doc").getRole();
        advanceTo(GamePhase.NIGHT);
        game.setTarget(p("doc"), p("doc"));
        clock.finish();
        assertEquals(0, doctor.selfHealsLeft());
        assertFalse(doctor.canTarget(game, p("doc")));
    }

    @Test
    void sheriffAndMilkmanGetTheirResults() {
        startWith("w", new Werewolf(), "sh", new Sheriff(), "mk", new Milkman(), "d", new Townie(), "e", new Townie());
        advanceTo(GamePhase.NIGHT);
        game.setTarget(p("sh"), p("w"));
        game.setTarget(p("mk"), p("sh"));
        clock.finish();

        assertEquals(List.of("Your target is a sussy baka"), listener.tells.get("sh"));
        assertEquals(List.of("Your target was absent from their home"), listener.tells.get("mk"));
    }

    @Test
    void foolWinsWhenLynchedAndHauntsSomeoneWhoDidNotVoteInnocent() {
        startWith("f", new Fool(), "w", new Werewolf(), "x", new Werewolf(),
                "b", new Townie(), "c", new Townie(), "d", new Townie(), "e", new Townie());
        advanceTo(GamePhase.VOTING);
        for (String voter : List.of("b", "c", "d", "e")) game.vote(p(voter), p("f"));
        clock.finish(); // -> judgment
        game.judge(p("b"), Judgment.INNOCENT);
        game.judge(p("c"), Judgment.INNOCENT);
        game.judge(p("e"), Judgment.GUILTY);
        game.judge(p("w"), Judgment.GUILTY);
        game.judge(p("x"), Judgment.GUILTY);
        game.setTarget(p("f"), p("b"));
        clock.finish(); // -> sentence
        assertEquals(GamePhase.SENTENCE, game.getPhase());
        assertFalse(game.getPlayer("f").getRole().canTarget(game, p("b")), "b voted innocent");

        advanceTo(GamePhase.POST_NIGHT);
        long hauntedNonInnocents = game.getPlayers().stream()
                .filter(pl -> !pl.isAlive() && pl.getCausesOfDeath().stream()
                        .anyMatch(c -> RecordingListener.plain(c).equals("died of guilt")))
                .filter(pl -> pl.getJudgment() != Judgment.INNOCENT)
                .count();
        assertEquals(1, hauntedNonInnocents);

        game.end();
        assertTrue(listener.results.contains("TOWN FOOL f has won!"));
    }

    @Test
    void townWinsAtDuskWhenTheLastWolfIsExecuted() {
        startWith("w", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        advanceTo(GamePhase.VOTING);
        for (String voter : List.of("b", "c", "d")) game.vote(p(voter), p("w"));
        clock.finish();
        game.judge(p("b"), Judgment.GUILTY);
        clock.finish(); // sentence
        clock.finish(); // dusk -> win check
        assertFalse(game.isActive());
        assertEquals(List.of("THE TOWN has won!"), listener.results);
    }

    @Test
    void wolvesWinAtDawnWhenTheyMatchTheTown() {
        startWith("w", new Werewolf(), "b", new Townie(), "c", new Townie());
        advanceTo(GamePhase.NIGHT);
        game.setTarget(p("w"), p("b"));
        advanceTo(GamePhase.POST_NIGHT);
        clock.finish();
        assertFalse(game.isActive());
        assertEquals(List.of("WEREWOLVES have won!"), listener.results);
    }

    @Test
    void chatChannelsFollowThePhase() {
        startWith("w", new Werewolf(), "b", new Townie(), "c", new Townie(), "d", new Townie());
        assertEquals(ChatChannel.PUBLIC, game.chatChannel(p("b")));
        advanceTo(GamePhase.NIGHT);
        assertEquals(ChatChannel.WEREWOLVES, game.chatChannel(p("w")));
        assertEquals(ChatChannel.MUTED, game.chatChannel(p("b")));
        advanceTo(GamePhase.VOTING);
        for (String voter : List.of("b", "c", "d")) game.vote(p(voter), p("w"));
        assertEquals(ChatChannel.PUBLIC, game.chatChannel(p("w")), "the accused may speak");
        assertEquals(ChatChannel.MUTED, game.chatChannel(p("b")));
        game.kill(p("c"));
        assertEquals(ChatChannel.DEAD, game.chatChannel(p("c")));
    }

    @Test
    void multipleCausesOfDeathAreJoined() {
        WWPlayer player = new WWPlayer("z");
        player.addCauseOfDeath(net.kyori.adventure.text.Component.text("a"));
        player.addCauseOfDeath(net.kyori.adventure.text.Component.text("b"));
        player.addCauseOfDeath(net.kyori.adventure.text.Component.text("c"));
        assertEquals("z a, b, and c", RecordingListener.plain(Game.deathMessage(player, null)));
    }
}
