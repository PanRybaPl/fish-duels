/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.duels;

import org.bukkit.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.scheduler.BukkitTask;
import pl.panryba.mc.hardcore.HardcoreManager;

import java.util.*;
import java.util.logging.Level;

/**
 *
 * @author PanRyba.pl
 */
public class PluginApi {

    public Map<String, Duel> playerDuels;
    private final List<DuelArena> freeArenas;
    private final Map<Duel, DuelArena> assignedArenas;
    private final Set<String> blockedDuels;
    private final Map<String, DuelRequest> duelRequests;
    private final Map<String, List<ItemStack>> itemsToGive;    
    private final Map<Duel, BukkitTask> scheduleDuelTasks;
    private final org.bukkit.plugin.Plugin plugin;

    public PluginApi(org.bukkit.plugin.Plugin plugin) {
        this.plugin = plugin;
        this.playerDuels = new HashMap<>();
        this.freeArenas = new ArrayList<>();
        this.assignedArenas = new HashMap<>();
        this.scheduleDuelTasks = new HashMap<>();
        this.blockedDuels = new HashSet<>();
        this.duelRequests = new HashMap<>();
        this.itemsToGive = new HashMap<>();        
    }

    public StartDuelResult prepareDuel(Player playerA, Player playerB) {
        String playerAName = playerA.getName();
        String playerBName = playerB.getName();

        boolean aInDuel = isInDuel(playerAName);
        boolean bInDuel = isInDuel(playerBName);

        if (aInDuel || bInDuel) {
            return StartDuelResult.alreadyInDuel();
        }

        Duel duel = new Duel(playerAName, playerBName);
        DuelArena arena = this.reserveArena(duel);

        if (arena == null) {
            return StartDuelResult.noArenas();
        }
        
        for (String playerName : duel.getPlayers()) {
            this.duelRequests.remove(playerName);
            this.playerDuels.put(playerName, duel);

            arena.reserveSpawnForPlayer(playerName);
            
            teleportPlayer(playerName, arena.getPlayerSpectateLocation(playerName));            
            sendDuelMessage(playerName, ChatColor.YELLOW + "Zostales przeniesiony na arene. Przygotuj sie do walki!");
        }
        
        scheduleDuelStart(duel);

        return StartDuelResult.started();
    }
    
    private void healPlayer(String playerName) {
        Player player = Bukkit.getPlayerExact(playerName);
        if(player == null) {
            return;
        }
        
        player.setHealth(player.getMaxHealth());
        player.setFireTicks(0);
    }

    private boolean isInDuel(String playerName) {
        return this.playerDuels.get(playerName) != null;
    }

    public Duel getCurrentPlayerDuel(String playerName) {
        return this.playerDuels.get(playerName);
    }

    public boolean playerDied(Player player, List<ItemStack> drops) {
        boolean handled = this.playerLost(player, DuelCompletedReason.KILL);
        
        if(!handled) {
            return false;
        }
        
        List<ItemStack> items = new ArrayList<>(drops);
        this.itemsToGive.put(player.getName(), items);
        
        return true;
    }

    void playerQuit(Player player) {
        this.playerLost(player, DuelCompletedReason.QUIT);
    }

    private boolean playerLost(Player player, DuelCompletedReason reason) {
        String playerName = player.getName();

        Duel duel = this.getCurrentPlayerDuel(playerName);
        if (duel == null) {
            return false;
        }

        duel.setLoser(playerName, reason);
        this.completeDuel(duel);
        return true;
    }

