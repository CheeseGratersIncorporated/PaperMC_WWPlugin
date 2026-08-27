package io.github.gegagegagega.werewolf;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.block.Biome;
import org.bukkit.entity.Player;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.title.Title;

public class Game {
    private ArrayList<WWPlayer> players;
    public Set<WWPlayer> dead=new HashSet<>();
    public Set<WWPlayer> announceDead=new HashSet<>();
    public Set<WWPlayer> wolves=new HashSet<>();
    public Set<WWPlayer> knockedOut=new HashSet<>();
    Map<String, WWPlayer> playerMap = new HashMap<>();
    Map<String, Integer> roles = new HashMap<>();
    private Handler handler;
    private int WWPlayerCount, aliveCount;
    private boolean isGameActive = false;
    private WerewolfPlugin plugin;
    public ArrayList<Component> gameEndFlags=new ArrayList<>();


    public Game(WerewolfPlugin plugin) {
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.roles = new HashMap<>();
        this.handler = new Handler(this,plugin);
        this.isGameActive= false;
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
        Collections.shuffle(players);
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
                getWWPlayer(p.getName()).setRole(new Spectator());
                p.getInventory().clear();
                    // plugin.inventoryManager.prepareInventory(p);
                p.kill();

            }
        }
        announceDead=new HashSet<>();
    }

    public void endGame(){
        // return;
        //hi
        isGameActive = false;
        int i=0;
        for(Component c : gameEndFlags){
            Bukkit.getServer().broadcast(c);
            Bukkit.getScheduler().runTaskLater(plugin,() -> {
                for(Player p : Bukkit.getServer().getOnlinePlayers()){
                                    Component title = c;
                    Component subtitle = Component.text(
                            "",
                            NamedTextColor.GRAY
                        );
                    
                    

                    Title.Times times = Title.Times.times(
                            Duration.ofMillis(500),  // Fade in
                            Duration.ofSeconds(3),  // Stay
                            Duration.ofMillis(500)  // Fade out
                    );

                    p.showTitle(Title.title(title, subtitle, times));
                }
            },4*20*i);
            i++;
        }
        for(WWPlayer wwp : players){
            Bukkit.getServer().broadcast(Component.text(wwp.getName(),NamedTextColor.WHITE).append(Component.text(" was a ",NamedTextColor.YELLOW)).append(wwp.getRole().title));
        }
        gameEndFlags=new ArrayList<>();
        if(plugin.sceneManager.getLocation("default_spawnpoint")!=null)
        for(Player p: Bukkit.getServer().getOnlinePlayers()){

            p.teleport(plugin.sceneManager.getLocation("default_spawnpoint"));
        }
        plugin.timerInstance.stop();
        plugin.timerInstance.kill=true;
        Bukkit.getScheduler().runTask(plugin,() -> {
            
        plugin.reset();
        });
        Bukkit.getScheduler().runTaskLater(plugin,() -> {
        plugin.reset();
        },5);
        plugin.reset();
        
    }
    public void killWWPlayer(WWPlayer wwp) {
       
        dead.add(wwp);
        if(!(getPhase()==GamePhase.NIGHT||getPhase()==GamePhase.POST_NIGHT)){
            plugin.fakeTabManager.removeFakePlayerForEveryone(wwp.getName());
            plugin.fakeTabManager.addFakePlayerForEveryone(
                wwp.getName(),
            wwp.getName(),
            Component.text(wwp.getName(),NamedTextColor.GRAY),
            42
            );
        }
        
        wwp.setPrivateChat(true);
        wwp.setPrivateChatPlayers(dead);
        wwp.setPrivateChatPrefix(Component.text("[DEAD]",NamedTextColor.GRAY));
         if(!wwp.isAlive()) return;
         
        wwp.setAlive(false);
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
    public int getWWPlayerJudgment(WWPlayer wwp){
        return wwp.getJudgment();
    }
    public Component setWWPlayerTarget(WWPlayer wwp, WWPlayer target) {
        // if(wwp.getTarget() == target) return Component.text("You are already targetting "+target.getName(), NamedTextColor.RED);
        if(wwp.getRole().checkTarget(target))wwp.setTarget(target);
        return wwp.getRole().getNewTargetResponse(target);
    }
    public boolean setWWPlayerVote(WWPlayer wwp, WWPlayer target) {
        if(wwp.getVotedFor() == target) return false;
        if(wwp.getVotedFor() != null){
            wwp.getVotedFor().removeVote();
            
            plugin.votingDisplayManager.setPlayer(wwp.getVotedFor().getName(), wwp.getVotedFor().getVote());
        }
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
        if(isGameActive){
            wwp.setRole(new Spectator());
            killWWPlayer(wwp);
        }
        players.add(wwp);
        playerMap.put(wwp.getName(), wwp);
        plugin.fakeTabManager.addFakePlayerForEveryone(
            wwp.getName(),
        wwp.getName(),
        Component.text(wwp.getName()),
        42
        );
        
    }
    public void addWWPlayer(String p) {
        WWPlayer wwp = new WWPlayer(p);
        if(isGameActive){
            wwp.setRole(new Spectator());
            killWWPlayer(wwp);
        }
        players.add(wwp);
        playerMap.put(p, wwp);
        plugin.fakeTabManager.addFakePlayerForEveryone(
            wwp.getName(),
        wwp.getName(),
        Component.text(wwp.getName()),
        42
        );
    }
    public void removeWWPlayer(WWPlayer wwp) {
        if(!players.contains(wwp)) return;
        if(isGameActive)killWWPlayer(wwp);
        WWPlayerCount--;
        plugin.fakeTabManager.removeFakePlayerForEveryone(wwp.getName());
        if(wwp.getHouse()!=null)
        wwp.getHouse().owner=null;
        players.remove(wwp);
        playerMap.remove(wwp.getName());

    }
    public void removeWWPlayer(String wwp) {
        if(!playerMap.containsKey(wwp)) return;
        if(isGameActive)killWWPlayer(playerMap.get(wwp));
        
        WWPlayerCount--;
        WWPlayer p = playerMap.get(wwp);
        
        plugin.fakeTabManager.removeFakePlayerForEveryone(p.getName());
        if(p.getHouse()!=null)
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
    public GamePhase getPhase(){
        return handler.currentPhase;
    }
    public WWPlayer getSentenced(){
        return handler.sentenced;
    }
    public boolean canWolvesPVP(){
        return handler.currentPhase==GamePhase.POST_NIGHT;
    }
    
}
class Times{
    
    public static final int FIRSTDAY = 60;
    public static final int DISCUSSION = 120;
    public static final int VOTING = 60;
    public static final int DEFENSE = 20;
    public static final int JUDGMENT = 20;
    public static final int SENTENCE = 10;
    public static final int DUSK = 15;
    public static final int NIGHT = 60;
    public static final int POST_NIGHT = 15;
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