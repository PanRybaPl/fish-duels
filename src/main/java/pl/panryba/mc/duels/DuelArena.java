/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.duels;

import com.sk89q.worldedit.Vector;
import com.sk89q.worldguard.bukkit.WGBukkit;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;

/**
 *
 * @author PanRyba.pl
 */
public class DuelArena {
    
    private static Location deserializeLocation(Map<String, Object> map) {
        World world = Bukkit.getWorld((String)map.get("world"));
        int x = (int)map.get("x");
        int y = (int)map.get("y");
        int z = (int)map.get("z");
        float yaw = (float)(double)map.get("yaw");
        float pitch = (float)(double)map.get("pitch");
        
        return new Location(world, x, y, z, yaw, pitch);
    }
    
    private Location spectatorLocation;
    private Set<Location> spawns;
    private List<Location> freeSpawns;
    private Map<String, Location> playerSpawns;
    private World fightWorld;
    private ProtectedRegion fightRegion;
    
    public static DuelArena deserialize(ConfigurationSection map) {
        Set<Location> spawns = new HashSet<>();
        
        List<Map<String, Object>> spawnsList = (List<Map<String, Object>>)map.getList("spawns");
        
        for(Map<String, Object> spawnLocationMap : spawnsList) {
            Location location = deserializeLocation(spawnLocationMap);
            spawns.add(location);
        }
        
        ConfigurationSection regionSection = map.getConfigurationSection("region");
        World fightWorld = Bukkit.getWorld(regionSection.getString("world"));
        
        ProtectedRegion fightRegion = readRegion(fightWorld, regionSection);
        
        Map<String, Object> spectateMap = map.getConfigurationSection("spectate").getValues(false);
        Location spectate = deserializeLocation(spectateMap);
        
        DuelArena arena = new DuelArena(spawns, spectate, fightWorld, fightRegion);
        return arena;
    }
    
    public DuelArena(Set<Location> spawns, Location spectateLocation, World fightWorld, ProtectedRegion fightRegion) {
        this.spawns = spawns;
        this.spectatorLocation = spectateLocation;
        this.fightRegion = fightRegion;
        this.fightWorld = fightWorld;
        
        this.resetArena();
    }
    
    private static ProtectedRegion readRegion(World world, ConfigurationSection section) {
        String name = section.getString("name");        
        return WGBukkit.getPlugin().getRegionManager(world).getRegion(name);
    }    
    
    public Location reserveSpawnForPlayer(String playerName) {
        if(this.freeSpawns.isEmpty()) {
            return null;
        }
        
        Location spawnLocation = this.playerSpawns.get(playerName);
        if(spawnLocation != null) {
            return spawnLocation;
        }
        
        spawnLocation = freeSpawns.remove(0);
        this.playerSpawns.put(playerName, spawnLocation);
        
        return spawnLocation;
    }
    
    public Location getPlayerSpectateLocation(String playerName) {
        return this.spectatorLocation;
    }
    
    public Location getPlayerSpawnLocation(String playerName) {
        return this.playerSpawns.get(playerName);
    }

    public final void resetArena() {
        this.freeSpawns = new ArrayList<>();
        this.playerSpawns = new HashMap<>();
        
        this.freeSpawns.addAll(this.spawns);
    }
    
    public boolean isInFightRegion(Location location) {
        if(this.fightWorld != location.getWorld()) {
            return false;
        }
        
        Vector locVector = new Vector(location.getX(), location.getY(), location.getZ());
        return this.fightRegion.contains(locVector);
    }
}