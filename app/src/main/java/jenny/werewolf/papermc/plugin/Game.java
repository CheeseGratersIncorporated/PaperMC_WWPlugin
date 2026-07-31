package jenny.werewolf.papermc.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.GameRules;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import net.kyori.adventure.title.Title;

import java.time.Duration;

public class Game {
    private ArrayList<WWPlayer> players;
    public Set<WWPlayer> dead=new HashSet<>();
    public Set<WWPlayer> wolves=new HashSet<>();
    public Set<WWPlayer> knockedOut=new HashSet<>();
    Map<String, WWPlayer> playerMap = new HashMap<>();
    Map<String, Integer> roles = new HashMap<>();
    private Handler handler;
    private int WWPlayerCount, aliveCount;
    private boolean isGameActive = false;
    private ExamplePlugin plugin;


    public Game(ExamplePlugin plugin) {
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.roles = new HashMap<>();
        this.handler = new Handler(this,plugin);
    }
    public void startGame() {
        isGameActive = true;
        WWPlayerCount = players.size();
        aliveCount = WWPlayerCount;
        handler.resetGuillotine();

        //create role instances
        ArrayList<Role> roleInstancesList = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : roles.entrySet()) {
            String key = entry.getKey();
            Integer value = entry.getValue();
            for(int i = 0; i < value; i++) {
                roleInstancesList.add(Role.create(key));
            }
            
        }
        //randomize role instances
        Collections.shuffle(roleInstancesList);
        int i = 0;


        for (WWPlayer WWPlayer : players) {
            WWPlayer.setAlive(true);


            //assign roles to players, if there are more players than roles, assign the remaining players as townies
            if(i >= roleInstancesList.size()) {
                WWPlayer.setRole(Role.create("townie"));
            } else{
                WWPlayer.setRole(roleInstancesList.get(i));
                i++;
            }
            WWPlayer.getRole().player=WWPlayer;

            if(WWPlayer.getRole().werewolf)wolves.add(WWPlayer);
        }


        
         plugin.setBiomeVolume(plugin.sceneManager.getLocation("town_corner1"),plugin.sceneManager.getLocation("town_corner2"),Biome.TAIGA);

                plugin.setWorldTime(6000L);
        plugin.timerInstance.start(Times.FIRSTDAY, BossBar.bossBar(
            Component.text("DAY 1", NamedTextColor.YELLOW),
            1.0f,
            BossBar.Color.YELLOW,
            BossBar.Overlay.PROGRESS
        ), this, 
        //on complete
        () -> {
            
            handler.initPhase(GamePhase.DUSK);
        }, 
        //on tick
        (timeLeft) -> {

        });
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            
            WWPlayer wwp=playerMap.get(p.getName());
           

