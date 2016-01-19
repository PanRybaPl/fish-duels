/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.duels;

import java.util.Date;

/**
 *
 * @author PanRyba.pl
 */
public class DuelRequest {
    
    private String player;
    private String other;
    private Date requestedAt;
    
    public DuelRequest(String player, String other) {
        this.player = player;
        this.other = other;
        this.requestedAt = new Date();
    }

    public String getOther() {
        return this.other;
    }
    
    public String getPlayer() {
        return this.player;
    }
    
    public Date getRequestedAt() {
        return this.requestedAt;
    }
    
}
