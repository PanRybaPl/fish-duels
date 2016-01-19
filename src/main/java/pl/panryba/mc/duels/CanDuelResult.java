/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.duels;

/**
 *
 * @author PanRyba.pl
 */
public class CanDuelResult {
    private boolean result;
    private CanDuelReason reason;
    
    public CanDuelResult(boolean result, CanDuelReason reason) {
        this.result = result;
        this.reason = reason;
    }
    
    public boolean getResult() {
        return this.result;
    }
    
    public CanDuelReason getReason() {
        return this.reason;
    }
}
