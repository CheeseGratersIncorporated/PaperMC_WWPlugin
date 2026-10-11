package io.github.gegagegagega.werewolf;

import java.util.HashMap;
import java.util.Map;

import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import io.github.gegagegagega.werewolf.commands.WWCommand;
import io.github.gegagegagega.werewolf.core.Game;
import io.github.gegagegagega.werewolf.display.FakeTabManager;
import io.github.gegagegagega.werewolf.display.ItemHider;
import io.github.gegagegagega.werewolf.display.NameTagManager;
import io.github.gegagegagega.werewolf.display.SkinMasker;
import io.github.gegagegagega.werewolf.display.VotingDisplay;
import io.github.gegagegagega.werewolf.game.BossBarClock;
import io.github.gegagegagega.werewolf.game.GameEffects;
import io.github.gegagegagega.werewolf.game.WorldEffects;
import io.github.gegagegagega.werewolf.housing.House;
import io.github.gegagegagega.werewolf.housing.HousingEditorListener;
import io.github.gegagegagega.werewolf.housing.HousingManager;
import io.github.gegagegagega.werewolf.housing.HousingStore;
import io.github.gegagegagega.werewolf.inventory.PlayerInventories;
import io.github.gegagegagega.werewolf.listener.ChatListener;
import io.github.gegagegagega.werewolf.listener.CombatListener;
import io.github.gegagegagega.werewolf.listener.ConnectionListener;
import io.github.gegagegagega.werewolf.listener.ProtectionListener;
import io.github.gegagegagega.werewolf.listener.TrialListener;
import io.github.gegagegagega.werewolf.scene.SceneLocations;
import io.github.gegagegagega.werewolf.sync.PlayerSync;
import io.github.gegagegagega.werewolf.voice.VoiceChat;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

/**
 * Plugin entry point. Builds the services, wires listeners and commands, and
 * owns the current {@link Game}. Game rules live in the {@code core} module;
 * Minecraft effects live in {@link GameEffects} (one-shots) and
 * {@link PlayerSync} (everything that follows from state).
 */
public class WerewolfPlugin extends JavaPlugin {

    private Game game;
    private GameEffects effects;
    private PlayerSync sync;
    private WorldEffects world;

    private SceneLocations scene;
    private HousingManager housing;
    private HousingStore housingStore;
    private PlayerInventories inventories;
    private FakeTabManager fakeTab;
    private NameTagManager nameTags;
    private SkinMasker skins;
    private ItemHider itemHider;
    private VotingDisplay votingDisplay;
    private VoiceChat voiceChat;

    private final Map<String, Perm> perms = new HashMap<>();
    private boolean blueyMode = false;

