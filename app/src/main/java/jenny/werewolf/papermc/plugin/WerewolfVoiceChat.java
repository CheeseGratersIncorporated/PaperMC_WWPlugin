package jenny.werewolf.papermc.plugin;

import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import de.maxhenkel.voicechat.api.events.MicrophonePacketEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStartedEvent;
import de.maxhenkel.voicechat.api.events.VoicechatServerStoppedEvent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class WerewolfVoiceChat implements VoicechatPlugin {

    private final JavaPlugin plugin;

    /*
     * This is accessed by the voice-chat networking thread,
     * so use a thread-safe set.
     */
    private final Set<UUID> mutedPlayers =
            ConcurrentHashMap.newKeySet();

    private volatile VoicechatServerApi voicechatApi;

    public WerewolfVoiceChat(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Must be unique among Simple Voice Chat addons.
     */
    @Override
    public String getPluginId() {
        return "werewolf";
    }

    /**
     * Voice Chat API events must be registered here.
     * They are not Bukkit events.
     */
    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(
                VoicechatServerStartedEvent.class,
                this::onVoicechatServerStarted
        );

        registration.registerEvent(
                VoicechatServerStoppedEvent.class,
                event -> {
                    voicechatApi = null;

                    plugin.getLogger().info(
                            "Simple Voice Chat server stopped"
                    );
                }
        );

        registration.registerEvent(
                MicrophonePacketEvent.class,
                this::onMicrophonePacket
        );
    }

    private void onVoicechatServerStarted(
            VoicechatServerStartedEvent event
    ) {
        voicechatApi = event.getVoicechat();

        plugin.getLogger().info(
                "Werewolf integration connected to Simple Voice Chat"
        );
    }

    /**
     * Prevents outgoing voice packets from muted players.
     */
    private void onMicrophonePacket(MicrophonePacketEvent event) {
        VoicechatConnection sender =
                event.getSenderConnection();

        if (sender == null) {
            return;
        }

        UUID senderId =
                sender.getPlayer().getUuid();

        if (mutedPlayers.contains(senderId)) {
            event.cancel();
        }
    }

    public void mute(Player player) {
        mutedPlayers.add(player.getUniqueId());
    }

    public void unmute(Player player) {
        mutedPlayers.remove(player.getUniqueId());
    }

    public boolean isMuted(Player player) {
        return mutedPlayers.contains(player.getUniqueId());
    }

    /**
     * Returns true when the player has a compatible
     * Simple Voice Chat client mod installed.
     */
    public boolean hasVoiceChatInstalled(Player player) {
        VoicechatServerApi api = voicechatApi;

        if (api == null) {
            return false;
        }

        VoicechatConnection connection =
                api.getConnectionOf(player.getUniqueId());

        return connection != null
                && connection.isInstalled();
    }

    /**
     * Returns true when the player's voice client is
     * installed and currently connected.
     */
    public boolean isVoiceChatConnected(Player player) {
        VoicechatServerApi api = voicechatApi;

        if (api == null) {
            return false;
        }

        VoicechatConnection connection =
                api.getConnectionOf(player.getUniqueId());

        return connection != null
                && connection.isInstalled()
                && connection.isConnected();
    }

    public boolean isReady() {
        return voicechatApi != null;
    }

    public VoicechatServerApi getApi() {
        return voicechatApi;
    }
}