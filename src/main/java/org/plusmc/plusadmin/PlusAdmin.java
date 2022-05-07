package org.plusmc.plusadmin;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.plusmc.plusadmin.commands.*;
import org.plusmc.plusadmin.items.GravityGun;
import org.plusmc.plusadmin.items.PortalHead;
import org.plusmc.plusadmin.items.SlayButton;
import org.plusmc.pluslib.bukkit.managed.PlusCommand;
import org.plusmc.pluslib.bukkit.managed.PlusItem;
import org.plusmc.pluslib.bukkit.managing.BaseManager;
import org.plusmc.pluslib.bukkit.managing.PlusCommandManager;
import org.plusmc.pluslib.bukkit.managing.PlusItemManager;
import org.plusmc.pluslib.bukkit.managing.TickingManager;

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
        BaseManager.createManager(PlusCommandManager.class, this);
        BaseManager.createManager(PlusItemManager.class, this);
        BaseManager.createManager(TickingManager.class, this);


        ITEMS.forEach(item->BaseManager.registerAny(item, this));
        COMMANDS.forEach(command->BaseManager.registerAny(command, this));

        if (!getDataFolder().exists()) getDataFolder().mkdir();
        loadConfig();
        LISTENERS.forEach((lis) -> getServer().getPluginManager().registerEvents(lis, this));
    }

    private void loadConfig() {
        FileConfiguration config = PlusAdmin.getInstance().getConfig();
        config.addDefault("player-backup-folder", "backups/world/playerdata");
        config.options().copyDefaults(true);
        PlusAdmin.getInstance().saveConfig();
    }

    @Override
    public void onDisable() {
        BaseManager.shutdownAll(this);
    }
}
