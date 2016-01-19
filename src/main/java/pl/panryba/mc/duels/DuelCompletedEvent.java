/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.duels;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
 
public final class DuelCompletedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final String winner;
    private final String loser;
 
    public DuelCompletedEvent(String winner, String loser) {
        this.winner = winner;
        this.loser = loser;
    }
    
    public String getWinner() {
        return this.winner;
    }
    
    public String getLoser() {
        return this.loser;
    }
 
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }
 
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
