package io.github.gegagegagega.werewolf.display;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * Swaps a player's skin for a registered preset and back. {@link #apply} is
 * idempotent, so it can be called on every sync.
 */
public final class SkinMasker {

    private final Map<String, SignedSkin> presetSkins = new HashMap<>();
    private final Map<UUID, PlayerProfile> originalSkins = new HashMap<>();
    private final Map<UUID, String> currentPresets = new HashMap<>();

    public void registerPreset(String presetName, String textureValue, String textureSignature) {
        Objects.requireNonNull(presetName, "presetName");
        presetSkins.put(normalize(presetName), new SignedSkin(textureValue, textureSignature));
    }

    /** Wears {@code presetName}, or the player's own skin when it is null. Does nothing if already so. */
    public void apply(Player player, String presetName) {
        String wanted = presetName == null ? null : normalize(presetName);
        if (Objects.equals(wanted, currentPresets.get(player.getUniqueId()))) {
            return;
        }
        if (wanted == null) {
            restoreSkin(player);
        } else {
            setSkin(player, wanted);
        }
    }

    public void setSkin(Player player, String presetName) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(presetName, "presetName");

        SignedSkin skin = presetSkins.get(normalize(presetName));
        if (skin == null) {
            throw new IllegalArgumentException("Unknown skin preset: " + presetName);
        }

        // Save the real original profile only once.
        originalSkins.putIfAbsent(player.getUniqueId(), player.getPlayerProfile().clone());

        // Replace the complete texture property rather than calling PlayerTextures#setSkin(URL).
        PlayerProfile maskedProfile = player.getPlayerProfile();
        maskedProfile.removeProperty("textures");
        maskedProfile.setProperty(skin.toPaperProperty());
        player.setPlayerProfile(maskedProfile);
        currentPresets.put(player.getUniqueId(), normalize(presetName));
    }

    public boolean restoreSkin(Player player) {
        Objects.requireNonNull(player, "player");
        currentPresets.remove(player.getUniqueId());
        PlayerProfile original = originalSkins.remove(player.getUniqueId());
        if (original == null) {
            return false;
        }
        player.setPlayerProfile(original);
        return true;
    }

    /** A player who rejoins gets their real profile back from the server, so forget the mask. */
    public void forget(Player player) {
        currentPresets.remove(player.getUniqueId());
        originalSkins.remove(player.getUniqueId());
    }

    public SignedSkin getPreset(String presetName) {
        return presetName == null ? null : presetSkins.get(normalize(presetName));
    }

    public boolean hasPreset(String presetName) {
        return getPreset(presetName) != null;
    }

    public boolean isMasked(Player player) {
        return originalSkins.containsKey(player.getUniqueId());
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}
