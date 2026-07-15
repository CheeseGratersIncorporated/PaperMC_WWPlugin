package jenny.werewolf.papermc.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class Handler{
    GamePhase currentPhase;
    GamePhase nextPhase;
    Game game;
    ExamplePlugin plugin;
    int dayCount=1;
    public int maxWolfVote=0;

    public Handler(Game game, ExamplePlugin plugin) {
        this.game = game;
        this.plugin = plugin;
    }
    //smaller priority goes first
        PriorityQueue<WWEvent> eventQueue = new PriorityQueue<>();
    
    public void pushEvent(WWEvent e){
        eventQueue.add(e);
    }
        public void flushEvents(){
        while(!eventQueue.isEmpty()){
            WWEvent e=eventQueue.poll();
            e.run();
        }
    }

    public void initPhase(GamePhase phase) {
        this.currentPhase = phase;
        //REMINDER: kill timers
        switch(phase) {
            case DISCUSSION:
                // Initialize discussion phase
                for(Player player : Bukkit.getServer().getOnlinePlayers()){
                    WWPlayer wwp=game.getWWPlayer(player.getName());
                    if(wwp!=null){
                            
                        player.teleport(wwp.getHouse().location);

                    }
                    
                }
                plugin.setWorldTime(6000L);
                plugin.timerInstance.start(
                    Times.DISCUSSION, 
                    BossBar.bossBar(
                        Component.text("DISCUSSION - DAY " + dayCount, NamedTextColor.YELLOW),
                        1.0f,
                        BossBar.Color.YELLOW,
                        BossBar.Overlay.PROGRESS
                    ), 
                    game, 
                    //on complete
                    () -> {
                        
                        initPhase(GamePhase.VOTING);
                    }, 
                    //on tick
                    (timeLeft) -> {

                    });
                break;
            case VOTING:
                // Initialize voting phase
                for (WWPlayer WWPlayer : game.getWWPlayerList()) {
                    WWPlayer.setVotedFor(null);
                    WWPlayer.clearVotes();
                }
                plugin.timerInstance.start(
                    Times.VOTING, 
                    BossBar.bossBar(
                        Component.text("VOTING - DAY " + dayCount, NamedTextColor.GREEN),
                        1.0f,
                        BossBar.Color.GREEN,
                        BossBar.Overlay.PROGRESS
                    ), 
                    game, 
                    //on complete
                    () -> {
                        
                        initPhase(GamePhase.DUSK);
                    }, 
                    //on tick
                    (timeLeft) -> {

                    });
                break;
            case DEFENSE:
                // Initialize defense phase
                break;
            case JUDGMENT:
                // Initialize judgment phase
                break;
            case SENTENCE:
                // Initialize sentence phase
                break;
            case DUSK:
                // Initialize dusk phase
                for (WWPlayer WWPlayer : game.getWWPlayerList()) {
                    
                    WWPlayer.getRole().dayAbility(this);
                }
                flushEvents();
                plugin.timerInstance.start(
                    Times.DUSK, 
                    BossBar.bossBar(
                        Component.text("Night is falling...", NamedTextColor.DARK_PURPLE),
                        1.0f,
                        BossBar.Color.PURPLE,
                        BossBar.Overlay.PROGRESS
                    ), 
                    game, 
                    //on complete
                    () -> {
                        
                        initPhase(GamePhase.NIGHT);
                    }, 
                    //on tick
                    (timeLeft) -> {
                        if(timeLeft==20){
                            for(Player player : Bukkit.getServer().getOnlinePlayers()){
                                WWPlayer wwp=game.getWWPlayer(player.getName());
                                if(wwp!=null){
                                    player.addPotionEffect(
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
                            
                            }
                        }
                        plugin.setWorldTime((long)((1- (float)timeLeft / (Times.DUSK * 20)) * 12000L + 6000L));
                    });
                break;
            case NIGHT:
                // Initialize night phase
                for(Player player : Bukkit.getServer().getOnlinePlayers()){
                    WWPlayer wwp=game.getWWPlayer(player.getName());
                    if(wwp!=null){
                            
                        player.teleport(wwp.getHouse().location);

                    }
                    
                }
                plugin.setWorldTime(18000L);
                plugin.timerInstance.start(
                    Times.NIGHT, 
                    BossBar.bossBar(
                        Component.text("NIGHT " + dayCount, NamedTextColor.BLUE),
                        1.0f,
                        BossBar.Color.BLUE,
                        BossBar.Overlay.PROGRESS
                    ), 
                    game, 
                    //on complete
                    () -> {
                        
                        initPhase(GamePhase.POST_NIGHT);
                    }, 
                    //on tick
                    (timeLeft) -> {

                    });
                break;
            case POST_NIGHT:
                for (WWPlayer WWPlayer : game.getWWPlayerList()) {
                    WWPlayer.clearVotes();
                    WWPlayer.getRole().nightAbility(this);
                }
                //WOLF KILL
                pushEvent(WWEvent.of(60,()->{
                    Collections.shuffle(game.getWWPlayerList());
                    for (WWPlayer WWPlayer : game.getWWPlayerList()){
                        if(WWPlayer.getVote()==maxWolfVote){
                            if(WWPlayer.getRole().def<1
                            &&!WWPlayer.getRole().doctored){
                                WWPlayer.setAlive(false);
                            }
                        }
                    }
                }));
                flushEvents();
                plugin.setWorldTime(18000L);
                plugin.timerInstance.start(
                    Times.POST_NIGHT, 
                    BossBar.bossBar(
                        Component.text("Dawn breaks... " + dayCount, NamedTextColor.DARK_PURPLE),
                        1.0f,
                        BossBar.Color.PURPLE,
                        BossBar.Overlay.PROGRESS
                    ), 
                    game, 
                    //on complete
                    () -> {
                        
                        initPhase(GamePhase.DAWN);
                    }, 
                    //on tick
                    (timeLeft) -> {
                        plugin.setWorldTime((long)((1- (float)timeLeft / (Times.POST_NIGHT * 20)) * 5000L + 18000L));
                    });
                break;
            case DAWN:
                // Initialize dawn phase
                for (WWPlayer wwp : game.getWWPlayerList()){
                    if(!wwp.isAlive()&&!wwp.killed()){
                        wwp.kill();
                    }
                }
                dayCount++; 
                plugin.timerInstance.start(
                    Times.DAWN, 
                    BossBar.bossBar(
                        Component.text("Cock-a-doodley-doo mfs", NamedTextColor.YELLOW),
                        1.0f,
                        BossBar.Color.YELLOW,
                        BossBar.Overlay.PROGRESS
                    ), 
                    game, 
                    //on complete
                    () -> {
                        
                        initPhase(GamePhase.DISCUSSION);
                    }, 
                    //on tick
                    (timeLeft) -> {
                        if(timeLeft==20){
                            for(Player player : Bukkit.getServer().getOnlinePlayers()){
                                WWPlayer wwp=game.getWWPlayer(player.getName());
                                if(wwp!=null){
                                    player.addPotionEffect(
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
                            
                            }
                        }
                        plugin.setWorldTime((long)((1- (float)timeLeft / (Times.DAWN * 20)) * 7000L + 23000L));
                    });
                break;
        }
        game.flushEvents();
    }
}