            if(wwp!=null){
                    plugin.inventoryManager.prepareInventory(p);

            
                    Title.Times times = Title.Times.times(
                            Duration.ofMillis(500),  // Fade in
                            Duration.ofSeconds(3),  // Stay
                            Duration.ofMillis(500)  // Fade out
                    );

                    p.showTitle(Title.title(wwp.getRole().title, wwp.getRole().subtitle, times));
                    if(
                    (wwp.getHouse()!=null)){
                       wwp.getHouse().openDoors();

                    }
                }
                
            }

            
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            
            WWPlayer wwp=playerMap.get(p.getName());
            if(wwp==null){
                
                addWWPlayer(p.getName());
                getWWPlayer(p.getName()).setRole(new Townie());
                
                    plugin.inventoryManager.prepareInventory(p);
                p.kill();

            }
        }
    }


    public void killWWPlayer(WWPlayer wwp) {
        if(!wwp.isAlive()) return;
        wwp.setAlive(false);
        dead.add(wwp);
        wwp.setPrivateChat(true);
        wwp.setPrivateChatPlayers(dead);
        wwp.setPrivateChatPrefix(Component.text("[DEAD]",NamedTextColor.GRAY));
        aliveCount--;
    }
    public boolean canVote(){
        return handler.currentPhase == GamePhase.VOTING;
    }
    public boolean canJudge(){
        return handler.currentPhase == GamePhase.JUDGMENT;
    }
    public boolean playersFrozen(){
        return handler.currentPhase == GamePhase.SENTENCE||handler.currentPhase == GamePhase.DEFENSE||handler.currentPhase == GamePhase.JUDGMENT;
    }
    public void setWWPlayerJudgment(WWPlayer wwp, int j){
        wwp.setJudgment(j);
    }
    public Component setWWPlayerTarget(WWPlayer wwp, WWPlayer target) {
        // if(wwp.getTarget() == target) return Component.text("You are already targetting "+target.getName(), NamedTextColor.RED);
        if(wwp.getRole().checkTarget(target))wwp.setTarget(target);
        return wwp.getRole().getNewTargetResponse(target);
    }
    public boolean setWWPlayerVote(WWPlayer wwp, WWPlayer target) {
        if(wwp.getVotedFor() == target) return false;
        if(wwp.getVotedFor() != null)wwp.getVotedFor().removeVote();
            wwp.setVotedFor(target);
            target.addVote();
            plugin.votingDisplayManager.setPlayer(target.getName(), target.getVote());
            if(target.getVote()>Math.floor(((double)aliveCount)/2)){
                plugin.timerInstance.stop();
                handler.sentenced=target;
                handler.initPhase(GamePhase.DEFENSE);
            }
        return true;
    }
    public void addWWPlayer(WWPlayer wwp) {
        if(isGameActive)killWWPlayer(wwp);
        players.add(wwp);
        playerMap.put(wwp.getName(), wwp);
    }
    public void addWWPlayer(String p) {
        WWPlayer wwp = new WWPlayer(p);
        if(isGameActive)killWWPlayer(wwp);
        players.add(wwp);
        playerMap.put(p, wwp);
    }
    public void removeWWPlayer(WWPlayer wwp) {
        if(!players.contains(wwp)) return;
        if(isGameActive)killWWPlayer(wwp);
        WWPlayerCount--;
        
        wwp.getHouse().owner=null;
        players.remove(wwp);
        playerMap.remove(wwp.getName());

    }
    public void removeWWPlayer(String wwp) {
        if(!playerMap.containsKey(wwp)) return;
        if(isGameActive)killWWPlayer(playerMap.get(wwp));
        
        WWPlayerCount--;
        WWPlayer p = playerMap.get(wwp);
        
        p.getHouse().owner=null;
        players.remove(p);
        playerMap.remove(wwp);
    }
    public void setRole(String role, int count) {
        roles.put(role, count);
        if (roles.containsKey(role)) if(roles.get(role) <= 0) roles.remove(role);
    }
    public Map<String, Integer> getRoles() {
        return roles;
    }
    public ArrayList<WWPlayer> getWWPlayerList() {
        return players;
    }
    public Set<String> getWWPlayerNames() {
        return playerMap.keySet();
    }
    public WWPlayer getWWPlayer(String name) {
        return playerMap.get(name);
    }
    public Role getWWPlayerRole(String name) {
        WWPlayer WWPlayer = playerMap.get(name);
        return WWPlayer != null ? WWPlayer.getRole() : null;
    }
    public boolean isActive(){
        return isGameActive;
    }
    public void flushEvents() {
        handler.flushEvents();
    }
    public boolean canWolvesPVP(){
        return handler.currentPhase==GamePhase.POST_NIGHT;
    }
    
}
class Times{
    
    public static final int FIRSTDAY = 15;
    public static final int DISCUSSION = 15;
    public static final int VOTING = 30;
    public static final int DEFENSE = 15;
    public static final int JUDGMENT = 30;
    public static final int SENTENCE = 10;
    public static final int DUSK = 15;
    public static final int NIGHT = 30;
    public static final int POST_NIGHT = 30;
    public static final int DAWN = 15;
}
enum GamePhase {
    DISCUSSION,
    VOTING,
    DEFENSE,
    JUDGMENT,
    SENTENCE, 
    DUSK,   //doors unlock
    NIGHT, 
    POST_NIGHT, //werewolves punch ppl
    DAWN, //doors unlock
}