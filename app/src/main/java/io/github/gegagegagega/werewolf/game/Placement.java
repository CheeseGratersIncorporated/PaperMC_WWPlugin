package io.github.gegagegagega.werewolf.game;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.GamePhase;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.housing.House;
import io.github.gegagegagega.werewolf.scene.SceneKeys;

/**
 * Where players should stand in each phase. Used both when a phase starts
 * and when someone rejoins mid-phase, so the two can't drift apart.
 */
public final class Placement {

    /** Audience seats per row in front of the trial stage. */
    private static final int AUDIENCE_ROW_LENGTH = 12;

    private final WerewolfPlugin plugin;

    public Placement(WerewolfPlugin plugin) {
        this.plugin = plugin;
    }

    /** Moves every online participant to where the new phase needs them. */
    public void placeAll(GamePhase phase) {
        Game game = plugin.game();
        int seat = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            WWPlayer participant = game.getPlayer(player.getName());
            if (participant == null) {
                continue;
            }
            switch (phase) {
                case DISCUSSION -> {
                    House house = houseOf(participant);
                    if (participant.isAlive() && house != null && house.tpout != null
                            && house.contains(player.getLocation())) {
                        player.teleport(house.tpout);
                    }
                }
                case DEFENSE -> {
                    if (participant == game.getAccused()) {
                        teleport(player, SceneKeys.TRIAL_STAGE);
                    } else {
                        seatInAudience(player, seat % AUDIENCE_ROW_LENGTH, seat / AUDIENCE_ROW_LENGTH);
                        seat++;
                    }
                }
                case SENTENCE -> teleport(player, SceneKeys.EXECUTION_AUDIENCE);
                case NIGHT -> {
                    if (!participant.isAlive()) {
                        break;
                    }
                    House house = houseOf(participant);
                    if (house != null && !house.contains(player.getLocation())) {
                        player.teleport(house.location);
                    }
                    if (participant.isWerewolf()) {
                        teleport(player, SceneKeys.WOLF_DEN);
                    }
                }
                case POST_NIGHT -> {
                    // The victim is dragged to the den, where the wolves are waiting.
                    WWPlayer victim = game.getWolfVictim();
                    if (victim != null && (participant == victim
                            || (participant.isAlive() && participant.isWerewolf()))) {
                        teleport(player, SceneKeys.WOLF_DEN);
                    }
                }
                case DAWN -> {
                    House house = houseOf(participant);
                    if (participant.isAlive() && participant.isWerewolf() && house != null) {
                        player.teleport(house.location);
                    }
                }
                default -> {
                }
            }
        }
    }

    /** Puts a living participant who just rejoined where the current phase needs them. */
    public void placeRejoining(Player player, WWPlayer participant) {
        Game game = plugin.game();
        GamePhase phase = game.getPhase();
        if (phase == null || !participant.isAlive()) {
            return;
        }
        House house = houseOf(participant);
        switch (phase) {
            case DISCUSSION, VOTING, DUSK, DAWN -> {
                if (house != null && house.tpout != null) {
                    player.teleport(house.tpout);
                }
            }
            case DEFENSE, JUDGMENT -> {
                if (participant == game.getAccused()) {
                    teleport(player, SceneKeys.TRIAL_STAGE);
                } else {
                    seatInAudience(player, 5, -1);
                }
            }
            case SENTENCE -> teleport(player, SceneKeys.EXECUTION_AUDIENCE);
            case NIGHT, POST_NIGHT -> {
                if (participant.isWerewolf() && plugin.scene().hasLocation(SceneKeys.WOLF_DEN)) {
                    teleport(player, SceneKeys.WOLF_DEN);
                } else if (house != null) {
                    player.teleport(house.location);
                }
            }
            default -> {
            }
        }
    }

    private void seatInAudience(Player player, int x, int z) {
        Location audience = plugin.scene().getLocation(SceneKeys.TRIAL_AUDIENCE);
        if (audience == null) {
            return;
        }
        Location seat = audience.add(x, 0, z);
        Location stage = plugin.scene().getLocation(SceneKeys.TRIAL_STAGE);
        if (stage != null) {
            seat.setDirection(stage.toVector().subtract(seat.toVector()));
        }
        player.teleport(seat);
    }

    private void teleport(Player player, String sceneKey) {
        Location location = plugin.scene().getLocation(sceneKey);
        if (location != null) {
            player.teleport(location);
        }
    }

    private House houseOf(WWPlayer participant) {
        return plugin.housing().houseOf(participant.getName());
    }
}
