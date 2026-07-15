package jenny.werewolf.papermc.plugin;

import java.beans.EventHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import java.util.function.Supplier;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


import net.kyori.adventure.text.Component;

import net.kyori.adventure.text.format.NamedTextColor;

public abstract class Role {
    private static final Map<String, Supplier<Role>> ROLES = new HashMap<>();
    static {
        register("townie", Townie::new);
        register("doctor", Doctor::new);
        register("werewolf", Werewolf::new);
        register("sheriff", Sheriff::new);
    }

    public static void register(String name, Supplier<Role> factory) {
        ROLES.put(name.toLowerCase(), factory);
    }

    public static Set<String> names() {
        return ROLES.keySet();
    }

    public static Role create(String name) {
        return ROLES.get(name.toLowerCase()).get();
    }
    public boolean werewolf=true;
    public int def=0,atk=0;
    public boolean doctored=false;
    public WWPlayer player;
    public abstract String getName();
    public abstract void dayAbility(Handler handler); //trigger on end of day
    public abstract void nightAbility(Handler handler); //trigger on end of night
    public abstract void immediateAbility(); //trigger immediately, check params within
}

class Townie extends Role {
    public String getName() {
        return "townie";
    }
    @Override
    public void dayAbility(Handler handler) {
        // Townie's ability logic here
    }

    @Override
    public void nightAbility(Handler handler) {
        // Townie's night ability logic here
    }

    @Override
    public void immediateAbility() {
        // Townie's immediate ability logic here
    }
}

class Werewolf extends Role {
    public Werewolf(){
        this.werewolf=true;
        this.atk=1;
    }
    public String getName() {
        return "werewolf";
    }
    @Override
    public void dayAbility(Handler handler) {
        // Werewolf's ability logic here
    }

    @Override
    public void nightAbility(Handler handler) {
        handler.pushEvent(WWEvent.of(100,
            ()->{
                player.getTarget().addVote();
                handler.maxWolfVote=Math.max(player.getTarget().getVote(),handler.maxWolfVote);
            }));
        
    }

    @Override
    public void immediateAbility() {
        // Werewolf's immediate ability logic here
    }
}

class Sheriff extends Role {
    public String getName() {
        return "sheriff";
    }
    @Override
    public void dayAbility(Handler handler) {
        // Sheriff's ability logic here
    }

    @Override
    public void nightAbility(Handler handler) {
        handler.pushEvent(WWEvent.of(70,
            ()->{
            for(Player p : Bukkit.getServer().getOnlinePlayers()){
            if(p.getName()==player.getName()){
                if(player.getTarget().getRole().werewolf)
                    p.sendMessage(Component.text("Your target is a sussy baka", NamedTextColor.AQUA));
                else
                    p.sendMessage(Component.text("Your target is not a sussy baka", NamedTextColor.AQUA));

            }
        }
        }));
        
    }

    @Override
    public void immediateAbility() {
        // Sheriff's immediate ability logic here
    }
}

class Doctor extends Role {

    public String getName() {
        return "doctor";
    }

    @Override
    public void dayAbility(Handler handler) {
        // Doctor's ability logic here
    }

    @Override
    public void nightAbility(Handler handler) {
        handler.pushEvent(WWEvent.of(100,
            ()->{
            player.getTarget().getRole().doctored=true;
        }));
        handler.pushEvent(WWEvent.of(0,
            ()->{
            player.getTarget().getRole().doctored=false;
        }));
    }

    @Override
    public void immediateAbility() {
        // Doctor's immediate ability logic here
    }
}