    private void completeDuel(Duel duel) {
        String winner = duel.getWinner();
        String loser = duel.getLoser();
        
        String winnerMsg;
        String loserMsg;
        
        switch(duel.getReason()) {
            case ESCAPE:
                winnerMsg = ChatColor.RED + loser + ChatColor.YELLOW + " uciekl z areny, w zwiazku z czym wygrales pojedynek!";
                loserMsg = ChatColor.YELLOW + "Uciekles z areny i przegrales pojedynek z " + ChatColor.RED + winner;
                break;
            case QUIT:
                winnerMsg = ChatColor.RED + loser + ChatColor.YELLOW + " wyszedl z gry, w zwiazku z czym wygrales pojedynek!";
                loserMsg = ChatColor.YELLOW + "Wyszedles z gry i przegrales pojedynek z " + ChatColor.RED + winner;
                break;
            default:
                winnerMsg = ChatColor.YELLOW + "Brawo! Wygrales pojedynek z " + ChatColor.RED + loser + "!";
                loserMsg = ChatColor.RED + "Przegrales pojedynek z " + ChatColor.YELLOW + winner + "!";
                break;
        }
        
        this.sendDuelMessage(winner, winnerMsg);
        this.sendDuelMessage(loser, loserMsg);
        
        Bukkit.broadcastMessage(ChatColor.RED + winner + ChatColor.YELLOW + " wygral pojedynek 1vs1 z " + ChatColor.RED + loser);        
        
        this.completeDuelSchedule(duel);
        
        DuelArena arena = this.assignedArenas.remove(duel);
        
        for(String playerName : duel.getPlayers()) {
            this.playerDuels.remove(playerName);

            if(duel.getReason() == DuelCompletedReason.ESCAPE && playerName.equals(loser)) {
                // Do not teleport escaped players
                continue;
            }
            
            teleportPlayer(playerName, arena.getPlayerSpectateLocation(playerName));
        }
                
        this.freeArenas.add(arena);
        
        DuelCompletedEvent event = new DuelCompletedEvent(winner, loser);
        Bukkit.getServer().getPluginManager().callEvent(event);

        Bukkit.getLogger().info("[DUEL] " + winner + " > " + loser);
    }

    private DuelArena reserveArena(Duel duel) {
        if (this.freeArenas.isEmpty()) {
            return null;
        }

        DuelArena arena = this.freeArenas.remove(0);
        arena.resetArena();

        this.assignedArenas.put(duel, arena);
        return arena;
    }

    private void teleportPlayer(String playerName, Location spawn) {
        Player player = Bukkit.getPlayerExact(playerName);
        if (player == null) {
            return;
        }

        player.teleport(spawn);
    }
    
    private void sendDuelMessage(String playerName, String msg) {
        Player player = Bukkit.getPlayerExact(playerName);
        if(player == null) {
            return;
        }
        
        sendDuelMessage(player, msg);
    }

    public void sendDuelMessage(Player player, String msg) {
        String fullMsg = getDuelMessage(msg);
        player.sendMessage(fullMsg);
    }

    private void startDuel(Duel duel) {
        duel.setAsStarted();
        DuelArena arena = this.assignedArenas.get(duel);
        
        for(String playerName : duel.getPlayers()) {
            Location spawn = arena.getPlayerSpawnLocation(playerName);
            
            teleportPlayer(playerName, spawn);
            healPlayer(playerName);
            
            sendDuelMessage(playerName, ChatColor.RED + "Pojedynek rozpoczety! Ataaaaaaaaaak!!!");
        }
    }

    private void completeDuelSchedule(Duel duel) {
        BukkitTask task = this.scheduleDuelTasks.get(duel);
        if(task == null) {
            return;
        }
        
        task.cancel();
        this.scheduleDuelTasks.remove(duel);
    }

    public CanDuelResult canDuel(Player player, Player otherPlayer) {        
        if(player == otherPlayer) {
            return new CanDuelResult(false, CanDuelReason.SAME_PLAYER);
        }
        
        if(this.getHasBlockedDuels(otherPlayer.getName())) {
            return new CanDuelResult(false, CanDuelReason.BLOCKED);
        }
        
        Duel playerDuel = this.getCurrentPlayerDuel(player.getName());
        Duel otherPlayerDuel = this.getCurrentPlayerDuel(otherPlayer.getName());
        
        if(playerDuel != null && otherPlayerDuel != null) {
            if(playerDuel == otherPlayerDuel) {
                return new CanDuelResult(false, CanDuelReason.ALREADY_IN_DUEL);
            }
        }
        
        if(playerDuel != null) {
            return new CanDuelResult(false, CanDuelReason.YOU_ARE_IN_DUEL);
        }
        
        if(otherPlayerDuel != null) {
            return new CanDuelResult(false, CanDuelReason.OTHER_IN_DUEL);
        }
        
        HardcoreManager hcManager = HardcoreManager.getInstance();
        
        if(hcManager.getHasNewbieProtection(player)) {
            return new CanDuelResult(false, CanDuelReason.YOU_ARE_NEWBIE_PROTECTED);
        }
        
        if(hcManager.getHasNewbieProtection(otherPlayer)) {
            return new CanDuelResult(false, CanDuelReason.OTHER_IS_NEWBIE_PROTECTED);
        }

        if(!this.hasEnoughEmptySlots(player)) {
            return new CanDuelResult(false, CanDuelReason.NOT_ENOUGH_EMPTY_SLOTS);
        }
        
        return new CanDuelResult(true, CanDuelReason.ALLOWED);
    }

