package jenny.werewolf.papermc.plugin;

import java.util.Collections;
import java.util.PriorityQueue;

import org.bukkit.block.Biome;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Display;
import com.destroystokyo.paper.profile.PlayerProfile;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import net.kyori.adventure.title.Title;
import org.bukkit.Sound;


import java.time.Duration;

import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class Handler{
    GamePhase currentPhase;
    GamePhase nextPhase;
    Game game;
    ExamplePlugin plugin;
    int dayCount=1;
    public int maxWolfVote=0;
    int voteTimeLeft=-1;
    WWPlayer sentenced=null;
    float bladeVel=0;
    BlockDisplay guillotineBlade;
    BlockDisplay guillotineChain;
    ItemDisplay sentencedHead;
    int headFrame=0;

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
                    if(wwp!=null&&
                    (wwp.getHouse()!=null)){
                        if(wwp.isAlive())wwp.getHouse().closeDoors();
                        else wwp.getHouse().openDoors();
                        if(wwp.getHouse().contains(player.getLocation())&&wwp.getHouse().tpout!=null&&wwp.isAlive())
                        player.teleport(wwp.getHouse().tpout);

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
                        voteTimeLeft=-1;
                        initPhase(GamePhase.VOTING);
                    }, 
                    //on tick
                    (timeLeft) -> {

                    });
                break;
            case VOTING:
                // Initialize voting phase
                plugin.votingDisplayManager.clear();
                plugin.votingDisplayManager.show();
                
                sentenced=null;
                for (WWPlayer WWPlayer : game.getWWPlayerList()) {
                    WWPlayer.setVotedFor(null);
                    WWPlayer.clearVotes();
                }
                for(Player p: Bukkit.getServer().getOnlinePlayers()){
                    WWPlayer wwp=game.getWWPlayer(p.getName());
                        if(wwp!=null&&wwp.isAlive()){
                            p.setInvisible(false);
                        }
                }
                if(voteTimeLeft<0){
                    for(Player p : Bukkit.getServer().getOnlinePlayers()){
                                    Component title = Component.text(
                            "VOTING",
                            NamedTextColor.GREEN
                    );
                    Component subtitle = Component.text(
                            "Use \"/ww vote\" to vote someone up to the stand",
                            NamedTextColor.GRAY
                        );
                    
                    

                    Title.Times times = Title.Times.times(
                            Duration.ofMillis(500),  // Fade in
                            Duration.ofSeconds(3),  // Stay
                            Duration.ofMillis(500)  // Fade out
                    );

                    p.showTitle(Title.title(title, subtitle, times));
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
                            voteTimeLeft=-1;
                            initPhase(GamePhase.DUSK);
                        }, 
                        //on tick
                        (timeLeft) -> {
                                voteTimeLeft=timeLeft;
                        });
                }else{
                    plugin.timerInstance.start(
                        (int)Math.ceil((double)voteTimeLeft/20), 
                        BossBar.bossBar(
                            Component.text("VOTING - DAY " + dayCount, NamedTextColor.GREEN),
                            1.0f,
                            BossBar.Color.GREEN,
                            BossBar.Overlay.PROGRESS
                        ), 
                        game, 
                        //on complete
                        () -> {
                            voteTimeLeft=-1;
                            initPhase(GamePhase.DUSK);
                        }, 
                        //on tick
                        (timeLeft) -> {
                                voteTimeLeft=timeLeft;
                        });
                }
                    
                break;
            case DEFENSE:
                // Initialize defense phase
                for(Player p : Bukkit.getServer().getOnlinePlayers()){
                                    Component title = Component.text(
                            "TRIAL",
                            NamedTextColor.GOLD
                    );
                    Component subtitle=Component.text("");
                    if(sentenced!=null){
                         subtitle = Component.text(
                            sentenced.getName()+" has been voted up to the stand!",
                            NamedTextColor.GRAY
                        );
                    }
                    

                    Title.Times times = Title.Times.times(
                            Duration.ofMillis(500),  // Fade in
                            Duration.ofSeconds(3),  // Stay
                            Duration.ofMillis(500)  // Fade out
                    );

                    p.showTitle(Title.title(title, subtitle, times));
                }
                for(Player p: Bukkit.getServer().getOnlinePlayers()){
                    WWPlayer wwp=game.getWWPlayer(p.getName());
                    if(wwp!=null){
                        if(wwp.getName().equals(sentenced.getName())){
                            if(plugin.sceneManager.getLocation("trial_stage")!=null)
                            p.teleport(plugin.sceneManager.getLocation("trial_stage"));
                        }else{
                            if(plugin.sceneManager.getLocation("trial_audience")!=null)
                            p.teleport(plugin.sceneManager.getLocation("trial_audience"));
                            p.setInvisible(true);
                        }
                            
                    }
                }
                for(WWPlayer wwp: game.getWWPlayerList()){
                    if(wwp.isAlive()){
                wwp.setPrivateChat(true);
                wwp.setPrivateChatPlayers(null);
                wwp.setPrivateChatPrefix(Component.text("[ERROR]",NamedTextColor.RED));
                    }
                }
                sentenced.setPrivateChat(false);
                
                plugin.votingDisplayManager.hide();
                plugin.timerInstance.start(
                    Times.DEFENSE, 
                    BossBar.bossBar(
                        Component.text("DEFENSE - DAY " + dayCount, NamedTextColor.GOLD),
                        1.0f,
                        BossBar.Color.YELLOW,
                        BossBar.Overlay.PROGRESS
                    ), 
                    game, 
                    //on complete
                    () -> {
                        
                        initPhase(GamePhase.JUDGMENT);
                    }, 
                    //on tick
                    (timeLeft) -> {
                        
                    });
                break;
            case JUDGMENT:
                // Initialize judgment phase
                for(Player p : Bukkit.getServer().getOnlinePlayers()){
                                    Component title = Component.text(
                            "JUDGMENT",
                            NamedTextColor.LIGHT_PURPLE
                    );
                    Component subtitle = Component.text(
                            "/ww vote INNOCENT or GUILTY",
                            NamedTextColor.GRAY
                        );
                    
                    

                    Title.Times times = Title.Times.times(
                            Duration.ofMillis(500),  // Fade in
                            Duration.ofSeconds(3),  // Stay
                            Duration.ofMillis(500)  // Fade out
                    );

                    p.showTitle(Title.title(title, subtitle, times));
                }
                for(WWPlayer wwp: game.getWWPlayerList()){
                    if(wwp.isAlive()){
                wwp.setPrivateChat(false);
                wwp.setPrivateChatPlayers(null);
                wwp.setPrivateChatPrefix(Component.text("[ERROR]",NamedTextColor.RED));
                    }
                }
                
                plugin.timerInstance.start(
                    Times.JUDGMENT, 
                    BossBar.bossBar(
                        Component.text("JUDGMENT - DAY " + dayCount, NamedTextColor.LIGHT_PURPLE),
                        1.0f,
                        BossBar.Color.PINK,
                        BossBar.Overlay.PROGRESS
                    ), 
                    game, 
                    //on complete
                    () -> {
                        int judgment=0;
                        for(WWPlayer p : game.getWWPlayerList()){
                            if(!p.isAlive())continue;
                            judgment+=p.getJudgment();
                            if(p.getJudgment()>0){
                                Component message = Component.empty()
            .append(Component.text(p.getName(), NamedTextColor.WHITE))
            .append(Component.text(" voted ", NamedTextColor.YELLOW))
            .append(Component.text("GUILTY", NamedTextColor.RED));

    Bukkit.getServer().broadcast(message);
                            }else if(p.getJudgment()<0){
                                Component message = Component.empty()
            .append(Component.text(p.getName(), NamedTextColor.WHITE))
            .append(Component.text(" voted ", NamedTextColor.YELLOW))
            .append(Component.text("INNOCENT", NamedTextColor.GREEN));

    Bukkit.getServer().broadcast(message);
                            }else{
                                 Component message = Component.empty()
            .append(Component.text(p.getName(), NamedTextColor.WHITE))
            .append(Component.text(" ABSTAINED", NamedTextColor.BLUE));

    Bukkit.getServer().broadcast(message);
                            }
                            p.setJudgment(0);
                        }
                        if(judgment>0){
                            
                            initPhase(GamePhase.SENTENCE);
                            
                        game.killWWPlayer(sentenced);
                        }else initPhase(GamePhase.VOTING);
                    }, 
                    //on tick
                    (timeLeft) -> {
                        
                    });
                break;
            case SENTENCE:
                // Initialize sentence phase
                resetGuillotine();
                sentencedHead=
                plugin.sceneManager.getLocation("guillotine_blade2").getWorld().spawn(
                    plugin.sceneManager.getLocation("guillotine_blade2").getBlock().getLocation(), 
                    ItemDisplay.class, display -> {
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

                        if (!(head.getItemMeta() instanceof SkullMeta skullMeta)) {
                            throw new IllegalStateException("PLAYER_HEAD did not provide SkullMeta");
                        }

                        PlayerProfile profile = Bukkit.createProfile(sentenced.getName());
                        skullMeta.setPlayerProfile(profile);
                        head.setItemMeta(skullMeta);

                    display.setItemStack(head);
                    // display.setBrightness(new Display.Brightness(15, 15));
                    display.setViewRange(256f);

                    display.setShadowRadius(0f);
                    display.setShadowStrength(0f);

                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(2);

                    display.setPersistent(false);
                    display.setVisibleByDefault(true);
                     Matrix4f matrix = new Matrix4f()
                    .translate(0.5f, 0.8f, 0.6f);
         ;   //.translate((float)((housingManager.close.getX()-housingManager.open.getX())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2) );

                    display.setTransformationMatrix(matrix);
                    display.addScoreboardTag("ww_guillotine");
                });


                for(Player p: Bukkit.getServer().getOnlinePlayers()){
                    WWPlayer wwp=game.getWWPlayer(p.getName());
                    if(wwp!=null){
                            if(plugin.sceneManager.getLocation("execution_audience")!=null)
                            p.teleport(plugin.sceneManager.getLocation("execution_audience"));
                            p.setInvisible(true);
                    }
                }
                for(WWPlayer wwp: game.getWWPlayerList()){
                    if(wwp.isAlive()){
                wwp.setPrivateChat(true);
                wwp.setPrivateChatPlayers(null);
                wwp.setPrivateChatPrefix(Component.text("[ERROR]",NamedTextColor.RED));
                    }
                }
                sentenced.setPrivateChat(false);
                
                plugin.timerInstance.start(
                    Times.SENTENCE, 
                    BossBar.bossBar(
                        Component.text("DEATH SENTENCE", NamedTextColor.RED),
                        1.0f,
                        BossBar.Color.RED,
                        BossBar.Overlay.PROGRESS
                    ), 
                    game, 
                    //on complete
                    () -> {
                        sentencedHead.remove();
                        sentencedHead=null;
                        
                        game.killWWPlayer(sentenced);
                        sentenced.setPrivateChat(true);
        sentenced.setPrivateChatPlayers(game.dead);
        sentenced.setPrivateChatPrefix(Component.text("[DEAD]",NamedTextColor.GRAY));
                        if(!sentenced.killed()){
                                for(Player p: Bukkit.getServer().getOnlinePlayers()){
                                    if(p.getName().equals(sentenced.getName()))p.kill();
                                }
                            }
                        for(Player p: Bukkit.getServer().getOnlinePlayers()){
                    WWPlayer wwp=game.getWWPlayer(p.getName());
                        if(wwp!=null&&wwp.isAlive()){
                                p.setInvisible(false);
                        }
                }
                        initPhase(GamePhase.DUSK);
                        // initPhase(GamePhase.SENTENCE);
                    }, 
                    //on tick
                    (timeLeft) -> {
                        int start=(10-6)*20;
                        if(timeLeft==start&&plugin.sceneManager.getLocation("guillotine_blade1")!=null){
                            plugin.sceneManager.getLocation("guillotine_blade1").getWorld().playSound(
                                    plugin.sceneManager.getLocation("guillotine_blade1"),
                                    Sound.BLOCK_CHEST_LOCKED,
                                    1.0f, // volume
                                    0.1f  // pitch
                            );
                        }
                        if(timeLeft<start){
                                    float gravity=-0.1f;
                                bladeVel+=gravity;
                                if(guillotineBlade.getLocation().add(0,bladeVel,0).getY()<plugin.sceneManager.getLocation("guillotine_blade2").getY()){
                                    if(!sentenced.killed()){
                                        if(plugin.sceneManager.getLocation("guillotine_blade2")!=null){
                                            plugin.sceneManager.getLocation("guillotine_blade2").getWorld().playSound(
                                                    plugin.sceneManager.getLocation("guillotine_blade2"),
                                                    Sound.ENTITY_PLAYER_ATTACK_SWEEP,
                                                    1.0f, // volume
                                                    0.1f  // pitch
                                            );
                                            plugin.sceneManager.getLocation("guillotine_blade2").getWorld().playSound(
                                                    plugin.sceneManager.getLocation("guillotine_blade2"),
                                                    Sound.ENTITY_PLAYER_ATTACK_STRONG,
                                                    1.0f, // volume
                                                    2f  // pitch
                                            );
                                        }
                                        Bukkit.getScheduler().runTask(plugin, () -> {
                                            Matrix4f endMatrix = new Matrix4f()
                                                    .translate(0.5f, -0.3f, 0.2f)
                                                    .rotateX((float) -Math.toRadians(90));

                                            sentencedHead.setInterpolationDelay(0);
                                            sentencedHead.setInterpolationDuration(5);
                                            sentencedHead.setTransformationMatrix(endMatrix);
                                        });
                        sentenced.setPrivateChat(true);
        sentenced.setPrivateChatPlayers(game.dead);
        sentenced.setPrivateChatPrefix(Component.text("[DEAD]",NamedTextColor.GRAY));
                                        for(Player p: Bukkit.getServer().getOnlinePlayers()){
                                            if(p.getName().equals(sentenced.getName()))p.kill();
                                        }
                                    }
                                    guillotineBlade.teleport(plugin.sceneManager.getLocation("guillotine_blade2").getBlock().getLocation());
                                    guillotineChain.teleport(plugin.sceneManager.getLocation("guillotine_blade2").getBlock().getLocation());
                                    
                                    
                                    
                                    
                                }else{
                                    
                                guillotineBlade.teleport(guillotineBlade.getLocation().add(0,bladeVel,0));
                                    guillotineChain.teleport(guillotineChain.getLocation().add(0,bladeVel,0));
                                
                            }
                                
                                Matrix4f matrix = new Matrix4f()
                            .translate(0f,1f,0.25f)
                            .scale(1f,(float)(1+plugin.sceneManager.getLocation("guillotine_blade1").getBlock().getLocation().getY()-guillotineChain.getLocation().getY()),1f);
                            guillotineChain.setTransformationMatrix(matrix);
                        }
                        
                    });
                break;
            case DUSK:
                // Initialize dusk phase
                for(WWPlayer wwp: game.getWWPlayerList()){
                    if(wwp.isAlive()){
                wwp.setPrivateChat(false);
                wwp.setPrivateChatPlayers(null);
                wwp.setPrivateChatPrefix(Component.text("[ERROR]",NamedTextColor.RED));
                    }
                }
                plugin.votingDisplayManager.hide();
                for (WWPlayer wwp : game.getWWPlayerList()) {
                    
                    wwp.getRole().dayAbility(this);
                    if(wwp.getHouse()!=null)
                    wwp.getHouse().openDoors();
                }

                pushEvent(WWEvent.of((0-9999),()->{
                    for (WWPlayer p : game.getWWPlayerList()){
                        p.setTarget(null);
                    }
                }));
                flushEvents();
                for(Player p : Bukkit.getServer().getOnlinePlayers()){
                                    Component title = Component.text(
                            "Night is falling...",
                            NamedTextColor.DARK_PURPLE
                    );

                    Component subtitle = Component.text(
                            "Return to your house",
                            NamedTextColor.GRAY
                    );

                    Title.Times times = Title.Times.times(
                            Duration.ofMillis(500),  // Fade in
                            Duration.ofSeconds(3),  // Stay
                            Duration.ofMillis(500)  // Fade out
                    );

                    p.showTitle(Title.title(title, subtitle, times));
                }
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
                            plugin.setBiomeVolume(plugin.sceneManager.getLocation("town_corner1"),plugin.sceneManager.getLocation("town_corner2"),plugin.fogBiome);
                            for(Player player : Bukkit.getServer().getOnlinePlayers()){
                                WWPlayer wwp=game.getWWPlayer(player.getName());
                                if(wwp!=null&&wwp.isAlive()&&!wwp.getHouse().contains(player.getLocation())){
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
                resetGuillotine();
                for(Player player : Bukkit.getServer().getOnlinePlayers()){
                    WWPlayer wwp=game.getWWPlayer(player.getName());

                    if(wwp!=null){
                        if((wwp.getHouse()!=null)){

                            if(wwp.isAlive()){
                                
                                wwp.getHouse().closeDoors();
                            }
                            if(!wwp.getHouse().contains(player.getLocation())&&wwp.isAlive())player.teleport(wwp.getHouse().location);

                        }
                        if(wwp.getRole().werewolf&&plugin.sceneManager.getLocation("wolf_den")!=null&&wwp.isAlive()){
                            player.teleport(plugin.sceneManager.getLocation("wolf_den"));
                            plugin.skinMasker.setSkin(player, "bluey");
                        }else{
                            for (Player viewer : Bukkit.getOnlinePlayers()) {
                                WWPlayer viewerWWP = game.getWWPlayer(viewer.getName());
                                if(viewerWWP==null)continue;
                                if(!viewerWWP.isAlive())continue;
                                if (!viewer.equals(player)) {
                                    viewer.hidePlayer(plugin, player);
                                }
                            }
                        }
                    }
                    
                    
                }
                for(WWPlayer wwp: game.getWWPlayerList()){
                    if(wwp.isAlive()){

                    
                            if(wwp.getRole().werewolf){
                                
                wwp.setPrivateChat(true);
                wwp.setPrivateChatPlayers(game.wolves);
                wwp.setPrivateChatPrefix(Component.text("[WEREWOLF]",NamedTextColor.RED));
                            }else{
                                
                wwp.setPrivateChat(true);
                wwp.setPrivateChatPlayers(null);
                wwp.setPrivateChatPrefix(Component.text("[ERROR]",NamedTextColor.RED));
                            }
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
                maxWolfVote=0;
                for (WWPlayer WWPlayer : game.getWWPlayerList()) {
                    WWPlayer.clearVotes();
                    WWPlayer.getRole().nightAbility(this);
                }
                //WOLF KILL
                pushEvent(WWEvent.of(60,()->{
                    Collections.shuffle(game.getWWPlayerList());
                    for (WWPlayer WWPlayer : game.getWWPlayerList()){
                        if(!WWPlayer.isAlive())continue;
                        if(maxWolfVote>0&&WWPlayer.getVote()==maxWolfVote){
                            
                            WWPlayer.getRole().attacked=true;
                            
                            if(WWPlayer.getRole().def<1
                            // &&!WWPlayer.getRole().doctored
                        ){
                               if(!WWPlayer.getRole().doctored) game.killWWPlayer(WWPlayer);
                               else game.knockedOut.add(WWPlayer);
                                for(Player p : Bukkit.getServer().getOnlinePlayers()){
                                    
                                    if(p.getName().equals(WWPlayer.getName())){
                                        p.teleport(plugin.sceneManager.getLocation("wolf_den"));
                                        for (Player viewer : Bukkit.getOnlinePlayers()) {

                                            if (!viewer.equals(p)) {
                                                viewer.showPlayer(plugin, p);
                                            }
                                        }
                                    }
                                }
        //                         if(WWPlayer.getRole().doctored){
        //                             BukkitRunnable task = new BukkitRunnable() {
          

        //     @Override
        //     public void run() {
        //         for(Player p : Bukkit.getServer().getOnlinePlayers()){
                                    
        //                             if(p.getName().equals(WWPlayer.getName())){
        //                                 p.teleport(WWPlayer.getHouse().location);
        //                                 p.sendMessage(Component.text("You were saved by a doctor!", NamedTextColor.GREEN));
                    
        //                             }
        //                         }
        //     }
        // };
        // task.runTaskLater(plugin, Times.POST_NIGHT/2);
        //                         }
                                break;
                            }
                        }
                    }
                    for(Player player : Bukkit.getServer().getOnlinePlayers()){
                                WWPlayer wwp=game.getWWPlayer(player.getName());
                                if(wwp!=null&&wwp.isAlive()&&game.wolves.contains(wwp)){
                                    player.addPotionEffect(
                                            new PotionEffect(
                                                PotionEffectType.STRENGTH,
                                                Times.POST_NIGHT*20, // 10 seconds (20 ticks = 1 second)
                                                2,       // amplifier
                                                false,   // ambient
                                                false,   // particles
                                                false    // icon
                                            )
                                        );

                                }
                            
                            }
                }));
                pushEvent(WWEvent.of((0-9999),()->{
                    for (WWPlayer p : game.getWWPlayerList()){
                        p.setTarget(null);
                        p.getRole().attacked=false;
                    }
                }));
                flushEvents();
                plugin.setWorldTime(18000L);
                plugin.timerInstance.start(
                    Times.POST_NIGHT, 
                    BossBar.bossBar(
                        Component.text("Dawn breaks... ", NamedTextColor.DARK_PURPLE),
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
 plugin.setBiomeVolume(plugin.sceneManager.getLocation("town_corner1"),plugin.sceneManager.getLocation("town_corner2"),Biome.TAIGA);
 
 for (WWPlayer wwp : game.getWWPlayerList()){
                    if(wwp.isAlive()){
                        
        wwp.setPrivateChat(false);
        wwp.setPrivateChatPlayers(null);
        wwp.setPrivateChatPrefix(Component.text("[ERROR]",NamedTextColor.RED));
                    }
                    if(wwp.getHouse()!=null){
                        
                        wwp.getHouse().openDoors();
                        }
                        
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            plugin.skinMasker.restoreSkin(p);
                    if(!wwp.isAlive()){
                        if(p.getName().equals(wwp.getName())){
                            if(!wwp.killed())p.kill();
                            if(wwp.getHouse()!=null)plugin.inventoryManager.copyBookToLectern(p, wwp.getHouse().lectern);
                        }
                    }
                    if(game.knockedOut.contains(wwp)){
                        if(p.getName().equals(wwp.getName()))p.kill();
                    }
            if(wwp.getHouse()!=null&&wwp.getName().equals(p.getName())&&wwp.isAlive())if(wwp.getRole().werewolf)p.teleport(wwp.getHouse().location);
                    
        }
                    
                }
                
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            WWPlayer wwp = game.getWWPlayer(p.getName());
            if(wwp==null)continue;
            if(wwp.isAlive()){
                for (Player viewer : Bukkit.getOnlinePlayers()) {

                                            if (!viewer.equals(p)) {
                                                viewer.showPlayer(plugin, p);
                                            }
                                        }
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
                                if(wwp!=null&&wwp.getHouse().contains(player.getLocation())){
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



public void resetGuillotine(){
    if(plugin.sceneManager.getLocation("guillotine_blade1")==null||plugin.sceneManager.getLocation("guillotine_blade2")==null)return;
    ExamplePlugin.removeEntitiesWithTag("ww_guillotine");

    if(sentencedHead!=null){
        sentencedHead.remove();
        sentencedHead=null;
    }
                guillotineBlade=plugin.sceneManager.getLocation("guillotine_blade1").getWorld().spawn(
                    plugin.sceneManager.getLocation("guillotine_blade1").getBlock().getLocation(), BlockDisplay.class, display -> {
                    display.setBlock(Material.IRON_BLOCK.createBlockData());
                    // display.setBrightness(new Display.Brightness(15, 15));
                    display.setViewRange(256f);

                    display.setShadowRadius(0f);
                    display.setShadowStrength(0f);

                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(2);

                    display.setPersistent(false);
                    display.setVisibleByDefault(true);
                    Matrix4f matrix = new Matrix4f()
                    .translate(-0.25f,0.1f,0.75f)
                    .scale(1.5f,1f,0.0625f);
                ;   //.translate((float)((housingManager.close.getX()-housingManager.open.getX())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2) );

                            display.setTransformationMatrix(matrix);
                            display.addScoreboardTag("ww_guillotine");
                        });
                
                
                guillotineChain=plugin.sceneManager.getLocation("guillotine_blade1").getWorld().spawn(
                    plugin.sceneManager.getLocation("guillotine_blade1").getBlock().getLocation(), BlockDisplay.class, display -> {
                    display.setBlock(Material.IRON_CHAIN.createBlockData());
                    // display.setBrightness(new Display.Brightness(15, 15));
                    display.setViewRange(256f);

                    display.setShadowRadius(0f);
                    display.setShadowStrength(0f);

                    display.setInterpolationDelay(0);
                    display.setInterpolationDuration(2);

                    display.setPersistent(false);
                    display.setVisibleByDefault(true);
                    Matrix4f matrix = new Matrix4f()
                    .translate(0f,1f,0.25f)
                    .scale(1f,1f,1f);
                
         ;   //.translate((float)((housingManager.close.getX()-housingManager.open.getX())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2) );

                    display.setTransformationMatrix(matrix);
                    display.addScoreboardTag("ww_guillotine");
                });
            
//                 sentencedHead=plugin.sceneManager.getLocation("guillotine_blade1").getWorld().spawn(
//                     plugin.sceneManager.getLocation("guillotine_blade1").getBlock().getLocation(), ItemDisplay.class, display -> {
//                         ItemStack head = new ItemStack(Material.PLAYER_HEAD);

// if (!(head.getItemMeta() instanceof SkullMeta skullMeta)) {
//     throw new IllegalStateException("PLAYER_HEAD did not provide SkullMeta");
// }

// PlayerProfile profile = Bukkit.createProfile(sentenced.getName());
// skullMeta.setPlayerProfile(profile);
// head.setItemMeta(skullMeta);

//                     display.setItemStack(head);
//                     display.setBrightness(new Display.Brightness(15, 15));
//                     display.setViewRange(256f);

//                     display.setShadowRadius(0f);
//                     display.setShadowStrength(0f);

//                     display.setInterpolationDelay(0);
//                     display.setInterpolationDuration(2);

//                     display.setPersistent(false);
//                     display.setVisibleByDefault(true);
//                      Matrix4f matrix = new Matrix4f()
//                     .translate(0.5f, 0.8f, 0.6f);
//          ;   //.translate((float)((housingManager.close.getX()-housingManager.open.getX())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2) );

//                     display.setTransformationMatrix(matrix);
//                     display.addScoreboardTag("ww_guillotine");
//                 });
}
}

