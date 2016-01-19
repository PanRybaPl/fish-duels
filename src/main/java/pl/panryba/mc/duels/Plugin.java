/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package pl.panryba.mc.duels;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import pl.panryba.mc.duels.commands.DuelCommand;

/**
 *
 * @author PanRyba.pl
 */
public class Plugin extends JavaPlugin {

    @Override
    public void onEnable() {
        PluginApi api = new PluginApi(this);
        
        File arenasFile = new File(getDataFolder(), "arenas.yml");
        YamlConfiguration arenasConfig = new YamlConfiguration();
        
        try {
            arenasConfig.load(arenasFile);
            api.loadArenas(arenasConfig);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | InvalidConfigurationException ex) {
            Logger.getLogger(Plugin.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        getServer().getPluginManager().registerEvents(new DuelListener(api), this);
        getCommand("pojedynek").setExecutor(new DuelCommand(api));
    }
}
