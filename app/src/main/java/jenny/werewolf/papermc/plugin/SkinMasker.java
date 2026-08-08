package jenny.werewolf.papermc.plugin;
import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.entity.Player;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import com.destroystokyo.paper.profile.PlayerProfile;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public final class SkinMasker {

    private final Map<String, SignedSkin> presetSkins =
            new HashMap<>();

    private final Map<UUID, PlayerProfile> originalSkins =
            new HashMap<>();

    public void registerPreset(
            String presetName,
            String textureValue,
            String textureSignature
    ) {
        Objects.requireNonNull(presetName, "presetName");

        presetSkins.put(
                normalize(presetName),
                new SignedSkin(
                        textureValue,
                        textureSignature
                )
        );
    }

    public void setSkin(
            Player player,
            String presetName
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(presetName, "presetName");

        SignedSkin skin =
                presetSkins.get(normalize(presetName));

        if (skin == null) {
            throw new IllegalArgumentException(
                    "Unknown skin preset: " + presetName
            );
        }

        /*
         * Save the real original profile only once.
         */
        originalSkins.putIfAbsent(
                player.getUniqueId(),
                player.getPlayerProfile().clone()
        );

        PlayerProfile maskedProfile =
                player.getPlayerProfile();

        /*
         * Replace the complete texture property rather than
         * calling PlayerTextures#setSkin(URL).
         */
        maskedProfile.removeProperty("textures");
        maskedProfile.setProperty(
                skin.toPaperProperty()
        );

        player.setPlayerProfile(maskedProfile);
    }

    public boolean restoreSkin(Player player) {
        Objects.requireNonNull(player, "player");

        PlayerProfile original =
                originalSkins.remove(
                        player.getUniqueId()
                );

        if (original == null) {
            return false;
        }

        player.setPlayerProfile(original);
        return true;
    }

    public SignedSkin getPreset(String presetName) {
        if (presetName == null) {
            return null;
        }

        return presetSkins.get(
                normalize(presetName)
        );
    }

    public boolean hasPreset(String presetName) {
        return getPreset(presetName) != null;
    }

    public boolean isMasked(Player player) {
        return originalSkins.containsKey(
                player.getUniqueId()
        );
    }

    private static String normalize(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }
}