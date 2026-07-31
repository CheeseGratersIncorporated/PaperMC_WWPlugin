package jenny.werewolf.papermc.plugin;

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
    public boolean werewolf=false;
    public boolean townie=true;
    public int def=0,atk=0;
    public boolean doctored=false, attacked=false;
    public WWPlayer player;
    public Component title=Component.text("filler text haha"),subtitle=Component.text("im bad");
    public abstract String getName();
    public abstract void dayAbility(Handler handler); //trigger on end of day
    public abstract void nightAbility(Handler handler); //trigger on end of night
    public abstract void immediateAbility(); //trigger immediately, check params within
    public boolean checkTarget(WWPlayer t){return true;}
    public Component getNewTargetResponse(WWPlayer t){
        return Component.text("You are now targeting "+t.getName(), NamedTextColor.GREEN);
    }
}

class Townie extends Role {
    public Townie(){
        this.title=Component.text("TOWNIE",NamedTextColor.GREEN);
        this.subtitle=Component.text("Vote out the werewolves and survive.",NamedTextColor.GRAY);
    }
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

    public boolean checkTarget(WWPlayer t){return false;}
    public Component getNewTargetResponse(WWPlayer t){
        return Component.text("You have no ability", NamedTextColor.RED);
    }
}
class NullRole extends Role{
    public String getName() {
        return "null";
    }

    @Override
    public void dayAbility(Handler handler) {
    }

    @Override
    public void nightAbility(Handler handler) {
    }

    @Override
    public void immediateAbility() {
    }
}
class Werewolf extends Role {
    public Werewolf(){
        this.werewolf=true;
        this.atk=1;
        this.title=Component.text("WEREWOLF",NamedTextColor.RED);
        this.subtitle=Component.text("Murder >:3",NamedTextColor.GRAY);
    
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
        if(!player.isAlive()||player.getTarget()==null)return;
        handler.pushEvent(WWEvent.of(100,
            ()->{
                if(player.getTarget()!=null){
                    
                player.getTarget().addVote();
                handler.maxWolfVote=Math.max(player.getTarget().getVote(),handler.maxWolfVote);
                }
            }));
        
    }

    @Override
    public void immediateAbility() {
        // Werewolf's immediate ability logic here
    }
    public boolean checkTarget(WWPlayer t){
        if(!player.isAlive())return false;
        if(t==null)return true;
        if(t.getRole().werewolf)return false;
        if(!t.isAlive())return false;
        return true;
    }
    public Component getNewTargetResponse(WWPlayer t){
        if(!player.isAlive())return Component.text("你死了", NamedTextColor.RED);
        if(t==null)return Component.text("You've decided not to target anyone", NamedTextColor.GREEN);
        if(t.getRole().werewolf)return Component.text("You can't target a fellow werewolf", NamedTextColor.RED);
        if(!t.isAlive())return Component.text(t.getName() + " is already dead.", NamedTextColor.RED); 
        return Component.text("You've decided to target "+ t.getName(), NamedTextColor.GREEN);
    }
}

class Sheriff extends Role {
    public Sheriff(){
        
        this.title=Component.text("SHERIFF",NamedTextColor.GREEN);
        this.subtitle=Component.text("Investigate others for signs of tomfoolery",NamedTextColor.GRAY);
    }
    public String getName() {
        return "sheriff";
    }
    @Override
    public void dayAbility(Handler handler) {
        // Sheriff's ability logic here
    }

    @Override
    public void nightAbility(Handler handler) {
        if(!player.isAlive()||player.getTarget()==null)return;
        handler.pushEvent(WWEvent.of(70,
            ()->{
                if(player.getTarget()!=null){
            for(Player p : Bukkit.getServer().getOnlinePlayers()){
            if(p.getName().equals(player.getName())){
                if(player.getTarget().getRole().werewolf)
                    p.sendMessage(Component.text("Your target is a sussy baka", NamedTextColor.AQUA));
                else
                    p.sendMessage(Component.text("Your target is not a sussy baka", NamedTextColor.AQUA));

            }
        }}
        }));
        
    }

    @Override
    public void immediateAbility() {
        // Sheriff's immediate ability logic here
    }
    public boolean checkTarget(WWPlayer t){
        if(!player.isAlive())return false;
        if(t==null)return true;
        if(!t.isAlive())return false;
        if(t.getName().equals(player.getName()))return false;
        return true;
    }
    public Component getNewTargetResponse(WWPlayer t){
        if(!player.isAlive())return Component.text("你死了", NamedTextColor.RED);
        if(t==null)return Component.text("You decided not to investigate anyone", NamedTextColor.GREEN);
        if(t.getName().equals(player.getName()))return Component.text("You realize that you are in fact NOT a sussy baka and stop wasting your time.", NamedTextColor.RED);
        if(!t.isAlive())return Component.text("You feel that investigating "+t.getName()+" will come up as cold as their corpse.", NamedTextColor.RED); 
        return Component.text("You decided to investigate "+ t.getName(), NamedTextColor.GREEN);
    }
}

class Doctor extends Role {
    private int selfHeals=1;
    public Doctor(){
        this.selfHeals=1;
        this.title=Component.text("DOCTOR",NamedTextColor.GREEN);
        this.subtitle=Component.text("Save someone from the wolves every night",NamedTextColor.GRAY);
    
    }
    public String getName() {
        return "doctor";
    }

    @Override
    public void dayAbility(Handler handler) {
        // Doctor's ability logic here
    }

    @Override
    public void nightAbility(Handler handler) {
        if(!player.isAlive()||player.getTarget()==null)return;
        if(player.getTarget().getName().equals(player.getName())){
            if(selfHeals<=0)return;
            selfHeals--;
        }
        handler.pushEvent(WWEvent.of(100,
            ()->{
                if(player.getTarget()!=null){
            player.getTarget().getRole().doctored=true;
        }
        }));
        handler.pushEvent(WWEvent.of(0,
            ()->{
                if(player.getTarget()!=null){
            player.getTarget().getRole().doctored=false;
            if(player.getTarget().getRole().attacked){
                for(Player p : Bukkit.getServer().getOnlinePlayers()){
                    if(p.getName().equals(player.getName())){
                        
                            p.sendMessage(Component.text("Your target was attacked!", NamedTextColor.GREEN));
                    }
                }
            }
        }
        }));
    }

    @Override
    public void immediateAbility() {
        // Doctor's immediate ability logic here
    }
    public boolean checkTarget(WWPlayer t){
        if(!player.isAlive())return false;
        if(t==null)return true;
        if(!t.isAlive())return false;
        if(t.getName().equals(player.getName())&&selfHeals<=0)return false;
        return true;
    }
    public Component getNewTargetResponse(WWPlayer t){
        if(!player.isAlive())return Component.text("你死了", NamedTextColor.RED);
        if(t==null)return Component.text("You decided not to heal anyone", NamedTextColor.GREEN);
        if(t.getName().equals(player.getName())){
            if(selfHeals>0)return Component.text("You decide to hole up at home and heal yourself tonight", NamedTextColor.GREEN);
            return Component.text("A sudden sense of moral duty surges through your body. Seems you can't keep being selfish and healing yourself.", NamedTextColor.RED);
        }if(!t.isAlive())return Component.text("Trying to treat a corpse may not be the brightest of ideas.", NamedTextColor.RED); 
        return Component.text("You decided to heal "+ t.getName(), NamedTextColor.GREEN);
    }
}