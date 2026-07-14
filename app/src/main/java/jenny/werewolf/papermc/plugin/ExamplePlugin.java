package jenny.werewolf.papermc.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.plugin.java.JavaPlugin;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;

import java.util.ArrayList;

import jenny.werewolf.papermc.plugin.commands.WWCommand;

public class ExamplePlugin extends JavaPlugin implements Listener {
  public Game gameInstance = new Game(this);
  public Timer timerInstance = new Timer(this);
  public HousingManager housingManager = new HousingManager(this);
  World world = getServer().getWorld("world");
  //public ArrayList<RotatingGhostBeam> beams = new ArrayList<RotatingGhostBeam>();


  @Override
  public void onEnable() {
    gameInstance=new Game(this);
    timerInstance=new Timer(this);
    housingManager=new HousingManager(this);
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
    public void onMove(PlayerMoveEvent event) {
        // if (!event.hasChangedBlock()) {
        //     return;
        // }
        housingManager.updateHover(event.getPlayer());
    }

}, this);
  }

  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    event.getPlayer().sendMessage(Component.text("Hello, " + event.getPlayer().getName() + "!"));
  }
  public void setWorldTime(long time) {
    if (world != null) {
        world.setTime(time);
    }
  }
}