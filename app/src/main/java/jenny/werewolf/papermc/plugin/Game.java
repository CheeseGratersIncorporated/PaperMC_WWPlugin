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

import com.google.common.util.concurrent.AbstractScheduledService.Scheduler;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class Game {
    private ArrayList<WWPlayer> players;
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
        }
        
        
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
    }


    public void killWWPlayer(WWPlayer WWPlayer) {
        if(!WWPlayer.isAlive()) return;
        WWPlayer.setAlive(false);
        aliveCount--;
    }
    public boolean canVote(){
        return handler.currentPhase == GamePhase.VOTING;
    }
    public boolean setWWPlayerTarget(WWPlayer WWPlayer, WWPlayer target) {
        if(WWPlayer.getTarget() == target) return false;
        WWPlayer.setTarget(target);
        return true;
    }
    public boolean setWWPlayerVote(WWPlayer WWPlayer, WWPlayer target) {
        if(WWPlayer.getVotedFor() == target) return false;
        if(WWPlayer.getVotedFor() != null)WWPlayer.getVotedFor().removeVote();
            WWPlayer.setVotedFor(target);
            target.addVote();
        return true;
    }
    public void addWWPlayer(WWPlayer WWPlayer) {
        if(isGameActive)WWPlayer.setAlive(false);
        players.add(WWPlayer);
        playerMap.put(WWPlayer.getName(), WWPlayer);
    }
    public void addWWPlayer(String WWPlayer) {
        WWPlayer p = new WWPlayer(WWPlayer);
        if(isGameActive)p.setAlive(false);
        players.add(p);
        playerMap.put(WWPlayer, p);
    }
    public void removeWWPlayer(WWPlayer WWPlayer) {
        if(!players.contains(WWPlayer)) return;
        if(isGameActive)killWWPlayer(WWPlayer);
        WWPlayerCount--;
        players.remove(WWPlayer);
        playerMap.remove(WWPlayer.getName());
    }
    public void removeWWPlayer(String WWPlayer) {
        if(!playerMap.containsKey(WWPlayer)) return;
        if(isGameActive)killWWPlayer(playerMap.get(WWPlayer));
        
        WWPlayerCount--;
        WWPlayer p = playerMap.get(WWPlayer);
        players.remove(p);
        playerMap.remove(WWPlayer);
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
    public void flushEvents() {
        handler.flushEvents();
    }
    
}
class Times{
    
    public static final int FIRSTDAY = 20;
    public static final int DISCUSSION = 180;
    public static final int VOTING = 30;
    public static final int DEFENSE = 15;
    public static final int JUDGMENT = 30;
    public static final int SENTENCE = 10;
    public static final int DUSK = 15;
    public static final int NIGHT = 60;
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