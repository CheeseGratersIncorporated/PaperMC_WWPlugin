package jenny.werewolf.papermc.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerSpawnChangeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Matrix4f;

import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import com.destroystokyo.paper.event.player.PlayerPostRespawnEvent;

import de.maxhenkel.voicechat.api.BukkitVoicechatService;

import org.bukkit.event.Event;
import org.bukkit.entity.Entity;


import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;

import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;

import io.papermc.paper.event.entity.EntityEquipmentChangedEvent;
import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.NavigableSet;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import net.kyori.adventure.key.Key;
import org.bukkit.Registry;
import org.bukkit.block.Biome;

import jenny.werewolf.papermc.plugin.commands.WWCommand;

public class ExamplePlugin extends JavaPlugin implements Listener {
    public WerewolfVoiceChat voiceChat;
  public SkinMasker skinMasker;
  public Game gameInstance = new Game(this);
  public Timer timerInstance = new Timer(this);
  public HousingManager housingManager;
  public HousingDataManager housingDataManager;
  public VotingDisplayManager votingDisplayManager;
  public Scene sceneManager;
  public ControlledInventoryManager inventoryManager;
  public FakeTabManager fakeTabManager;
  public NameTagManager nameTagManager;
  public ItemHider itemHider;
  World world = getServer().getWorld("world");
  public Biome fogBiome;
  public HashMap<String, Perm> permMap = new HashMap<>();
  //public ArrayList<RotatingGhostBeam> beams = new ArrayList<RotatingGhostBeam>();


  @Override
  public void onEnable() {
    BukkitVoicechatService service =
                getServer()
                        .getServicesManager()
                        .load(BukkitVoicechatService.class);

        if (service == null) {
            getLogger().severe(
                    "Simple Voice Chat service was not found"
            );

            getServer()
                    .getPluginManager()
                    .disablePlugin(this);

            return;
        }

        voiceChat = new WerewolfVoiceChat(this);
        service.registerPlugin(voiceChat);

        getLogger().info(
                "Registered Werewolf Simple Voice Chat integration"
        );
    Registry<Biome> biomeRegistry = RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.BIOME);

