package org.plusmc.plusadmin.commands;

import org.plusmc.plusadmin.PlusAdmin;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.SimplePluginManager;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public interface PlusCommand extends CommandExecutor, TabCompleter {
    List<PlusCommand> COMMANDS = List.of(
            new AdminTool(),
            new RevertPlayer(),
            new InvSee(),
            new Vanish(),
            new Test()
    );

    String getName();
    String getPermission();
    String getUsage();
    String getDescription();
    List<String> getCompletions(int page);
    default void load() {
        // ignore
    }
    default void unload() {
        // ignore
    }

    default List<String> filterCompletions(String arg, int page) {
        String arg2 = arg.toLowerCase();
        List<String> completions = getCompletions(page) == null ? new ArrayList<>() : getCompletions(page);
        List<String> filtered = new ArrayList<>();
        completions.forEach(s -> {
            if (s.toLowerCase().startsWith(arg2))
                filtered.add(s);
        });
        return filtered;
    }

    @Override
    default List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (args.length == 0)
            return new ArrayList<>();
        return filterCompletions(args[args.length - 1], args.length);
    }

    static void loadCommands() {
        CommandMap commandMap = getCommandMap();
        for(PlusCommand cmd : COMMANDS) {
            PluginCommand command = createCommand(cmd.getName(), PlusAdmin.getInstance());
            command.setExecutor(cmd);
            command.setTabCompleter(cmd);
            command.setPermission(cmd.getPermission());
            command.setUsage(cmd.getUsage());
            command.setDescription(cmd.getDescription());
            commandMap.register(PlusAdmin.getInstance().getName(), command);
            cmd.load();
            PlusAdmin.logger().info("Loaded command: " + cmd.getName());
        }
    }

    static void unloadCommands() {
        CommandMap commandMap = getCommandMap();
        for(PlusCommand cmd : COMMANDS) {
            PluginCommand command = PlusAdmin.getInstance().getCommand(cmd.getName());
            if (command == null)
                continue;
            command.unregister(commandMap);
            cmd.unload();
            PlusAdmin.logger().info("Unloaded command: " + cmd.getName());
        }
    }

    private static CommandMap getCommandMap() {
        CommandMap commandMap = null;
        try {
            if (Bukkit.getPluginManager() instanceof SimplePluginManager) {
                Field f = SimplePluginManager.class.getDeclaredField("commandMap");
                f.setAccessible(true);

                commandMap = (CommandMap) f.get(Bukkit.getPluginManager());
            }
        } catch (Exception e) {
            PlusAdmin.logger().severe("Failed to get CommandMap");
        }

        return commandMap;
    }

    private static PluginCommand createCommand(String name, Plugin plugin) {
        PluginCommand command = null;

        try {
            Constructor<PluginCommand> c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);

            command = c.newInstance(name, plugin);
        } catch (Exception e) {
            PlusAdmin.logger().severe("Failed to create PluginCommand");
        }

        return command;
    }

}
