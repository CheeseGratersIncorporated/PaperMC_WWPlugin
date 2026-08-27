package io.github.gegagegagega.werewolf;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;


import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

import java.util.Set;
import java.util.ArrayList;

public class WWPlayer {
    
    private String name;
    private Role role;
    private boolean isAlive = true, killed=false;
    private WWPlayer votedFor = null;
    private int voteCount = 0;
    private WWPlayer target =null;
    private House house=null;
    private int judgment=0;
    private WWPlayer specPlayer=null;
    private Set<WWPlayer> privateChatPlayers; 
    private boolean privateChat=false;
    private Component privateChatPrefix=Component.text("");
    private ArrayList<Component> causesOfDeath=new ArrayList<>(); 

    public WWPlayer(String name, Role role) {
        this.name = name;
        this.role = role;
    }
    public WWPlayer(String name) {
        this.name = name;
    }

    public void appendCauseOfDeath(Component c){
        causesOfDeath.add(c);
    }
    public ArrayList<Component> getCausesOfDeath(){
        return causesOfDeath;
    }

    public String getName() {
        return name;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public void setHouse(House h){
        house=h;
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
        
        
        for(Player player : Bukkit.getServer().getOnlinePlayers()){
            if(player.getName().equals(getName())){
                
                player.setAllowFlight(true);
                player.setFlying(true);

                player.setInvulnerable(true);
                player.setInvisible(true);
                player.setCanPickupItems(false);
                player.setCollidable(false);
                player.setSilent(true);
                // player.getInventory().clear();
            }
        }
        
        killed=true;
    }
    public void setSpecPlayer(WWPlayer wwp){
        specPlayer=wwp;
    }
    public WWPlayer getSpecPlayer(){
        return specPlayer;
    }
    public void setJudgment(int j){
        judgment=j;
    }
    public int getJudgment(){
        return judgment;
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

    public boolean getPrivateChat(){
        return privateChat;
    }
    public void setPrivateChat(boolean b){
        privateChat=b;
    }
    public void setPrivateChatPlayers(Set<WWPlayer> s){
        privateChatPlayers=s;
    }
    public Set<WWPlayer> getPrivateChatPlayers(){
        return privateChatPlayers;
    }
    public void setPrivateChatPrefix(Component c){
        privateChatPrefix=c;

    }
    public Component getPrivateChatPrefix(){
        return privateChatPrefix;
    }
}
