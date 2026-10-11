package io.github.gegagegagega.werewolf.display;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.protocol.player.GameMode;
import com.github.retrooper.packetevents.protocol.player.UserProfile;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoRemove;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerPlayerInfoUpdate;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;

import java.util.concurrent.CompletableFuture;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class FakeTabManager {

    private static final EnumSet<
            WrapperPlayServerPlayerInfoUpdate.Action
            > FULL_UPDATE_ACTIONS = EnumSet.of(
            WrapperPlayServerPlayerInfoUpdate.Action.ADD_PLAYER,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LISTED,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LATENCY,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_GAME_MODE,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_DISPLAY_NAME,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_LIST_ORDER,
            WrapperPlayServerPlayerInfoUpdate.Action.UPDATE_HAT
    );

    private final JavaPlugin plugin;

    /*
     * Logical entry key -> complete fake-tab entry.
     *
     * Storing the complete entry lets us recreate the client's
     * fake tab list whenever needed.
     */
    private final Map<String, FakeTabEntry> fakeEntries =
            new ConcurrentHashMap<>();

    public FakeTabManager(JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "plugin");
    }

    /**
     * Creates or updates an entry and shows it to one viewer.
     *
     * The same UUID is retained when an existing key is updated.
     */
    // public UUID addFakePlayer(
    //         Player viewer,
    //         String entryKey,
    //         String profileName,
    //         Component displayName,
    //         int ping
    // ) {
    //     Objects.requireNonNull(viewer, "viewer");

    //     FakeTabEntry entry = createOrUpdateEntry(
    //             entryKey,
    //             profileName,
    //             displayName,
    //             ping,
    //             0,
    //             true
    //     );

    //     sendEntries(viewer, List.of(entry));
    //     return entry.uuid();
    // }

    /**
     * Creates or updates an entry and shows it to every
     * currently online player.
     */
    public UUID addFakePlayerForEveryone(
        String entryKey,
        String profileName,
        Component displayName,
        int ping
) {
    /*
     * Updating an entry whose skin is already known (e.g. greying out a
     * dead player) resends it straight away instead of looking it up again.
     */
    FakeTabEntry previous = fakeEntries.get(entryKey);
    if (previous != null
            && previous.profileName().equals(profileName)
            && !previous.textureProperties().isEmpty()) {
        FakeTabEntry updated = createOrUpdateEntry(
                entryKey,
                profileName,
                displayName,
                ping,
                previous.listOrder(),
                previous.showHat(),
                previous.textureProperties()
        );
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            sendEntries(viewer, List.of(updated));
        }
        return updated.uuid();
    }

    /*
     * Reserve and retain the fake UUID immediately.
     * The entry is not sent until its skin has loaded.
     */
    FakeTabEntry pendingEntry = createOrUpdateEntry(
            entryKey,
            profileName,
            displayName,
            ping,
            0,
            true,
            List.of()
    );

    resolveSkin(profileName).thenAcceptAsync(
            textureProperties -> {
                /*
                 * The entry may have been removed while the skin
                 * lookup was running.
                 */
                FakeTabEntry current =
                        fakeEntries.get(entryKey);

                if (current == null
                        || !current.uuid().equals(
                                pendingEntry.uuid()
                        )) {
                    return;
                }

                if (textureProperties.isEmpty()) {
                    plugin.getLogger().warning(
                            "No skin textures found for "
                                    + profileName
                    );
                    return;
                }

                FakeTabEntry resolvedEntry =
                        new FakeTabEntry(
                                current.uuid(),
                                current.profileName(),
                                current.displayName(),
                                current.ping(),
                                current.listOrder(),
                                current.showHat(),
                                textureProperties
                        );

                fakeEntries.put(entryKey, resolvedEntry);

                for (Player viewer :
                        Bukkit.getOnlinePlayers()) {
                    sendEntries(
                            viewer,
                            List.of(resolvedEntry)
                    );
                }
            },

            /*
             * Switch back to the Bukkit server thread before
             * accessing online players or sending the entries.
             */
            runnable -> Bukkit.getScheduler()
                    .runTask(plugin, runnable)
    ).exceptionally(exception -> {
        plugin.getLogger().warning(
                "Could not load skin for "
                        + profileName
                        + ": "
                        + exception.getMessage()
        );

        return null;
    });

    return pendingEntry.uuid();
}
    /**
     * Removes an entry from one viewer's client without deleting
     * the stored entry.
     */
    public boolean removeFakePlayer(
            Player viewer,
            String entryKey
    ) {
        FakeTabEntry entry = fakeEntries.get(entryKey);

        if (entry == null) {
            return false;
        }

        sendRemoval(viewer, List.of(entry.uuid()));
        return true;
    }

    /**
     * Deletes the stored entry and removes it from every
     * currently online player's tab list.
     */
    public boolean removeFakePlayerForEveryone(String entryKey) {
        FakeTabEntry entry = fakeEntries.remove(entryKey);

        if (entry == null) {
            return false;
        }

        WrapperPlayServerPlayerInfoRemove packet =
                new WrapperPlayServerPlayerInfoRemove(
                        entry.uuid()
                );

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            PacketEvents.getAPI()
                    .getPlayerManager()
                    .sendPacket(viewer, packet);
        }

        return true;
    }



    public void removeAll() {
    List<UUID> fakeIds = fakeEntries.values()
            .stream()
            .map(FakeTabEntry::uuid)
            .toList();

    if (fakeIds.isEmpty()) {
        return;
    }

    WrapperPlayServerPlayerInfoRemove packet =
            new WrapperPlayServerPlayerInfoRemove(fakeIds);

    for (Player viewer : Bukkit.getOnlinePlayers()) {
        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacket(viewer, packet);
    }

    fakeEntries.clear();
}
    /**
     * Sends all stored fake entries to a viewer without first
     * removing them.
     *
     * Use this for a player who just joined and does not have
     * any of the fake entries yet.
     */
    public void showAllTo(Player viewer) {
        Objects.requireNonNull(viewer, "viewer");

        List<FakeTabEntry> entries =
                List.copyOf(fakeEntries.values());

        if (entries.isEmpty()) {
            return;
        }

        sendEntries(viewer, entries);
    }

    /**
     * Completely refreshes every fake entry for one viewer.
     *
     * All currently stored fake UUIDs are removed, then all
     * current entries are resent one tick later.
     */
    public void refresh(Player viewer) {
        Objects.requireNonNull(viewer, "viewer");

        List<UUID> existingIds = fakeEntries.values()
                .stream()
                .map(FakeTabEntry::uuid)
                .toList();

        if (existingIds.isEmpty()) {
            return;
        }

        sendRemoval(viewer, existingIds);

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!viewer.isOnline()) {
                return;
            }

            showAllTo(viewer);
        });
    }

    /**
     * Completely refreshes fake entries for every online player.
     */
    public void refreshAll() {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            refresh(viewer);
        }
    }

    /**
     * Refreshes one fake entry for one viewer.
     */
    public boolean refreshEntry(
            Player viewer,
            String entryKey
    ) {
        FakeTabEntry entry = fakeEntries.get(entryKey);

        if (entry == null) {
            return false;
        }

        sendRemoval(viewer, List.of(entry.uuid()));

        Bukkit.getScheduler().runTask(plugin, () -> {
            if (!viewer.isOnline()) {
                return;
            }

            FakeTabEntry currentEntry =
                    fakeEntries.get(entryKey);

            if (currentEntry != null) {
                sendEntries(
                        viewer,
                        List.of(currentEntry)
                );
            }
        });

        return true;
    }

    /**
     * Refreshes one fake entry for everyone.
     */
    public boolean refreshEntryForEveryone(String entryKey) {
        FakeTabEntry entry = fakeEntries.get(entryKey);

        if (entry == null) {
            return false;
        }

        for (Player viewer : Bukkit.getOnlinePlayers()) {
            refreshEntry(viewer, entryKey);
        }

        return true;
    }

    public Set<String> entryKeys() {
        return Set.copyOf(fakeEntries.keySet());
    }

    public boolean containsEntry(String entryKey) {
        return fakeEntries.containsKey(entryKey);
    }

    public int size() {
        return fakeEntries.size();
    }

   private FakeTabEntry createOrUpdateEntry(
        String entryKey,
        String profileName,
        Component displayName,
        int ping,
        int listOrder,
        boolean showHat,
        List<TextureProperty> textureProperties
) {
    Objects.requireNonNull(entryKey, "entryKey");
    Objects.requireNonNull(profileName, "profileName");
    Objects.requireNonNull(
            textureProperties,
            "textureProperties"
    );

    FakeTabEntry previous =
            fakeEntries.get(entryKey);

    UUID uuid = previous == null
            ? UUID.randomUUID()
            : previous.uuid();

    FakeTabEntry updated =
            new FakeTabEntry(
                    uuid,
                    profileName,
                    displayName,
                    Math.max(0, ping),
                    listOrder,
                    showHat,
                    List.copyOf(textureProperties)
            );

    fakeEntries.put(entryKey, updated);
    return updated;
}
    
