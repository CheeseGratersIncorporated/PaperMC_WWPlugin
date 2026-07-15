package jenny.werewolf.papermc.plugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class WWPlayer {
    
    private String name;
    private Role role;
    private boolean isAlive = true, killed=false;
    private WWPlayer votedFor = null;
    private int voteCount = 0;
    private WWPlayer target =null;
    private House house=null;

    public WWPlayer(String name, Role role) {
        this.name = name;
        this.role = role;
    }
    public WWPlayer(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setHouse(House house){
    this.house=house;
    }

    public House getHouse(){
        return house;
    }

    public Role getRole() {
        return role;
    }

    public boolean isAlive() {
        return isAlive;
    }
    public boolean killed(){
        return killed;
    }
    public void kill(){
        killed=true;
        for(Player p : Bukkit.getServer().getOnlinePlayers()){
            if(p.getName()==getName()){
                p.kill();
                p.setAllowFlight(true);
                p.setInvisible(true);
            }
        }
    }

    public void setTarget(WWPlayer target) {
        this.target = target;
    }
    
    public WWPlayer getTarget() {
        return target;
    }

    public void setVotedFor(WWPlayer votedFor) {
        this.votedFor = votedFor;
    }
    public WWPlayer getVotedFor() {
        return this.votedFor;
    }
    
    public void clearVotes() {
        this.voteCount = 0;
    }

    public void addVote() {
        this.voteCount++;
    }

    public void removeVote() {
        this.voteCount--;
    }
    public int getVote() {
        return this.voteCount;
    }
    public void setAlive(boolean alive) {
        isAlive = alive;
    }
}
