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

public class SkinMasker {

    /*
     * Preset name -> Minecraft texture URL
     *
     * Preset names are stored in lowercase, so lookups are
     * case-insensitive.
     */
    private final Map<String, String> presetSkins = new HashMap<>();

    /*
     * Player UUID -> profile before the first mask was applied.
     *
     * putIfAbsent is important: applying multiple masks will not
     * overwrite the player's true original skin.
     */
    private final Map<UUID, PlayerProfile> originalSkins = new HashMap<>();

    public SkinMasker() {
        registerPreset(
                "bluey",
                "https://textures.minecraft.net/texture/"
                        + "8dc4e530cddb3d02f417b38e6378813b7ccc2da32feaee9612589e6fe8d605c6"
        );

        // Add more presets here:
        /*
        registerPreset(
                "werewolf",
                "https://textures.minecraft.net/texture/..."
        );

        registerPreset(
                "townie",
                "https://textures.minecraft.net/texture/..."
        );
        */
    }

    /**
     * Registers or replaces a preset skin.
     */
    public void registerPreset(
            String presetSkinName,
            String textureUrl
    ) {
        Objects.requireNonNull(
                presetSkinName,
                "presetSkinName"
        );

        Objects.requireNonNull(
                textureUrl,
                "textureUrl"
        );

        String normalizedName = normalizePresetName(presetSkinName);

        if (normalizedName.isBlank()) {
            throw new IllegalArgumentException(
                    "Preset skin name cannot be blank"
            );
        }

        // Validate the URL now instead of waiting until setSkin().
        parseTextureUrl(textureUrl);

        presetSkins.put(normalizedName, textureUrl);
    }

    /**
     * Applies a preset skin to a player.
     *
     * The player's original profile is saved only the first time
     * they are masked.
     */
    public void setSkin(
            Player player,
            String presetSkinName
    ) {
        Objects.requireNonNull(player, "player");
        Objects.requireNonNull(
                presetSkinName,
                "presetSkinName"
        );

        String normalizedName =
                normalizePresetName(presetSkinName);

        String textureUrl =
                presetSkins.get(normalizedName);

        if (textureUrl == null) {
            throw new IllegalArgumentException(
                    "Unknown skin preset: "
                            + presetSkinName
            );
        }

        /*
         * getPlayerProfile() returns a copy, so this stores an
         * independent copy of the player's original profile.
         *
         * putIfAbsent means:
         * original -> werewolf -> townie -> restore
         *
         * restores the true original rather than the werewolf skin.
         */
        originalSkins.putIfAbsent(
                player.getUniqueId(),
                player.getPlayerProfile()
        );

        PlayerProfile maskedProfile =
                player.getPlayerProfile();

        PlayerTextures textures =
                maskedProfile.getTextures();

        textures.setSkin(
                parseTextureUrl(textureUrl),
                PlayerTextures.SkinModel.CLASSIC
        );

        maskedProfile.setTextures(textures);
        player.setPlayerProfile(maskedProfile);
    }

    /**
     * Restores the player's profile from before the first mask.
     *
     * @return true if an original profile existed and was restored
     */
    public boolean restoreSkin(Player player) {
        Objects.requireNonNull(player, "player");

        PlayerProfile originalProfile =
                originalSkins.remove(player.getUniqueId());

        if (originalProfile == null) {
            return false;
        }

        player.setPlayerProfile(originalProfile);
        return true;
    }

    public boolean isMasked(Player player) {
        Objects.requireNonNull(player, "player");

        return originalSkins.containsKey(
                player.getUniqueId()
        );
    }

    public boolean hasPreset(String presetSkinName) {
        if (presetSkinName == null) {
            return false;
        }

        return presetSkins.containsKey(
                normalizePresetName(presetSkinName)
        );
    }

    /**
     * Returns a read-only view of the registered presets.
     */
    public Map<String, String> getPresetSkins() {
        return Collections.unmodifiableMap(presetSkins);
    }

    private static String normalizePresetName(String name) {
        return name.trim().toLowerCase(Locale.ROOT);
    }

    private static URL parseTextureUrl(String textureUrl) {
        try {
            URL url = URI.create(textureUrl).toURL();

            if (!url.getHost().equalsIgnoreCase(
                    "textures.minecraft.net"
            )) {
                throw new IllegalArgumentException(
                        "Skin URL must use textures.minecraft.net: "
                                + textureUrl
                );
            }

            return url;
        } catch (
                IllegalArgumentException
                | MalformedURLException exception
        ) {
            throw new IllegalArgumentException(
                    "Invalid skin texture URL: " + textureUrl,
                    exception
            );
        }
    }
}