    fogBiome = biomeRegistry.getOrThrow(
            Key.key("hehehe:foggy")
    );
    ConfigurationSerialization.registerClass(House.class);
    fakeTabManager=new FakeTabManager(this);
    nameTagManager=new NameTagManager();
    itemHider=new ItemHider();
    skinMasker=new SkinMasker();
    skinMasker.registerPreset("bluey",
    "ewogICJ0aW1lc3RhbXAiIDogMTcyMTM3Mjc5OTE0NiwKICAicHJvZmlsZUlkIiA6ICI5NWU5ODU1MzMwMmE0ZjdhYjRmNGQwYjZkZmE0MTkwMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJIX19JX19NIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhkYzRlNTMwY2RkYjNkMDJmNDE3YjM4ZTYzNzg4MTNiN2NjYzJkYTMyZmVhZWU5NjEyNTg5ZTZmZThkNjA1YzYiCiAgICB9CiAgfQp9",
    "T+7EA3YgK/AQIzp1+P2g5U6BHxzEKByRQ9qsFPzluoJNq4+I4j7FA1fz8HcFGUKdshvoBmwEoIUBT0/4sL1cZrG2OcBXq2QbcYU1UVJPjeLf1Dd/qYTbLv80lxjSKyQJCD7R/A0rHcZB0zDjyIu5SBc7UXSvQqFU+g1zuYsPhCGhruuhQzhfXJmNFZcmcR8hyAyTyCwzQJeVgnd9F4PdnMo0/Ef0Dy9V+Sw9PYuwtzneGThBCQWWNKXJIiXBcqDaEZovtsbe6wWpCQuXApPxwtoSfIynYawUfPBz+jYSR/b/vCPoG75Om485pB7hWBA7I1MIu9W41NC3QI6VhYI/XU3P41BT4OV67LeQp1SjIW2hEsr2Bg4D57v6aiAeLNGoxIAOgRzb8AA8XO6ATiLXg9H6JuhTj6EYo+Sh4qq447UTeDWVqP2MMPqYk1AqMb86fU7S6BdfMAB+PcpxEEH8aQNEQnaW4F8baYCq/MY5KAvG9Wnb9wrc1nsmy/YiFCAuIYSCOFcQYJRc4xtclvgtXc6a1KGPvuE9D9qg6wnPSht4G2GYBLVvHdd6bHEYnwJdG986i4kTKkX/yM3Ltdm2cJTW0kmYol0D7J42bLgV7WeYR3mtp/U7rN3CkmuRpoU77Xxhd6tpv3I4Km3mvvxSKFtHJNbA3mXJjeWrxATtRP4="
    );
    skinMasker.registerPreset("werewolf",
    "ewogICJ0aW1lc3RhbXAiIDogMTc4NjI0NjQ5NDU1NiwKICAicHJvZmlsZUlkIiA6ICI3OTI0MDQ0N2ViNTk0ZDBkOTVmY2E3YmJiMWZiMjVhNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJqdW5pb3JraW9iciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82MTBhNTc1ZjZlYjY4MjQwM2E5NjY4MzdjMjlhMGZmZjc0YzVlZjEzNWJkZjdhOGNlZDFkODJiYTkxYTUxYWYxIgogICAgfQogIH0KfQ==",
    "ooQs1Sotly4wwtdOX3IFhOKS6DnqYPwyNe9WJ+jo4trZFs1He8322XEya13jHeribPVHjDmIXgbIqfIZ2B5BIBw965Hy+8L8K/R7QidXDvjVrLFw3F/34ex1kAWLP+uuo4jQxGvS6reKKGNdIzc5dUjSu/GhsYUn0LpVxZvz3li3qiUOuCMeYAKdmfhS+5Is+z9BqNIUlpSQ7i7+JLCahCbE0kLspoJ+USpS1WqFrs3tEeckx+3c5QLxIhYfuksgK4S2Dpg3LKifiDwhf9rCa0WX4zzAj10akJMnEpgOEa+z28h7eiKfaKTnHNRdbaxpPtJSeFPVEkI8EopKNANORIKt1CdKGjZLCYa/3Gcc0V+oGrNIplNZrw8yr5LEJHOa4eGSiNV0D998/ySTRyx//gGpK5iQ6qjOrO82Fi4HIiedr7HRTlLf65i7Iex2ilgIlTqVtDc3ABBzFUIQYGAvoPrO9s4uW2RVlpbAi/SLmGVU+gkQ2iombHmAwJ/3J0CD4QXzgdtC2mckYBRFLiq4fHVW0NYXmdCqHoi8I18OOsWvLLIjuYUpYVnqeU49dM0c0V9KsYtVTH6Aopl3vMlk+xBYBC6L6vcaZR04o4kLZ7+A4yGsKqB55H22mpH8fTGUKRd5DIZrAdxCH8u0xJJ7ABZ13IGaGJ9vZN27pWSER24="
    );
    housingDataManager=new HousingDataManager(this);
    sceneManager=new Scene(this, "scenes.yml");
    sceneManager.load();
    gameInstance=new Game(this);
    timerInstance=new Timer(this);
    housingManager=new HousingManager(this);
    votingDisplayManager= new VotingDisplayManager();
    votingDisplayManager.hide();
    inventoryManager=new ControlledInventoryManager(this, 0);
 

Bukkit.getScheduler().runTaskLater(this, ()->{
  housingDataManager.load();           
  housingManager.load(housingDataManager.houses);
  sceneManager.load();
}, 5);
    
                    
    world=getServer().getWorld("world");
    Bukkit.getPluginManager().registerEvents(this, this);
    // this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
    //         final var commands = event.registrar();
            
    //         // Register "/hello" with your logic class, a description, and an alias
    //         commands.register(
    //             "hello", 
    //             "Greets the player back.", 
    //             java.util.List.of("hi"), 
    //             new TestCommand()
    //         );
    //     });