    private boolean hasEnoughEmptySlots(Player player) {
        PlayerInventory inv = player.getInventory();

        int empty = 0;
        for(ItemStack i : inv.getContents()) {
            if(i == null || i.getType() == Material.AIR) {
                if(++empty >= 4) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean getHasBlockedDuels(String name) {
        return this.blockedDuels.contains(name);
    }
    
    public void blockDuels(Player player) {
        this.blockedDuels.add(player.getName());
    }
    
    public void unblockDuels(Player player) {
        this.blockedDuels.remove(player.getName());
    }
    
    public boolean requiresAsk(Player player, Player otherPlayer) {
        DuelRequest request = this.duelRequests.get(otherPlayer.getName());
        if(request != null) {
            if(!this.isRequestValid(request)) {
                request = null;
                this.duelRequests.remove(otherPlayer.getName());
            }
        }
        if(request == null) {
            return true;
        }
        
        return !request.getOther().equals(player.getName());
    }
    
    private boolean isRequestValid(DuelRequest request) {
        long at = request.getRequestedAt().getTime();
        long validity = new Date().getTime() - 60 * 1000;
        return at >= validity;
    }

    public void askForDuel(Player player, Player otherPlayer) {
        DuelRequest currentRequest = this.duelRequests.get(player.getName());
        
        if(currentRequest != null) {
            if(!this.isRequestValid(currentRequest)) {
                currentRequest = null;
                this.duelRequests.remove(player.getName());
            }
        }
        
        if(currentRequest != null) {
            if(currentRequest.getOther().equals(otherPlayer.getName())) {
                sendDuelMessage(player, ChatColor.YELLOW + "Juz wyzwales gracza " + otherPlayer.getName() + " i oczekujesz na jego odpowiedz");
                return;
            } else {
                this.duelRequests.remove(player.getName());
                sendDuelMessage(currentRequest.getOther(), ChatColor.RED + player.getName() + ChatColor.YELLOW + " anulowal wyzwanie Ciebie do pojedynku");                
            }
        }
        
        DuelRequest request = new DuelRequest(player.getName(), otherPlayer.getName());
        this.duelRequests.put(player.getName(), request);

        this.sendDuelMessage(otherPlayer, ChatColor.RED + player.getName() +
                ChatColor.YELLOW + " wyzwal Cie na pojedynek! Wpisz " +
                ChatColor.GREEN + "/pojedynek " + player.getName() + ChatColor.YELLOW + " aby przyjac wyzwanie! (masz na to 60 sekund)");

        this.sendDuelMessage(otherPlayer, ChatColor.RED + "Pamietaj aby zrobic w plecaku lub skrzyni kresu miejsce na swoje przedmioty, inaczej stracisz je gdy przegrasz pojedynek!");

        this.sendDuelMessage(player, ChatColor.YELLOW + "Wyzwales " + ChatColor.RED + otherPlayer.getName() +
                ChatColor.YELLOW + " na pojedynek! Czy Twoj przeciwnik przyjmie wyzwanie? (wazne przez 60 sekund)");

        this.sendDuelMessage(player, ChatColor.RED + "Pamietaj aby zrobic w plecaku lub skrzyni kresu miejsce na swoje przedmioty, inaczej stracisz je gdy przegrasz pojedynek!");
    }

    void loadArenas(FileConfiguration config) {
        Set<String> names = config.getKeys(false);
        
        for(String name : names) {
            ConfigurationSection item = config.getConfigurationSection(name);
            DuelArena arena = DuelArena.deserialize(item);
            this.freeArenas.add(arena);
        }
        
        Bukkit.getLogger().log(Level.INFO, "[DUELS] Loaded arenas: {0}", this.freeArenas.size());
    }

    private String getDuelMessage(String msg) {
        return "[" + ChatColor.RED + "POJEDYNEK" + ChatColor.RESET + "] " + msg;
    }

    void onPlayerMove(Player player, Location to) {
        Duel duel = getCurrentPlayerDuel(player.getName());
        
        if(duel == null) {
            return;
        }
        
        if(!duel.getStarted() || duel.getCompleted()) {
            return;
        }
        
        DuelArena arena = this.getDuelArena(duel);
        if(arena.isInFightRegion(to)) {
            return;
        }
        
        this.playerLost(player, DuelCompletedReason.ESCAPE);
    }

    private DuelArena getDuelArena(Duel duel) {
        return this.assignedArenas.get(duel);
    }
    
    void playerRespawned(Player player) {
        List<ItemStack> toGive = this.itemsToGive.remove(player.getName());
        
        if(toGive == null) {
            Bukkit.getLogger().info("[DUEL RESPAWN NOITEMS] " + player.getName());
            return;
        }

        PlayerInventory inv = player.getInventory();

        Location playerLoc = player.getLocation();
        World world = playerLoc.getWorld();
        
        List<ItemStack> invFails = new ArrayList<>();
        
        for(ItemStack item : toGive) {
            HashMap<Integer, ItemStack> failed = inv.addItem(item);
            
            if(!failed.isEmpty()) {
                invFails.addAll(failed.values());
            }
        }
        
        Inventory ender = player.getEnderChest();
        
        boolean enderUsed = false;
        List<ItemStack> enderFails = new ArrayList<>();
        
        if(ender != null) {
            for(ItemStack item : invFails) {
                HashMap<Integer, ItemStack> failed = ender.addItem(item);
                
                if(!failed.isEmpty()) {
                    enderFails.addAll(failed.values());
                } else {
                    enderUsed = true;
                }
            }
        }
        
        boolean dropUsed = false;
        
        if(!enderFails.isEmpty()) {
            dropUsed = true;
            for(ItemStack item : enderFails) {
                world.dropItem(playerLoc, item);
                Bukkit.getLogger().info("[DUEL DROP] " + player.getName() + " " + item);
            }
        }

        Bukkit.getLogger().info("[DUEL RESPAWN ITEMS] " + player.getName() + " ender: " + enderUsed + " drop: " + dropUsed);

        if(enderUsed && dropUsed) {
            sendDuelMessage(player, ChatColor.YELLOW + "Czesc Twoich przedmiotow wrocila do Twojej skrzyni kresu (Ender Chest), jednak nie wszystkie zmiescily sie i zostaly wyrzucone na ziemie.");
        } else if (enderUsed) {
            sendDuelMessage(player, ChatColor.YELLOW + "Czesc Twoich przedmiotow wrocila do Twojej skrzyni kresu (Ender Chest)");
        } else if (dropUsed) {
            sendDuelMessage(player, ChatColor.YELLOW + "Czesc Twoich przedmiotow zostala wyrzucona na ziemie poniewaz nie zmiescila sie w Twoim plecaku ani skrzyni kresu");
        }
    }
    
    private class ScheduleDuel implements Runnable {
        private final PluginApi api;
        private final Duel duel;

        private ScheduleDuel(PluginApi api, Duel duel) {
            this.api = api;
            this.duel = duel;
        }

        @Override
        public void run() {
            api.completeDuelSchedule(this.duel);
            api.startDuel(this.duel);
        }
        
    }

    private void scheduleDuelStart(Duel duel) {
        BukkitTask task = Bukkit.getScheduler().runTaskLater(this.plugin, new ScheduleDuel(this, duel), 20 * 10);
        this.scheduleDuelTasks.put(duel, task);
    }

}
