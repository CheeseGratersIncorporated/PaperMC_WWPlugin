package io.github.gegagegagega.werewolf.voice;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Optional Simple Voice Chat integration. This interface has no Simple Voice
 * Chat types in it, so the plugin loads fine when that plugin is missing.
 */
public interface VoiceChat {

    void mute(Player player);

    void unmute(Player player);

    boolean isMuted(Player player);

    boolean isVoiceChatConnected(Player player);

    /**
     * Registers with Simple Voice Chat if it is installed. Returns null otherwise.
     */
    static VoiceChat tryRegister(JavaPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("voicechat") == null) {
            plugin.getLogger().info("Simple Voice Chat not found; voice integration disabled");
            return null;
        }
        try {
            return WerewolfVoiceChat.register(plugin);
        } catch (LinkageError | RuntimeException e) {
            plugin.getLogger().warning("Could not hook into Simple Voice Chat: " + e);
            return null;
        }
    }
}