      this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
        //commands.registrar().register(TestCommandTree.createCommand("paperparty"), "Have a nice party");
        commands.registrar().register(WWCommand.create(this), "Werewolf commands");
    });

    // getServer().getPluginManager().registerEvents(
    //         new JoinListener(this),
    //         this
    //     );
    getServer().getPluginManager().registerEvents(new Listener() {

      @EventHandler
    public void onDoorInteract(PlayerInteractEvent event) {
      if(!gameInstance.isActive()){
        return;
      }
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        Block clickedBlock = event.getClickedBlock();

        if (clickedBlock == null) {
            return;
        }
        if(!gameInstance.getWWPlayerNames().contains(event.getPlayer().getName())){
          return;
        }

        for(House h:housingManager.houses)for(Door d:h.doors)if(d.getBlock().equals(clickedBlock)){
          // Prevent the door/trapdoor/gate from being used.
        event.setUseInteractedBlock(Event.Result.DENY);

        // Optional: also prevent using the held item against the door.
        event.setUseItemInHand(Event.Result.DENY);
        };

        

        // // Avoid displaying the message twice from main-hand/off-hand events.
        // if (event.getHand() == EquipmentSlot.HAND) {
        //     event.getPlayer().sendMessage("You cannot interact with this door.");
        // }
    }
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        // if (!event.hasChangedBlock()) {
        //     return;
        // }
        
        if(gameInstance.getWWPlayerNames().contains(event.getPlayer().getName())){
         
          if(gameInstance.playersFrozen())event.setCancelled(true);
            

        }
        if(housingManager.follow!=null){
          
        housingManager.close=housingManager.follow.getLocation().getBlock().getLocation();

        Matrix4f matrix = new Matrix4f()
            
            .scale((float)(1+Math.abs(housingManager.close.getX()-housingManager.open.getX())), (float)(1+Math.abs(housingManager.close.getY()-housingManager.open.getY())), (float)(1+Math.abs(housingManager.close.getZ()-housingManager.open.getZ())))
         ;   //.translate((float)((housingManager.close.getX()-housingManager.open.getX())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2),(float)((housingManager.close.getY()-housingManager.open.getY())/2) );

        housingManager.interiorSetDisplay.setTransformationMatrix(matrix);
        housingManager.interiorSetDisplay.teleport(new Location(housingManager.open.getWorld(),Math.min(housingManager.open.getX(),housingManager.close.getX()),Math.min(housingManager.open.getY(),housingManager.close.getY()),Math.min(housingManager.open.getZ(),housingManager.close.getZ())));
        }
        housingManager.updateHover(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        // player.sendMessage(Component.text("Welcome to Werewolf!"));

        // plugin.getServer().broadcast(
        //     Component.text(player.getName() + " joined the game.")
        // );
        }

}, this);
  }
  public void reset(){
    
    timerInstance.stop();
    timerInstance.kill=true;
    timerInstance=new Timer(this);
    //Bukkit.getScheduler().cancelTasks(this);
    setBiomeVolume(sceneManager.getLocation("town_corner1"),sceneManager.getLocation("town_corner2"),Biome.TAIGA);
    setWorldTime(6000L);
    
    gameInstance=new Game(this);
    fakeTabManager.removeAll();
    housingDataManager.load();
    votingDisplayManager.hide();
                    
                    housingManager.load(housingDataManager.houses);
                    Bukkit.getServer().broadcast(Component.text("Loaded Houses", NamedTextColor.GREEN));
    for(Player p: Bukkit.getServer().getOnlinePlayers()){
      p.getWorld().setGameRule(GameRules.PVP, false);
      p.getInventory().clear();
       for (Player viewer : Bukkit.getOnlinePlayers()) {
                        viewer.showPlayer(this, p);
        }

                p.setInvulnerable(false);
                p.setCanPickupItems(true);
                p.setCollidable(true);
                p.setSilent(false);
      p.setInvisible(false);
      p.setAllowFlight(false);
      
    p.updateCommands();
    }
  }
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    event.getPlayer().sendMessage(Component.text("Hello, " + event.getPlayer().getName() + "!"));
    Bukkit.getScheduler().runTaskLater(this, housingManager::refreshVisibility, 5);
    Bukkit.getScheduler().runTask(this, ExamplePlugin::shuffleTabList);
    event.getPlayer().updateCommands();
    if(!gameInstance.isActive()){
      event.getPlayer().getWorld().setGameRule(GameRules.PVP, false);
      event.getPlayer().getInventory().clear();
      event.getPlayer().setGameMode(GameMode.ADVENTURE);
      nameTagManager.showNameTag(event.getPlayer());
    }
    for(Player p: Bukkit.getOnlinePlayers()){
      for (Player viewer : Bukkit.getOnlinePlayers()) {
        WWPlayer wwp = gameInstance.getWWPlayer(p.getName());
        if(wwp!=null&&!wwp.isAlive()&&wwp.killed()){
          viewer.hidePlayer(this, p);
        }
          viewer.unlistPlayer(p);
      }
    }
    Bukkit.getScheduler().runTaskLater(
            this,
            () -> {
                if (event.getPlayer().isOnline()) {
                    fakeTabManager.showAllTo(
                            event.getPlayer()
                    );
                }
                fakeTabManager.refreshAll();
            },
            5L
    );
    if(gameInstance.isActive()){
      
    WWPlayer p = gameInstance.getWWPlayer(event.getPlayer().getName());
      if(p!=null){
              if(p.getHouse()!=null){
              event.getPlayer().setRespawnLocation(p.getHouse().location,true);
            }
                                    
        if(!p.isAlive()){
          
          if(!p.killed())event.getPlayer().kill();
          gameInstance.killWWPlayer(p);
          p.kill();
        }else{
          
                    inventoryManager.toggleJudgment(event.getPlayer());
                    if(!gameInstance.canJudge())gameInstance.setWWPlayerJudgment(p, 0);
          if(gameInstance.getPhase()==GamePhase.DISCUSSION||gameInstance.getPhase()==GamePhase.VOTING||gameInstance.getPhase()==GamePhase.DUSK||gameInstance.getPhase()==GamePhase.DAWN){
            if(p.getHouse()!=null)event.getPlayer().teleport(p.getHouse().tpout);
            nameTagManager.showNameTag(event.getPlayer());
          }else if(gameInstance.getPhase()==GamePhase.DEFENSE||gameInstance.getPhase()==GamePhase.JUDGMENT){
            
        Bukkit.getScheduler().runTask(this, () -> hideHand(event.getPlayer()));
            nameTagManager.hideNameTag(event.getPlayer());
            if(gameInstance.getSentenced()!=null&&p.getName().equals(gameInstance.getSentenced().getName())){
                    if(sceneManager.getLocation("trial_stage")!=null)
                            event.getPlayer().teleport(sceneManager.getLocation("trial_stage"));
            }else{
                  if(sceneManager.getLocation("trial_audience")!=null){
                    Location l=sceneManager.getLocation("trial_audience").add(5,0,-1);
                    if(sceneManager.getLocation("trial_stage")!=null)
                                l.setDirection(sceneManager.getLocation("trial_stage").toVector().subtract(l.toVector()));
                            event.getPlayer().teleport(l);
                  }
                      event.getPlayer().setInvisible(true);
                      
                      event.getPlayer().setCollidable(false);
                      nameTagManager.hideNameTag(event.getPlayer());
                            for(Player viewer: Bukkit.getServer().getOnlinePlayers()){
                                viewer.hidePlayer(this, event.getPlayer());
                            }
                            if(gameInstance.getWWPlayerJudgment(p)<0){
                              inventoryManager.toggleJudgmentEnchant(event.getPlayer(), 7);
                            }else if(gameInstance.getWWPlayerJudgment(p)>0){
                              inventoryManager.toggleJudgmentEnchant(event.getPlayer(), 8);
                            }
            }

          }else if(gameInstance.getPhase()==GamePhase.SENTENCE){
            nameTagManager.hideNameTag(event.getPlayer());
              if(sceneManager.getLocation("execution_audience")!=null)
                  event.getPlayer().teleport(sceneManager.getLocation("execution_audience"));
                  event.getPlayer().setInvisible(true);
                  event.getPlayer().setCollidable(false);
                  
                            nameTagManager.hideNameTag(event.getPlayer());
                            for(Player viewer: Bukkit.getServer().getOnlinePlayers()){
                                viewer.hidePlayer(this, event.getPlayer());
                            }
          }else if(gameInstance.getPhase()==GamePhase.NIGHT||gameInstance.getPhase()==GamePhase.POST_NIGHT){
            nameTagManager.hideNameTag(event.getPlayer());
            if(p.getRole().werewolf&&sceneManager.getLocation("wolf_den")!=null)event.getPlayer().teleport(sceneManager.getLocation("wolf_den"));
            else if(p.getHouse()!=null)event.getPlayer().teleport(p.getHouse().location);
          }
        }
      }else{
        event.getPlayer().setRespawnLocation(sceneManager.getLocation("default_spawnpoint"),true);
        gameInstance.addWWPlayer(event.getPlayer().getName());
        gameInstance.getWWPlayer(event.getPlayer().getName()).setRole(new Spectator());
                    inventoryManager.prepareInventory(event.getPlayer());
                event.getPlayer().kill();
      }
      

    }
    
  }
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event) {
    Bukkit.getServer().broadcast(Component.text("We didn't like " + event.getPlayer().getName() + " anyways"));

    Bukkit.getScheduler().runTask(this, ExamplePlugin::shuffleTabList);
    
  }
  @EventHandler(
        priority = EventPriority.HIGHEST,
        ignoreCancelled = true
)
public void onPlayerDamage(EntityDamageByEntityEvent event) {
  
      if(!gameInstance.isActive())return;
    if (!(event.getEntity() instanceof Player victim)) {
        return;
    }

    Player attacker = getAttackingPlayer(event.getDamager());

    if (attacker == null) {
        return; // Damage was not caused by another player.
    }
    WWPlayer wwp=gameInstance.getWWPlayer(attacker.getName());
    WWPlayer wwpVictim=gameInstance.getWWPlayer(victim.getName());
    if(wwp==null||wwpVictim==null){
      return;
    }
    if(wwp.isAlive()&&gameInstance.canWolvesPVP()&&gameInstance.wolves.contains(wwp)&&!gameInstance.wolves.contains(wwpVictim)){
      return;
    }
      // Bukkit.getServer().broadcast(Component.text(gameInstance.canWolvesPVP()+"&&"+gameInstance.wolves.contains(wwp)+"&&"+!gameInstance.wolves.contains(wwpVictim)));
        event.setCancelled(true);
}
  
  @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    
      event.setShowDeathMessages(false);
      if(!gameInstance.isActive())return;
    WWPlayer wwp = gameInstance.getWWPlayer(event.getPlayer().getName());
    if(wwp!=null){
      if(!wwp.isAlive()&&!wwp.killed()){
      gameInstance.announceDead.add(wwp);
        gameInstance.killWWPlayer(wwp);
        wwp.kill();
      }
      else if(gameInstance.knockedOut.contains(wwp)){
        event.setDeathSound(Sound.ENTITY_WANDERING_TRADER_DRINK_POTION);
        event.getPlayer().sendMessage(Component.text("You were saved!", NamedTextColor.GREEN));
        gameInstance.knockedOut.remove(wwp);
      }
      else{
      event.setCancelled(true);

    }
    }
  }
   @EventHandler
    public void onPostRespawn(PlayerPostRespawnEvent event) {
        Player player = event.getPlayer();
if(!gameInstance.isActive())return;
    WWPlayer wwp = gameInstance.getWWPlayer(event.getPlayer().getName());
    if(wwp!=null){
      if(!wwp.isAlive()){
        for (Player viewer : Bukkit.getOnlinePlayers()) {
                    if (!viewer.equals(player)) {
                        viewer.hidePlayer(this, player);
                    }
                }
        wwp.kill();
      }
    }
        // Code that should run when the player respawns.
    }
  @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
      
      if(!gameInstance.isActive())return;
        Player player = event.getPlayer();
        WWPlayer wwp = gameInstance.getWWPlayer(player.getName());
        if (wwp==null) {
            return;
        }
        if(!wwp.getPrivateChat()){
          return;
        }
        Set<WWPlayer> allowedPlayers =
            wwp.getPrivateChatPlayers();

    event.viewers().removeIf(audience -> {
        // Remove console and other non-player audiences.
        if (!(audience instanceof Player recipient)) {
            return true;
        }

        // Always let the sender see their own message.
        // if (recipient.getUniqueId().equals(player.getUniqueId())) {
        //     return false;
        // }

        WWPlayer recipientWWP =
                gameInstance.getWWPlayer(recipient.getName());

        return recipientWWP == null
                || allowedPlayers==null
                || !allowedPlayers.contains(recipientWWP);
    });

    event.renderer((source, sourceName, message, viewer) ->
            Component.text("")
                    .append(wwp.getPrivateChatPrefix()==null?Component.text(""):wwp.getPrivateChatPrefix())
                    .append(sourceName)
                    .append(Component.text(": ", NamedTextColor.GRAY))
                    .append(message)
    );
    }
    @EventHandler
    public void onPaintingBreak(HangingBreakByEntityEvent event) {
        if (event.getRemover() instanceof Player) {
            event.setCancelled(true);
        }
    }
    @EventHandler
    public void onBedEnter(PlayerBedEnterEvent event) {
        event.setCancelled(true);

        // Optional
        // event.getPlayer().sendMessage("You can't sleep here.");
    }
    @EventHandler
    public void onSpawnChange(PlayerSpawnChangeEvent event) {
        if (event.getCause() == PlayerSpawnChangeEvent.Cause.BED) {
            event.setCancelled(true);
        }
    }
     @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        String cmd = event.getMessage().split(" ")[0].toLowerCase();
        String[] args = event.getMessage().split(" ");

        if (args.length < 2) return;

        if (cmd.equals("/w")
                || cmd.equals("/msg")
                ) {
                  if(gameInstance.getPhase()==GamePhase.NIGHT||gameInstance.getPhase()==GamePhase.POST_NIGHT){
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Component.text("You cannot whisper at night.",NamedTextColor.RED));
                    return;
                  }
                  if(gameInstance.getWWPlayer(event.getPlayer().getName())!=null&&!gameInstance.getWWPlayer(event.getPlayer().getName()).isAlive()){
                    event.setCancelled(true);
                    event.getPlayer().sendMessage(Component.text("You're dead.",NamedTextColor.RED));
                    return;
                  }
                  Player receiver = Bukkit.getPlayerExact(args[1]);

                  if (receiver == null) {
                      return; // Invalid or offline player
                  }
            Bukkit.getServer().broadcast(
                Component.text(
                    event.getPlayer().getName() +
                    " is whispering to " +
                    args[1], NamedTextColor.GRAY
                )
            );
          }
        if (cmd.equals("/tell")
                || cmd.equals("/teammsg")
                || cmd.equals("/tm")) {

            event.setCancelled(true);
            event.getPlayer().sendMessage(Component.text("stop.",NamedTextColor.RED));
        }
    }
    @EventHandler
    public void onCommandSend(PlayerCommandSendEvent event) {
        event.getCommands().remove("tm");
        event.getCommands().remove("tell");
        event.getCommands().remove("teammsg");
    }
    @EventHandler
    public void onPlayerItemHeld(PlayerItemHeldEvent event) {
      if(!gameInstance.isActive())return;
      if(gameInstance.getSentenced()==null)return;
      // Bukkit.getServer().broadcast(Component.text("item switch"));

      Player target = event.getPlayer();

        // Run after Minecraft sends the real held-item update.
        Bukkit.getScheduler().runTask(this, () -> {
          hideHand(target);
          itemHider.hideHeldItemFromEveryone(target);
        });
        // itemHider.hideHeldItemFromEveryone(event.getPlayer());
    }
    @EventHandler
    public void onEquipmentChange(EntityEquipmentChangedEvent event) {
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }
        if(!gameInstance.isActive())return;
      if(gameInstance.getSentenced()==null)return;

        Bukkit.getScheduler().runTask(this, () -> {
          hideHand(target);
          itemHider.hideHeldItemFromEveryone(target);
        });
      }
    private void hideHand(Player target) {
        ItemStack air = new ItemStack(Material.AIR);

        for (Player viewer : target.getWorld().getPlayers()) {
            if (viewer.equals(target)) {
                continue;
            }

            // viewer sees target holding air
            viewer.sendEquipmentChange(
                    target,
                    EquipmentSlot.HAND,
                    air
            );

            viewer.sendEquipmentChange(
                    target,
                    EquipmentSlot.OFF_HAND,
                    air
            );
        }
    }
  @EventHandler
    public void onLeftClick(PlayerInteractEvent event) {
      WWPlayer wwp = gameInstance.getWWPlayer(event.getPlayer().getName());
      if(gameInstance.isActive()&&wwp!=null&&!wwp.isAlive()){
              event.setUseInteractedBlock(Event.Result.DENY);
              event.setUseItemInHand(Event.Result.DENY);
              event.setCancelled(true);
      }
    }
    @EventHandler
    public void onRightClick(PlayerInteractEvent event) {
      WWPlayer wwp = gameInstance.getWWPlayer(event.getPlayer().getName());
      if(gameInstance.isActive()){
        if(wwp!=null){
          if(!wwp.isAlive()){
              event.setUseInteractedBlock(Event.Result.DENY);
              event.setUseItemInHand(Event.Result.DENY);
              event.setCancelled(true);
            }else if(gameInstance.canJudge()){
              if(event.getPlayer().getInventory().getHeldItemSlot()==8){
                if(gameInstance.getWWPlayerJudgment(wwp)==1){
                  gameInstance.setWWPlayerJudgment(wwp, 0);
                inventoryManager.toggleJudgmentEnchant(event.getPlayer(),6);
                }else{

                  
                  gameInstance.setWWPlayerJudgment(wwp, 1);
                inventoryManager.toggleJudgmentEnchant(event.getPlayer(),8);
                }
                
              }else if(event.getPlayer().getInventory().getHeldItemSlot()==7){
                if(gameInstance.getWWPlayerJudgment(wwp)==-1){
                  gameInstance.setWWPlayerJudgment(wwp, 0);
                inventoryManager.toggleJudgmentEnchant(event.getPlayer(),6);
                }else{

                  gameInstance.setWWPlayerJudgment(wwp, -1);
                inventoryManager.toggleJudgmentEnchant(event.getPlayer(),7);
                  
                }
                
              }
            }
          }
      }
    }
  public void setWorldTime(long time) {
    if (world != null) {
        world.setTime(time);
    }
  }
  public static void shuffleTabList() {
    List<Player> players = new ArrayList<>(Bukkit.getOnlinePlayers());

    Collections.shuffle(players);

    for (int i = 0; i < players.size(); i++) {
        players.get(i).setPlayerListOrder(i + 1);
    }
  }
  public void setBiomeVolume(
        Location corner1,
        Location corner2,
        Biome biome
) {
    // Bukkit.getServer().broadcast(Component.text("setbiome triggered"));
    Objects.requireNonNull(corner1, "corner1");
    Objects.requireNonNull(corner2, "corner2");
    Objects.requireNonNull(biome, "biome");

    World world = corner1.getWorld();

    if (world == null || corner2.getWorld() == null) {
        throw new IllegalArgumentException("Locations must have worlds");
    }

    if (!world.equals(corner2.getWorld())) {
        throw new IllegalArgumentException(
                "Both locations must be in the same world"
        );
    }

    int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
    int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());

    int minY = Math.max(
            world.getMinHeight(),
            Math.min(corner1.getBlockY(), corner2.getBlockY())
    );

    int maxY = Math.min(
            world.getMaxHeight() - 1,
            Math.max(corner1.getBlockY(), corner2.getBlockY())
    );

    int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
    int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

    if (minY > maxY) {
        return;
    }

    // Align the coordinates to the containing 4x4x4 biome cells.
    int startX = Math.floorDiv(minX, 4) * 4;
    int startY = Math.floorDiv(minY, 4) * 4;
    int startZ = Math.floorDiv(minZ, 4) * 4;

    int endX = Math.floorDiv(maxX, 4) * 4;
    int endY = Math.floorDiv(maxY, 4) * 4;
    int endZ = Math.floorDiv(maxZ, 4) * 4;

     for (int x = startX; x <= endX; x += 4) {
        for (int y = startY; y <= endY; y += 4) {
            for (int z = startZ; z <= endZ; z += 4) {
                world.setBiome(x, y, z, biome);
            }
        }
    }

    int minChunkX = startX >> 4;
    int maxChunkX = endX >> 4;
    int minChunkZ = startZ >> 4;
    int maxChunkZ = endZ >> 4;
    Bukkit.getScheduler().runTask(this, () -> {
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                if (world.isChunkLoaded(chunkX, chunkZ)) {
                    world.refreshChunk(chunkX, chunkZ);
                }
            }
        }

        // Bukkit.getServer().broadcast(
        //         Component.text("Biome changed and chunks refreshed")
        // );
    });
}
public static int removeEntitiesWithTag(String tag) {
    int removed = 0;

    for (World world : Bukkit.getWorlds()) {
        for (Entity entity : world.getEntities()) {
            if (entity.getScoreboardTags().contains(tag)) {
                entity.remove();
                removed++;
            }
        }
    }

    return removed;
}

private Player getAttackingPlayer(Entity damager) {
    if (damager instanceof Player player) {
        return player;
    }

    if (damager instanceof Projectile projectile
            && projectile.getShooter() instanceof Player player) {
        return player;
    }

    return null;
}
}