    @Override
    public void onEnable() {
        voiceChat = VoiceChat.tryRegister(this);
        ConfigurationSerialization.registerClass(House.class);

        scene = new SceneLocations(this, "scenes.yml");
        scene.load();
        housingStore = new HousingStore(this);
        housing = new HousingManager(this);
        fakeTab = new FakeTabManager(this);
        nameTags = new NameTagManager();
        itemHider = new ItemHider();
        skins = new SkinMasker();
        registerSkins(skins);
        votingDisplay = new VotingDisplay();
        inventories = new PlayerInventories(this);
        world = new WorldEffects(this, scene);
        sync = new PlayerSync(this);
        effects = new GameEffects(this, world);
        game = newGame();

        // Worlds finish loading after enable, so read the map data a moment later.
        Bukkit.getScheduler().runTaskLater(this, () -> {
            housingStore.load();
            housing.load(housingStore.houses);
            scene.load();
        }, 5);

        register(inventories);
        register(new ConnectionListener(this));
        register(new CombatListener(this));
        register(new ChatListener(this));
        register(new ProtectionListener(this));
        register(new TrialListener(this));
        register(new HousingEditorListener(housing));

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS,
                commands -> commands.registrar().register(WWCommand.create(this), "Werewolf commands"));
    }

    @Override
    public void onDisable() {
        if (game != null) {
            game.abort();
        }
    }

    private void register(Listener listener) {
        getServer().getPluginManager().registerEvents(listener, this);
    }

    private Game newGame() {
        return new Game(new BossBarClock(this, effects), effects);
    }

    /** Throws away the current game and puts everyone back in the lobby. */
    public void resetGame() {
        game.abort();
        game = newGame();
        world.clearFog();
        world.setTime(6000L);
        housingStore.load();
        housing.load(housingStore.houses);
        Bukkit.getServer().broadcast(Component.text("Loaded Houses", NamedTextColor.GREEN));
        for (Player player : Bukkit.getOnlinePlayers()) {
            player.getWorld().setGameRule(GameRules.PVP, false);
            player.getInventory().clear();
            player.updateCommands();
        }
        sync.syncAll();
    }

    private static void registerSkins(SkinMasker skins) {
        skins.registerPreset("bluey",
                "ewogICJ0aW1lc3RhbXAiIDogMTcyMTM3Mjc5OTE0NiwKICAicHJvZmlsZUlkIiA6ICI5NWU5ODU1MzMwMmE0ZjdhYjRmNGQwYjZkZmE0MTkwMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJIX19JX19NIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhkYzRlNTMwY2RkYjNkMDJmNDE3YjM4ZTYzNzg4MTNiN2NjYzJkYTMyZmVhZWU5NjEyNTg5ZTZmZThkNjA1YzYiCiAgICB9CiAgfQp9",
                "T+7EA3YgK/AQIzp1+P2g5U6BHxzEKByRQ9qsFPzluoJNq4+I4j7FA1fz8HcFGUKdshvoBmwEoIUBT0/4sL1cZrG2OcBXq2QbcYU1UVJPjeLf1Dd/qYTbLv80lxjSKyQJCD7R/A0rHcZB0zDjyIu5SBc7UXSvQqFU+g1zuYsPhCGhruuhQzhfXJmNFZcmcR8hyAyTyCwzQJeVgnd9F4PdnMo0/Ef0Dy9V+Sw9PYuwtzneGThBCQWWNKXJIiXBcqDaEZovtsbe6wWpCQuXApPxwtoSfIynYawUfPBz+jYSR/b/vCPoG75Om485pB7hWBA7I1MIu9W41NC3QI6VhYI/XU3P41BT4OV67LeQp1SjIW2hEsr2Bg4D57v6aiAeLNGoxIAOgRzb8AA8XO6ATiLXg9H6JuhTj6EYo+Sh4qq447UTeDWVqP2MMPqYk1AqMb86fU7S6BdfMAB+PcpxEEH8aQNEQnaW4F8baYCq/MY5KAvG9Wnb9wrc1nsmy/YiFCAuIYSCOFcQYJRc4xtclvgtXc6a1KGPvuE9D9qg6wnPSht4G2GYBLVvHdd6bHEYnwJdG986i4kTKkX/yM3Ltdm2cJTW0kmYol0D7J42bLgV7WeYR3mtp/U7rN3CkmuRpoU77Xxhd6tpv3I4Km3mvvxSKFtHJNbA3mXJjeWrxATtRP4=");
        skins.registerPreset("werewolf",
                "ewogICJ0aW1lc3RhbXAiIDogMTc4NjI0NjQ5NDU1NiwKICAicHJvZmlsZUlkIiA6ICI3OTI0MDQ0N2ViNTk0ZDBkOTVmY2E3YmJiMWZiMjVhNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJqdW5pb3JraW9iciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82MTBhNTc1ZjZlYjY4MjQwM2E5NjY4MzdjMjlhMGZmZjc0YzVlZjEzNWJkZjdhOGNlZDFkODJiYTkxYTUxYWYxIgogICAgfQogIH0KfQ==",
                "ooQs1Sotly4wwtdOX3IFhOKS6DnqYPwyNe9WJ+jo4trZFs1He8322XEya13jHeribPVHjDmIXgbIqfIZ2B5BIBw965Hy+8L8K/R7QidXDvjVrLFw3F/34ex1kAWLP+uuo4jQxGvS6reKKGNdIzc5dUjSu/GhsYUn0LpVxZvz3li3qiUOuCMeYAKdmfhS+5Is+z9BqNIUlpSQ7i7+JLCahCbE0kLspoJ+USpS1WqFrs3tEeckx+3c5QLxIhYfuksgK4S2Dpg3LKifiDwhf9rCa0WX4zzAj10akJMnEpgOEa+z28h7eiKfaKTnHNRdbaxpPtJSeFPVEkI8EopKNANORIKt1CdKGjZLCYa/3Gcc0V+oGrNIplNZrw8yr5LEJHOa4eGSiNV0D998/ySTRyx//gGpK5iQ6qjOrO82Fi4HIiedr7HRTlLf65i7Iex2ilgIlTqVtDc3ABBzFUIQYGAvoPrO9s4uW2RVlpbAi/SLmGVU+gkQ2iombHmAwJ/3J0CD4QXzgdtC2mckYBRFLiq4fHVW0NYXmdCqHoi8I18OOsWvLLIjuYUpYVnqeU49dM0c0V9KsYtVTH6Aopl3vMlk+xBYBC6L6vcaZR04o4kLZ7+A4yGsKqB55H22mpH8fTGUKRd5DIZrAdxCH8u0xJJ7ABZ13IGaGJ9vZN27pWSER24=");
    }

    // ------------------------------------------------------------------
    // Permissions
    // ------------------------------------------------------------------

    public Perm permOf(Player player) {
        return perms.getOrDefault(player.getName(), Perm.NONE);
    }

    public void setPerm(Player player, Perm perm) {
        perms.put(player.getName(), perm);
    }

    public boolean isDev(Player player) {
        return permOf(player) == Perm.DEV;
    }

    /** Hosts run games; devs can do anything a host can. */
    public boolean isHost(Player player) {
        Perm perm = permOf(player);
        return perm == Perm.HOST || perm == Perm.DEV;
    }

    // ------------------------------------------------------------------
    // Accessors
    // ------------------------------------------------------------------

    public Game game() {
        return game;
    }

    public GameEffects effects() {
        return effects;
    }

    public PlayerSync sync() {
        return sync;
    }

    public WorldEffects world() {
        return world;
    }

    public SceneLocations scene() {
        return scene;
    }

    public HousingManager housing() {
        return housing;
    }

    public HousingStore housingStore() {
        return housingStore;
    }

    public PlayerInventories inventories() {
        return inventories;
    }

    public FakeTabManager fakeTab() {
        return fakeTab;
    }

    public NameTagManager nameTags() {
        return nameTags;
    }

    public SkinMasker skins() {
        return skins;
    }

    public ItemHider itemHider() {
        return itemHider;
    }

    public VotingDisplay votingDisplay() {
        return votingDisplay;
    }

    /** Simple Voice Chat integration, or null when that plugin isn't installed. */
    public VoiceChat voiceChat() {
        return voiceChat;
    }

    public boolean isBlueyMode() {
        return blueyMode;
    }

    public void setBlueyMode(boolean blueyMode) {
        this.blueyMode = blueyMode;
    }
}
