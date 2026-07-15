package jenny.werewolf.papermc.plugin;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.ArrayList;

public class Scene {
    HashMap<String,Location> roles=new HashMap<>();

    public void start(){

    }

    public void stop(){

    }

    
}

class Role{
    ArrayList<Player> actors;
    Location loc;
    boolean invis=false;


}
