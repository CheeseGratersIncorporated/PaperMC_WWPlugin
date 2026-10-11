package io.github.gegagegagega.werewolf.voice;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;
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

public final class WerewolfVoiceChat implements VoicechatPlugin, VoiceChat {

    private final JavaPlugin plugin;

    /*
     * This is accessed by the voice-chat networking thread,
     * so use a thread-safe set.
     */
    private final Set<UUID> mutedPlayers =
            ConcurrentHashMap.newKeySet();

    private volatile VoicechatServerApi voicechatApi;

    private WerewolfVoiceChat(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /** Only call when the voicechat plugin is present; see {@link VoiceChat#tryRegister}. */
    static VoiceChat register(JavaPlugin plugin) {
        BukkitVoicechatService service = plugin.getServer()
                .getServicesManager()
                .load(BukkitVoicechatService.class);
        if (service == null) {
            plugin.getLogger().warning("Simple Voice Chat service was not found");
            return null;
        }
        WerewolfVoiceChat voiceChat = new WerewolfVoiceChat(plugin);
        service.registerPlugin(voiceChat);
        plugin.getLogger().info("Registered Werewolf Simple Voice Chat integration");
        return voiceChat;
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

    @Override
    public void mute(Player player) {
        mutedPlayers.add(player.getUniqueId());
    }

    @Override
    public void unmute(Player player) {
        mutedPlayers.remove(player.getUniqueId());
    }

    @Override
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
    @Override
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