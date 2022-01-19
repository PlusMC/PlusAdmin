package org.plusmc.plusadmin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.plusmc.plusadmin.commands.*;
import org.plusmc.plusadmin.items.GravityGun;
import org.plusmc.plusadmin.items.PortalHead;
import org.plusmc.plusadmin.items.SlayButton;
import org.plusmc.pluslib.GeneralManager;
import org.plusmc.pluslib.commands.PlusCommand;
import org.plusmc.pluslib.item.PlusItem;

import java.util.List;
import java.util.logging.Logger;

public final class PlusAdmin extends JavaPlugin {
    private static final List<Listener> LISTENERS = List.of(
            new InvSee.Listener()
    );
    private static final List<PlusItem> ITEMS = List.of(
            new GravityGun(),
            new PortalHead(),
            new SlayButton()
    );
    private static final List<PlusCommand> COMMANDS = List.of(
            new AdminTool(),
            new InvSee(),
            new Lobby(),
            new RevertPlayer(),
            new Vanish()
    );

    public static PlusAdmin getInstance() {
        return JavaPlugin.getPlugin(PlusAdmin.class);
    }

    public static Logger logger() {
        return PlusAdmin.getInstance().getLogger();
    }

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) getDataFolder().mkdir();
        loadConfig();
        LISTENERS.forEach((lis) -> getServer().getPluginManager().registerEvents(lis, this));
        ITEMS.forEach(GeneralManager::register);
        COMMANDS.forEach(GeneralManager::register);
    }

    private void loadConfig() {
        FileConfiguration config = PlusAdmin.getInstance().getConfig();
        config.addDefault("player-backup-folder", "backups/world/playerdata");
        config.options().copyDefaults(true);
        PlusAdmin.getInstance().saveConfig();
    }
}
