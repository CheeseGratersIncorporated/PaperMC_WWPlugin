package io.github.gegagegagega.werewolf.game;

import java.time.Duration;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import io.github.gegagegagega.werewolf.WerewolfPlugin;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.core.GameClock;
import io.github.gegagegagega.werewolf.core.GameListener;
import io.github.gegagegagega.werewolf.core.GamePhase;
import io.github.gegagegagega.werewolf.core.WWPlayer;
import io.github.gegagegagega.werewolf.housing.House;
import io.github.gegagegagega.werewolf.inventory.PlayerInventories;
import io.github.gegagegagega.werewolf.scene.SceneKeys;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

/**
 * Turns game events into one-shot Minecraft effects: titles, sounds,
 * teleports, the boss bar, fog and time of day, the guillotine. Anything
 * that should <i>stay</i> a certain way (visibility, name tags, ghosts) is not
 * done here; every event ends with {@link #onStateChanged()}, which hands
 * over to {@link io.github.gegagegagega.werewolf.sync.PlayerSync}.
 */
public final class GameEffects implements GameListener {

    private static final Title.Times TITLE_TIMES =
            Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500));
    private static final int START_COUNTDOWN_SECONDS = 5;

    private final WerewolfPlugin plugin;
    private final Placement placement;
    private final Guillotine guillotine;
    private final WorldEffects world;

    public GameEffects(WerewolfPlugin plugin, WorldEffects world) {
        this.plugin = plugin;
        this.world = world;
        this.placement = new Placement(plugin);
        this.guillotine = new Guillotine(plugin, plugin.scene());
    }

    public Placement placement() {
        return placement;
    }

    private Game game() {
        return plugin.game();
    }

    // ------------------------------------------------------------------
    // Starting
    // ------------------------------------------------------------------

    /** Sets up the map and players, then starts the countdown to the first day. */
    public boolean beginStart() {
        Game game = game();
        if (game.isActive() || game.isStarting()) {
            return false;
        }
        plugin.housing().closeAllDoors();
        plugin.scene().load();
        plugin.housing().clearViewers();
        Bukkit.getServer().broadcast(Component.text("loaded scenes", NamedTextColor.GREEN));
        plugin.housing().forceClaim(game.getPlayerNames());
        world.fog();
        for (House house : plugin.housing().getHouses()) {
            if (house.lectern != null) {
                plugin.inventories().wipeLecternBook(house.lectern);
            }
        }

        game.startCountdown(START_COUNTDOWN_SECONDS);

        Location spawn = plugin.scene().getLocation(SceneKeys.DEFAULT_SPAWN);
        for (Player player : Bukkit.getOnlinePlayers()) {
            House house = plugin.housing().houseOf(player.getName());
            if (!game.hasPlayer(player.getName())) {
                if (spawn != null) player.setRespawnLocation(spawn, true);
            } else if (house != null) {
                player.setRespawnLocation(house.location, true);
            }
            player.playSound(player.getLocation(), Sound.BLOCK_END_PORTAL_SPAWN, 0.2f, 1.0f);
            player.getWorld().setGameRule(GameRules.IMMEDIATE_RESPAWN, true);
            player.getWorld().setGameRule(GameRules.PVP, true);
            player.getInventory().clear();
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(false);
            player.setInvisible(false);
            // Everyone respawns at home (or the spawn point) for a clean start.
            player.kill();
            blind(player);
        }
        plugin.sync().syncAll();
        return true;
    }

    @Override
    public void onGameStarted() {
        Game game = game();
        world.clearFog();
        world.setTime(6000L);
        guillotine.reset();
        for (Player player : Bukkit.getOnlinePlayers()) {
            WWPlayer participant = game.getPlayer(player.getName());
            if (participant != null) {
                plugin.inventories().prepareInventory(player);
                player.showTitle(Title.title(participant.getRole().title(), participant.getRole().subtitle(), TITLE_TIMES));
            }
        }
        // Anyone online who didn't join watches as a spectator.
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!game.hasPlayer(player.getName())) {
                game.addPlayer(player.getName());
                player.getInventory().clear();
                player.kill();
            }
        }
    }

    // ------------------------------------------------------------------
    // Phases
    // ------------------------------------------------------------------

    @Override
    public void onPhaseStarted(GamePhase phase) {
        Game game = game();
        switch (phase) {
            case DISCUSSION -> {
                announceDeaths(game);
                placement.placeAll(phase);
                world.setTime(23500L); // sunrise
            }
            case VOTING -> {
                if (!game.isVotingResumed()) {
                    showTitleToAll(Component.text("VOTING", NamedTextColor.GREEN),
                            Component.text("Use \"/ww vote\" to vote someone up to the stand", NamedTextColor.GRAY));
                }
            }
            case DEFENSE -> {
                ringBell(1f);
                WWPlayer accused = game.getAccused();
                showTitleToAll(Component.text("TRIAL", NamedTextColor.GOLD), accused == null
                        ? Component.empty()
                        : Component.text(accused.getName() + " has been voted up to the stand!", NamedTextColor.GRAY));
                placement.placeAll(phase);
            }
            case JUDGMENT -> {
                showTitleToAll(Component.text("JUDGMENT", NamedTextColor.LIGHT_PURPLE),
                        Component.text("right click on concrete to judge", NamedTextColor.GRAY));
                for (Player player : Bukkit.getOnlinePlayers()) {
                    WWPlayer participant = game.getPlayer(player.getName());
                    if (participant != null && game.canJudge(participant)) {
                        player.getInventory().setHeldItemSlot(PlayerInventories.JUDGMENT_HOLD_SLOT);
                    }
                }
            }
            case SENTENCE -> {
                guillotine.reset();
                guillotine.placeHead(game.getAccused());
                placement.placeAll(phase);
            }
            case DUSK -> {
                guillotine.removeHead();
                enforceDeaths(game);
                ringBell(0.1f);
                Bukkit.getScheduler().runTaskLater(plugin, () -> ringBell(0.1f), 30);
                Bukkit.getScheduler().runTaskLater(plugin, () -> ringBell(0.1f), 60);
                showTitleToAll(Component.text("Night is falling...", NamedTextColor.DARK_PURPLE),
                        Component.text("Return to your house", NamedTextColor.GRAY));
            }
            case NIGHT -> {
                guillotine.reset();
                placement.placeAll(phase);
                world.setTime(18000L);
            }
            case POST_NIGHT -> {
                placement.placeAll(phase);
                int ticks = game.durationOf(GamePhase.POST_NIGHT) * GameClock.TICKS_PER_SECOND;
                for (Player player : Bukkit.getOnlinePlayers()) {
                    WWPlayer participant = game.getPlayer(player.getName());
                    if (participant != null && participant.isAlive() && participant.isWerewolf()) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, ticks, 0, false, false, false));
                    }
                }
                world.setTime(18000L);
            }
            case DAWN -> {
                world.clearFog();
                enforceDeaths(game);
                for (Player player : Bukkit.getOnlinePlayers()) {
                    WWPlayer participant = game.getPlayer(player.getName());
                    if (participant == null) continue;
                    House house = plugin.housing().houseOf(participant.getName());
                    if (!participant.isAlive() && house != null) {
                        // The town can now read the dead player's will.
                        plugin.inventories().copyBookToLectern(player, house.lectern);
                    }
                    if (game.getKnockedOut().contains(participant)) {
                        // Saved by the doctor: knocked down, then back up at home.
                        player.kill();
                    }
                }
                placement.placeAll(phase);
            }
            default -> {
            }
        }
    }

    /** Per-tick animations, called by {@link BossBarClock}. */
    public void onTick(GamePhase phase, int ticksLeft) {
        if (phase == null) {
            return;
        }
        Game game = game();
        float progress = 1 - (float) ticksLeft / (game.durationOf(phase) * GameClock.TICKS_PER_SECOND);
        switch (phase) {
            case SENTENCE -> guillotine.tick(ticksLeft, game.getAccused(), this::killBody);
            case DUSK -> {
                if (ticksLeft == 20) {
                    world.fog();
                    // Anyone still outside their house as the fog rolls in is blinded.
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        WWPlayer participant = game.getPlayer(player.getName());
                        House house = participant == null ? null : plugin.housing().houseOf(participant.getName());
                        if (participant != null && participant.isAlive() && house != null
                                && !house.contains(player.getLocation())) {
                            blind(player);
                        }
                    }
                }
                world.setTime((long) (progress * -5500L + 23500L)); // sunrise in reverse to midnight
            }
            case POST_NIGHT -> world.setTime((long) (progress * 5000L + 18000L));
            case DAWN -> {
                if (ticksLeft == 20) {
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        House house = game.hasPlayer(player.getName())
                                ? plugin.housing().houseOf(player.getName()) : null;
                        if (house != null && house.contains(player.getLocation())) {
                            blind(player);
                        }
                    }
                }
                world.setTime((long) (progress * 500L + 23000L)); // dawn to sunrise
            }
            default -> {
            }
        }
    }

    public BossBar bossBarFor(GamePhase phase) {
        if (phase == null) {
            return bossBar("Werewolf Game Starting...", NamedTextColor.RED, BossBar.Color.RED);
        }
        int day = game().getDay();
        return switch (phase) {
            case FIRST_DAY -> bossBar("DAY 1", NamedTextColor.YELLOW, BossBar.Color.YELLOW);
            case DISCUSSION -> bossBar("DISCUSSION - DAY " + day, NamedTextColor.YELLOW, BossBar.Color.YELLOW);
            case VOTING -> bossBar("VOTING - DAY " + day, NamedTextColor.GREEN, BossBar.Color.GREEN);
            case DEFENSE -> bossBar("DEFENSE - DAY " + day, NamedTextColor.GOLD, BossBar.Color.YELLOW);
            case JUDGMENT -> bossBar("JUDGMENT - DAY " + day, NamedTextColor.LIGHT_PURPLE, BossBar.Color.PINK);
            case SENTENCE -> bossBar("DEATH SENTENCE", NamedTextColor.RED, BossBar.Color.RED);
            case DUSK -> bossBar("Night is falling...", NamedTextColor.DARK_PURPLE, BossBar.Color.PURPLE);
            case NIGHT -> bossBar("NIGHT " + day, NamedTextColor.BLUE, BossBar.Color.BLUE);
            case POST_NIGHT -> bossBar("Dawn breaks... ", NamedTextColor.DARK_PURPLE, BossBar.Color.PURPLE);
            case DAWN -> bossBar("Cock-a-doodley-doo mfs", NamedTextColor.YELLOW, BossBar.Color.YELLOW);
        };
    }

    private static BossBar bossBar(String text, NamedTextColor textColor, BossBar.Color barColor) {
        return BossBar.bossBar(Component.text(text, textColor), 1.0f, barColor, BossBar.Overlay.PROGRESS);
    }

    // ------------------------------------------------------------------
    // Game events
    // ------------------------------------------------------------------

    @Override
    public void onStateChanged() {
        plugin.sync().syncAll();
    }

    @Override
    public void onLynched(WWPlayer player) {
        if (player.getRole() != null && player.getRole().winsWhenLynched()) {
            showTitleToAll(Component.text("THE FOOL WAS LYNCHED", NamedTextColor.LIGHT_PURPLE),
                    Component.text("L", NamedTextColor.GRAY));
        }
    }

    @Override
    public void onGameEnded(List<Component> results) {
        Game game = game();
        for (int i = 0; i < results.size(); i++) {
            Component line = results.get(i);
            Bukkit.getServer().broadcast(line);
            Bukkit.getScheduler().runTaskLater(plugin,
                    () -> showTitleToAll(line, Component.empty()), 4L * 20 * i);
        }
        for (WWPlayer player : game.getPlayers()) {
            Bukkit.getServer().broadcast(Component.text(player.getName(), NamedTextColor.WHITE)
                    .append(Component.text(" was a ", NamedTextColor.YELLOW))
                    .append(player.getRole().title()));
        }
        Location spawn = plugin.scene().getLocation(SceneKeys.DEFAULT_SPAWN);
        if (spawn != null) {
            for (Player player : Bukkit.getOnlinePlayers()) {
                player.teleport(spawn);
            }
        }
        Bukkit.getScheduler().runTask(plugin, plugin::resetGame);
    }

    @Override
    public void tell(WWPlayer player, Component message) {
        Player online = Bukkit.getPlayerExact(player.getName());
        if (online != null) {
            online.sendMessage(message);
        }
    }

    @Override
    public void broadcast(Component message) {
        Bukkit.getServer().broadcast(message);
    }

    // ------------------------------------------------------------------
    // Helpers
    // ------------------------------------------------------------------

    /** Night deaths are read out one per second at the start of discussion. */
    private void announceDeaths(Game game) {
        int delay = 0;
        for (WWPlayer dead : game.takePendingDeathAnnouncements()) {
            if (dead.isSpectator()) continue;
            delay++;
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (plugin.game() != game) return;
                if (!dead.getCausesOfDeath().isEmpty()) {
                    House house = plugin.housing().houseOf(dead.getName());
                    Bukkit.getServer().broadcast(Game.deathMessage(dead, house == null ? null : house.name));
                    ringBell(0.1f);
                }
                game.revealDeath(dead);
            }, delay * 20L);
        }
    }

    /** Makes the character of a dead player actually die, so they become a ghost. */
    public void killBody(WWPlayer participant) {
        if (participant.isAlive() || participant.isGhost()) {
            return;
        }
        Player player = Bukkit.getPlayerExact(participant.getName());
        if (player != null) {
            player.kill();
        }
        // Covers offline players and deaths that didn't go through.
        if (!participant.isGhost()) {
            participant.setGhost(true);
            plugin.sync().syncAll();
        }
    }

    /** Every dead participant who is still walking around dies for real. */
    private void enforceDeaths(Game game) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            WWPlayer participant = game.getPlayer(player.getName());
            if (participant != null && !participant.isAlive() && !participant.isGhost()) {
                killBody(participant);
            }
        }
    }

    private static void showTitleToAll(Component title, Component subtitle) {
        Title shown = Title.title(title, subtitle, TITLE_TIMES);
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.showTitle(shown);
        }
    }

    private static void ringBell(float pitch) {
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.playSound(player.getLocation(), Sound.BLOCK_BELL_USE, 1f, pitch);
        }
    }

    private static void blind(Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 255, false, false, false));
    }
}
