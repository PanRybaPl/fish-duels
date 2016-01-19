/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.duels;

/**
 *
 * @author PanRyba.pl
 */
public class StartDuelResult {
    private boolean started;
    private StartDuelReason reason;

    private StartDuelResult(boolean started, StartDuelReason reason) {
        this.started = started;
        this.reason = reason;
    }
    
    public static StartDuelResult alreadyInDuel() {
        return new StartDuelResult(false, StartDuelReason.ALREADY_IN_DUEL);
    }
    
    public static StartDuelResult started() {
        return new StartDuelResult(true, StartDuelReason.STARTED);
    }
    
    public static StartDuelResult noArenas() {
        return new StartDuelResult(true, StartDuelReason.NO_ARENAS);
    }
    
    public boolean getAllowed() {
        return this.started;
    }
    
    public StartDuelReason getReason() {
        return this.reason;
    }
}
