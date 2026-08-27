package jenny.werewolf.papermc.plugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import jenny.werewolf.papermc.plugin.WerewolfPlugin;
import jenny.werewolf.papermc.plugin.WWPlayer;
import jenny.werewolf.papermc.plugin.House;
import jenny.werewolf.papermc.plugin.Perm;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title.Times;
import net.kyori.adventure.bossbar.BossBar;

import org.bukkit.Sound;
import org.bukkit.block.Biome;


public class StartCommand {
    public static LiteralArgumentBuilder<CommandSourceStack> create(WerewolfPlugin plugin) {
        return Commands.literal("start")
        .requires(source -> {
                if( source.getSender() instanceof Player p){
                    if(plugin.permMap.get(p.getName())==Perm.DEV)return true;
                    else return (plugin.permMap.get(p.getName())==Perm.HOST)&&(!plugin.gameInstance.isActive());
                }
                return false;
        })
            .executes(ctx -> {
                plugin.housingManager.closeAllDoors();
                plugin.sceneManager.load();
                plugin.housingManager.clearViewers();
                    Bukkit.getServer().broadcast(Component.text("loaded scenes" , NamedTextColor.GREEN));
                    plugin.housingManager.forceClaim(plugin.gameInstance.getWWPlayerList());
             plugin.setBiomeVolume(plugin.sceneManager.getLocation("town_corner1"),plugin.sceneManager.getLocation("town_corner2"),plugin.fogBiome);
                for(House h: plugin.housingManager.getHouses()){
                    if(h.lectern!=null){
                        plugin.inventoryManager.wipeLecternBook(h.lectern);
                    }
                }
                plugin.timerInstance.start(
                    5, 
                    BossBar.bossBar(
                        Component.text("Werewolf Game Starting...", NamedTextColor.RED),
                        1.0f,
                        BossBar.Color.RED,
                        BossBar.Overlay.PROGRESS
                    ), 
                    plugin.gameInstance, 
                    //on complete
                    () -> {
                        
                        plugin.gameInstance.startGame();
                    }, 
                    //on tick
                    (timeLeft) -> {

                    });
                            for(Player p : Bukkit.getServer().getOnlinePlayers()){
                                for (Player viewer : Bukkit.getOnlinePlayers()) {
                        viewer.showPlayer(plugin, p);
                    
                                    }
                                    if(plugin.gameInstance.getWWPlayer(p.getName())==null){
                                        p.setRespawnLocation(plugin.sceneManager.getLocation("default_spawnpoint"),true);
                                        p.setInvisible(true);
                                        p.setCollidable(true);
                                    }else{
                                        if(plugin.gameInstance.getWWPlayer(p.getName()).getHouse()!=null){
                                            p.setRespawnLocation(plugin.gameInstance.getWWPlayer(p.getName()).getHouse().location,true);
                                        }
                                    }
                                p.playSound(
                                        p.getLocation(),
                                        Sound.BLOCK_END_PORTAL_SPAWN,
                                        0.2f,  // volume
                                        1.0f   // pitch
                                );
                                p.getWorld().setGameRule(GameRules.IMMEDIATE_RESPAWN, true);
                                p.getWorld().setGameRule(GameRules.PVP, true);
                                
                                p.getInventory().clear();
                                p.setGameMode(GameMode.ADVENTURE);
                                p.setAllowFlight(false);
                                p.setInvisible(false);
                                p.kill();

                                p.addPotionEffect(
                                                    new PotionEffect(
                                                        PotionEffectType.BLINDNESS,
                                                        20 * 3, // 10 seconds (20 ticks = 1 second)
                                                        255,       // amplifier
                                                        false,   // ambient
                                                        false,   // particles
                                                        false    // icon
                                                    )
                                                );
                                    }
                return Command.SINGLE_SUCCESS;
            });
    }
}
