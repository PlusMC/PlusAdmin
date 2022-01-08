package org.plusmc.plusadmin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.plusmc.plusadmin.PlusAdmin;

import java.util.ArrayList;
import java.util.List;

public class Vanish implements PlusCommand {

    private static List<Player> VANISHED;

    @Override
    public String getName() {
        return "vanish";
    }

    @Override
    public String getPermission() {
        return "plusadmin.vanish";
    }

    @Override
    public String getUsage() {
        return "/vanish";
    }

    @Override
    public String getDescription() {
        return "Make yourself completely invisible";
    }

    @Override
    public List<String> getCompletions(int page) {
        return null;
    }

    @Override
    public void load() {
        VANISHED = new ArrayList<>();
    }

    @Override
    public void unload() {
        for(Player p : VANISHED) {
            Bukkit.getOnlinePlayers().forEach(player -> player.showPlayer(PlusAdmin.getInstance(), p));
        }
        VANISHED.clear();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }
        Player player = (Player) sender;
        if(VANISHED.contains(player)) {
            VANISHED.remove(player);
            Bukkit.getOnlinePlayers().forEach(p -> p.showPlayer(PlusAdmin.getInstance(), player));
            sender.sendMessage("§aYou have §6unvanished!");
        } else {
            VANISHED.add(player);
            Bukkit.getOnlinePlayers().forEach(p -> p.hidePlayer(PlusAdmin.getInstance(), player));
            sender.sendMessage("§aYou have §6vanished!");
        }
        return true;
    }

    public static class Listener implements org.bukkit.event.Listener {
        @org.bukkit.event.EventHandler
        public void onPlayerJoin(org.bukkit.event.player.PlayerJoinEvent event) {
            VANISHED.forEach(player -> event.getPlayer().hidePlayer(PlusAdmin.getInstance(), player));
        }
    }
}
