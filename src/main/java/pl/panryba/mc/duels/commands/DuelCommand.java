/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.duels.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import pl.panryba.mc.duels.CanDuelResult;
import pl.panryba.mc.duels.PluginApi;
import pl.panryba.mc.duels.StartDuelResult;

/**
 *
 * @author PanRyba.pl
 */
public class DuelCommand implements CommandExecutor {
    private final PluginApi api;
    
    public DuelCommand(PluginApi api) {
        this.api = api;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        if(!(cs instanceof Player)) {
            return false;
        }
        
        if(strings.length < 1) {
            return false;
        }
        
        Player player = (Player)cs;
        String otherPlayerName = strings[0];
        Player otherPlayer = Bukkit.getPlayer(otherPlayerName);
        
        if(otherPlayer == null) {
            api.sendDuelMessage(player, ChatColor.YELLOW + "Nie odnaleziono takiego gracza");
            return true;
        }
        
        CanDuelResult result = api.canDuel(player, otherPlayer);
        
        if(!result.getResult()) {
            switch(result.getReason()) {
                case SAME_PLAYER:
                    api.sendDuelMessage(player, ChatColor.YELLOW + "Wyzwales samego siebie na pojedynek, ktory zakonczyl sie remisem :)");
                    break;
                case ALREADY_IN_DUEL:
                    api.sendDuelMessage(player, ChatColor.YELLOW + "Juz bierzesz udzial w pojedynku z tym graczem");
                    break;
                case OTHER_IN_DUEL:
                    api.sendDuelMessage(player, ChatColor.YELLOW + "Wybrany gracz juz bierze udzial w pojedynku");
                    break;
                case BLOCKED:
                    api.sendDuelMessage(player, ChatColor.YELLOW + "Wybrany gracz zablokowal mozliwosc wyzwania go do pojedynku");
                    break;
                case YOU_ARE_IN_DUEL:
                    api.sendDuelMessage(player, ChatColor.YELLOW + "Juz bierzesz udzial w innym pojedynku");
                    break;
                case YOU_ARE_NEWBIE_PROTECTED:
                    api.sendDuelMessage(player, ChatColor.YELLOW + "Nie mozesz brac udzialu w pojedynkach poniewaz jestes chroniony");
                    break;
                case OTHER_IS_NEWBIE_PROTECTED:
                    api.sendDuelMessage(player, ChatColor.YELLOW + otherPlayer.getName() + " nie moze brac udzialu w pojedynkach poniewaz jest chroniony");
                    break;
                case NOT_ENOUGH_EMPTY_SLOTS:
                    api.sendDuelMessage(player, ChatColor.YELLOW + "Masz za malo wolnego miejsca w plecaku aby wziac udzial w pojedynku");
                    break;
            }
            
            return true;
        }
        
        if(api.requiresAsk(player, otherPlayer)) {
            api.askForDuel(player, otherPlayer);
            return true;
        }
        
        StartDuelResult prepareResult = api.prepareDuel(player, otherPlayer);
        if(!prepareResult.getAllowed()) {
            switch(prepareResult.getReason()) {
                case ALREADY_IN_DUEL:
                    api.sendDuelMessage(player, ChatColor.YELLOW + "Ty lub Twoj przeciwnik bierzeciez juz udzial w pojedynku");
                    break;
                case NO_ARENAS:
                    api.sendDuelMessage(player, ChatColor.YELLOW + "Obecnie wszystkie areny sa zajete");
                    break;
            }
        }
        
        return true;
    }
    
}
