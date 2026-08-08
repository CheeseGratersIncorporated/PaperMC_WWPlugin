package jenny.werewolf.papermc.plugin;

import com.destroystokyo.paper.profile.ProfileProperty;
import com.github.retrooper.packetevents.protocol.player.TextureProperty;

import java.util.Objects;

public record SignedSkin(
        String value,
        String signature
) {
    public SignedSkin {
        Objects.requireNonNull(value, "value");
        Objects.requireNonNull(signature, "signature");
    }

    public ProfileProperty toPaperProperty() {
        return new ProfileProperty(
                "textures",
                value,
                signature
        );
    }

    public TextureProperty toPacketEventsProperty() {
        return new TextureProperty(
                "textures",
                value,
                signature
        );
    }
}