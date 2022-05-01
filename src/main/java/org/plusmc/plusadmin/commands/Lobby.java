package org.plusmc.plusadmin.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.plusmc.pluslib.managed.PlusCommand;
import org.plusmc.pluslib.util.BungeeUtil;

import java.util.List;

public class Lobby implements PlusCommand {

    @Override
    public String getName() {
        return "lobby";
    }

    @Override
    public String getPermission() {
        return "";
    }

    @Override
    public String getUsage() {
        return "/lobby";
    }

    @Override
    public String getDescription() {
        return "Teleports you to the lobby";
    }

    @Override
    public List<String> getCompletions(int page) {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("You must be a player to use this command");
            return true;
        }
        BungeeUtil.connectServer(p, "lobby");
        return true;
    }
}
