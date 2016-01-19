/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.duels;

/**
 *
 * @author PanRyba.pl
 */
public class Duel {
    private String playerA;
    private String playerB;
    
    private boolean started;
    private boolean completed;
    
    private String winner;
    private String loser;
    
    private DuelCompletedReason completedReason;
    
    Duel(String playerA, String playerB) {
        this.playerA = playerA;
        this.playerB = playerB;
    }
        
    public String[] getPlayers() {
        return new String[] { playerA, playerB };
    }

    void setLoser(String playerName, DuelCompletedReason reason) {
        if(this.playerA.equals(playerName)) {
            setBWinner();
        } else {
            setAWinner();
        }
        
        this.completedReason = reason;
    }

    private void setAWinner() {
        this.completed = true;
        this.winner = this.playerA;
        this.loser = this.playerB;
    }

    private void setBWinner() {
        this.completed = true;
        this.winner = this.playerB;
        this.loser = this.playerA;
    }
    
    public String getWinner() {
        return this.winner;
    }
    
    public String getLoser() {
        return this.loser;
    }
    
    public boolean getCompleted() {
        return this.completed;
    }

    public void setAsStarted() {
        this.started = true;
    }
    
    public boolean getStarted() {
        return this.started;
    }

    public DuelCompletedReason getReason() {
        return this.completedReason;
    }
}
