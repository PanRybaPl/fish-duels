/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.duels;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;

/**
 *
 * @author PanRyba.pl
 */
class DuelListener implements Listener {
    private final PluginApi api;

    public DuelListener(PluginApi api) {
        this.api = api;
    }
    
    @EventHandler
    protected void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        api.playerRespawned(player);
    }
    
    @EventHandler
    protected void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if(player == null) {
            return;
        }
        
        boolean cancelDrop = api.playerDied(player, event.getDrops());
        
        if(cancelDrop) {
            event.getDrops().clear();
            event.setDroppedExp(0);
        }
    }

    @EventHandler
    protected void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if(player == null) {
            return;
        }
        
        api.playerQuit(player);
    }
    
    @EventHandler
    protected void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if(player == null) {
            return;
        }
        
        api.onPlayerMove(player, event.getTo());
    }
}
