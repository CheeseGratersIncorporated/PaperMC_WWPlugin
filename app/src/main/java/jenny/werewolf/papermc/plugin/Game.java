package jenny.werewolf.papermc.plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;

public class Game {
    private ArrayList<WWPlayer> players;
    Map<String, WWPlayer> playerMap = new HashMap<>();
    Map<String, Integer> roles = new HashMap<>();
    private Scheduler scheduler;
    private int WWPlayerCount, aliveCount;
    private boolean isGameActive = false;
    private ExamplePlugin plugin;

    public Game(ExamplePlugin plugin) {
        this.plugin = plugin;
        this.players = new ArrayList<>();
        this.roles = new HashMap<>();
        this.scheduler = new Scheduler(this,plugin);
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
            
            scheduler.initPhase(GamePhase.DUSK);
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
        return scheduler.currentPhase == GamePhase.VOTING;
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
        scheduler.flushEvents();
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
class Scheduler{
    GamePhase currentPhase;
    GamePhase nextPhase;
    Game game;
    ExamplePlugin plugin;
    int dayCount=1;
    public Scheduler(Game game, ExamplePlugin plugin) {
        this.game = game;
        this.plugin = plugin;
    }
    //smaller priority goes first
        PriorityQueue<GameEvent> EventQueue = new PriorityQueue<>(
              Comparator.comparingInt(GameEvent::getPriority)
        );
    public void flushEvents(){
        
    }

    public void initPhase(GamePhase phase) {
        this.currentPhase = phase;
        //REMINDER: kill timers
        switch(phase) {
            case DISCUSSION:
                // Initialize discussion phase
                
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
                        
                        initPhase(GamePhase.VOTING);
                    }, 
                    //on tick
                    (timeLeft) -> {

                    });
                break;
            case VOTING:
                // Initialize voting phase
                for (WWPlayer WWPlayer : game.getWWPlayerList()) {
                    WWPlayer.setVotedFor(null);
                    WWPlayer.clearVotes();
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
                        
                        initPhase(GamePhase.DUSK);
                    }, 
                    //on tick
                    (timeLeft) -> {

                    });
                break;
            case DEFENSE:
                // Initialize defense phase
                break;
            case JUDGMENT:
                // Initialize judgment phase
                break;
            case SENTENCE:
                // Initialize sentence phase
                break;
            case DUSK:
                // Initialize dusk phase
                for (WWPlayer WWPlayer : game.getWWPlayerList()) {
                    WWPlayer.getRole().dayAbility();
                }
                flushEvents();
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
                        plugin.setWorldTime((long)((1- (float)timeLeft / (Times.DUSK * 20)) * 12000L + 6000L));
                    });
                break;
            case NIGHT:
                // Initialize night phase
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
                        
                        initPhase(GamePhase.DAWN);
                    }, 
                    //on tick
                    (timeLeft) -> {

                    });
                break;
            case POST_NIGHT:
                // Initialize night phase
                plugin.setWorldTime(18000L);
                plugin.timerInstance.start(
                    Times.POST_NIGHT, 
                    BossBar.bossBar(
                        Component.text("Dawn breaks... " + dayCount, NamedTextColor.DARK_PURPLE),
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
                for (WWPlayer WWPlayer : game.getWWPlayerList()) {
                    WWPlayer.getRole().nightAbility();
                }
                flushEvents();
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
                        plugin.setWorldTime((long)((1- (float)timeLeft / (Times.DAWN * 20)) * 7000L + 23000L));
                    });
                break;
        }
        game.flushEvents();
    }
}

class GameEvent{
    int priority;
    public int getPriority() {
        return priority;
    }

}
