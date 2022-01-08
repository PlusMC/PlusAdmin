package org.plusmc.plusadmin;

import org.plusmc.plusadmin.commands.InvSee;
import org.plusmc.plusadmin.commands.PlusCommand;
import org.plusmc.plusadmin.events.InteractEvents;
import org.plusmc.plusadmin.items.PlusItem;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.logging.Logger;

public final class PlusAdmin extends JavaPlugin {
    static final List<Listener> LISTENERS = List.of(
            new InteractEvents(),
            new InvSee.Listener()
    );


    @Override
    public void onEnable() {
        if(!getDataFolder().exists()) getDataFolder().mkdir();
        loadConfig();
        LISTENERS.forEach((lis) -> getServer().getPluginManager().registerEvents(lis, this));
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        PlusCommand.loadCommands();
        PlusItem.loadAll();
    }

    @Override
    public void onDisable() {
        Bukkit.getMessenger().unregisterOutgoingPluginChannel(this, "BungeeCord");
        PlusItem.unloadAll();
        PlusCommand.unloadCommands();
    }

    private void loadConfig() {
        FileConfiguration config =  PlusAdmin.getInstance().getConfig();
        config.addDefault("player-backup-folder", "backups/world/playerdata");
        config.options().copyDefaults(true);
        PlusAdmin.getInstance().saveConfig();
    }

    public static PlusAdmin getInstance() {
        return JavaPlugin.getPlugin(PlusAdmin.class);
    }

    public static Logger logger() {
        return PlusAdmin.getInstance().getLogger();
    }
}
