package jenny.werewolf.papermc.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.block.Biome;
import org.bukkit.block.Block;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Matrix4f;

import org.bukkit.Sound;
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

import io.papermc.paper.event.player.AsyncChatEvent;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;

import java.util.ArrayList;
import java.util.Collections;
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
  World world = getServer().getWorld("world");
  public Biome fogBiome;
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
    skinMasker=new SkinMasker();
    housingDataManager=new HousingDataManager(this);
    sceneManager=new Scene(this, "scenes.yml");
    sceneManager.load();
    gameInstance=new Game(this);
    timerInstance=new Timer(this);
    housingManager=new HousingManager(this);
    votingDisplayManager= new VotingDisplayManager();
    votingDisplayManager.hide();
    inventoryManager=new ControlledInventoryManager(this, 0);
 




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
    setBiomeVolume(sceneManager.getLocation("town_corner1"),sceneManager.getLocation("town_corner2"),Biome.TAIGA);

    gameInstance=new Game(this);
    timerInstance.stop();
    for(Player p: Bukkit.getServer().getOnlinePlayers()){
       for (Player viewer : Bukkit.getOnlinePlayers()) {
                        viewer.showPlayer(this, p);
        }

                p.setInvulnerable(false);
                p.setCanPickupItems(true);
                p.setCollidable(true);
                p.setSilent(false);
      p.setInvisible(false);
      p.setAllowFlight(false);
    }
  }
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    event.getPlayer().sendMessage(Component.text("Hello, " + event.getPlayer().getName() + "!"));
    Bukkit.getScheduler().runTask(this, housingManager::refreshVisibility);
    Bukkit.getScheduler().runTask(this, ExamplePlugin::shuffleTabList);
    if(gameInstance.isActive()){
    WWPlayer p = gameInstance.getWWPlayer(event.getPlayer().getName());
    if(p!=null&&!p.isAlive()){
      if(!p.killed())event.getPlayer().kill();
      p.kill();
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
    if(gameInstance.canWolvesPVP()&&gameInstance.wolves.contains(wwp)&&!gameInstance.wolves.contains(wwpVictim)){
      return;
    }
      Bukkit.getServer().broadcast(Component.text(gameInstance.canWolvesPVP()+"&&"+gameInstance.wolves.contains(wwp)+"&&"+!gameInstance.wolves.contains(wwpVictim)));
        event.setCancelled(true);
}
  
  @EventHandler
  public void onDeath(PlayerDeathEvent event) {
    
      if(!gameInstance.isActive())return;
      event.setShowDeathMessages(false);
    WWPlayer wwp = gameInstance.getWWPlayer(event.getPlayer().getName());
    if(wwp!=null){
      if(!wwp.isAlive()&&!wwp.killed()){
        Bukkit.getServer().broadcast(Component.text(wwp.getName()+" has died",NamedTextColor.RED));
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
    public void onLeftClick(PlayerInteractEvent event) {
      return;
//       if(!gameInstance.isActive())return;
//         Action action = event.getAction();

//         if (action != Action.LEFT_CLICK_AIR
//                 && action != Action.LEFT_CLICK_BLOCK) {
//             return;
//         }
//         WWPlayer wwp=gameInstance.getWWPlayer(event.getPlayer().getName());
//         if (wwp==null||!wwp.killed()) {
//             return;
//         }
//         NavigableSet<String> sorted = new TreeSet<>(gameInstance.getWWPlayerNames());
//         String next;
// if(wwp.getSpecPlayer()==null)next=sorted.first();
// else{
//     next = sorted.higher(wwp.getSpecPlayer().getName());
// }

//     next= next != null ? next : sorted.first();
//     wwp.setSpecPlayer(gameInstance.getWWPlayer(next));

//     for(Player p : Bukkit.getServer().getOnlinePlayers()){
//       if(p.getName().equals(next)){
//         event.getPlayer().teleport(p);
//         break;
//       }
//     }

//         event.setUseInteractedBlock(Event.Result.DENY);
//         event.setUseItemInHand(Event.Result.DENY);
//         event.setCancelled(true);
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
    Bukkit.getServer().broadcast(Component.text("setbiome triggered"));
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

        Bukkit.getServer().broadcast(
                Component.text("Biome changed and chunks refreshed")
        );
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