private void sendEntries(
        Player viewer,
        List<FakeTabEntry> entries
) {
    if (entries.isEmpty()) {
        return;
    }

    List<
            WrapperPlayServerPlayerInfoUpdate.PlayerInfo
            > playerInfos = new ArrayList<>();

    for (FakeTabEntry entry : entries) {
        /*
         * Do not send a temporary default skin while an
         * asynchronous profile lookup is still running.
         */
        if (entry.textureProperties().isEmpty()) {
            continue;
        }

        UserProfile profile = new UserProfile(
                entry.uuid(),
                entry.profileName(),
                entry.textureProperties()
        );

        playerInfos.add(
                new WrapperPlayServerPlayerInfoUpdate.PlayerInfo(
                        profile,
                        true,
                        entry.ping(),
                        GameMode.ADVENTURE,
                        entry.displayName(),
                        null,
                        entry.listOrder(),
                        entry.showHat()
                )
        );
    }

    if (playerInfos.isEmpty()) {
        return;
    }

    WrapperPlayServerPlayerInfoUpdate packet =
            new WrapperPlayServerPlayerInfoUpdate(
                    EnumSet.copyOf(FULL_UPDATE_ACTIONS),
                    playerInfos
            );

    PacketEvents.getAPI()
            .getPlayerManager()
            .sendPacket(viewer, packet);
}

    private void sendRemoval(
            Player viewer,
            List<UUID> profileIds
    ) {
        if (profileIds.isEmpty()) {
            return;
        }

        PacketEvents.getAPI()
                .getPlayerManager()
                .sendPacket(
                        viewer,
                        new WrapperPlayServerPlayerInfoRemove(
                                profileIds
                        )
                );
    }

    private record FakeTabEntry(
        UUID uuid,
        String profileName,
        Component displayName,
        int ping,
        int listOrder,
        boolean showHat,
        List<TextureProperty> textureProperties
) {
}

    private CompletableFuture<List<TextureProperty>> resolveSkin(
        String profileName
) {
    /*
     * Avoid an external lookup when the actual player is online.
     * Their profile already contains the official texture property.
     */
    Player onlinePlayer =
            Bukkit.getPlayerExact(profileName);

    if (onlinePlayer != null) {
        return CompletableFuture.completedFuture(
                convertTextures(
                        onlinePlayer.getPlayerProfile()
                )
        );
    }

    PlayerProfile unresolvedProfile =
            Bukkit.createProfile(profileName);

    return unresolvedProfile.update()
            .thenApply(FakeTabManager::convertTextures);
}

private static List<TextureProperty> convertTextures(
        PlayerProfile profile
) {
    return profile.getProperties()
            .stream()
            .filter(property ->
                    property.getName()
                            .equalsIgnoreCase("textures")
            )
            .map(property ->
                    new TextureProperty(
                            property.getName(),
                            property.getValue(),
                            property.getSignature()
                    )
            )
            .toList();
}
}