package org.plusmc.plusadmin.commands;

import org.plusmc.plusadmin.PlusAdmin;
import org.plusmc.plusadmin.Utils.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class RevertInventory implements PlusCommand {

    @Override
    public String getName() {
        return "revertinventory";
    }

    @Override
    public String getPermission() {
        return "plusadmin.revertinventory";
    }

    @Override
    public String getUsage() {
        return "§7/revertinventory <backup> <player>";
    }

    @Override
    public String getDescription() {
        return "§7Reverts a players inventory to a saved backup";
    }

    @Override
    public List<String> getCompletions(int page) {
        List<String> args = new ArrayList<>();
        switch (page) {
            case 1:
                if(getPlayerBackupFolder() == null) return args;
                PlusAdmin.logger().info(getPlayerBackupFolder().getAbsolutePath());
                File[] files = getPlayerBackupFolder().listFiles();
                if(files == null) return args;
                for(File file : files) {
                    args.add(file.getName());
                }
                break;
            case 2:
                Bukkit.getOnlinePlayers().forEach(player -> args.add(player.getName()));
                break;
        }
        return args;
    }

    public static File getPlayerBackupFolder() {
        String path = PlusAdmin.getInstance().getConfig().getString("player-backup-folder");
        if(path == null) return null;
        return new File(path);
    }



    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 2) {
            return false;
        }

        String folder = args[0];
        File backup = new File(getPlayerBackupFolder(), folder);
        if(!backup.exists()) {
            sender.sendMessage("§cBackup does not exist!");
            return true;
        }

        String playerName = args[1];
        OfflinePlayer offlinePlayer = null;
        String UUID = null;
        for(OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if(player.getName() == null) continue;
            if(player.getName().equalsIgnoreCase(playerName)) {
                UUID = player.getUniqueId().toString();
                offlinePlayer = player;
                break;
            }
        }

        if(UUID == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        byte[] data = FileUtil.readZip(backup,"playerdata/" + UUID + ".dat");
        if(data == null) {
            sender.sendMessage("§cPlayer data not found!");
            return true;
        }

        Player p = offlinePlayer.getPlayer();
        boolean online = p != null;
        if(online)
            p.kickPlayer("§cReverting inventory...");


        byte[] oldData = FileUtil.readData(new File("world/playerdata/" + UUID + ".dat"));
        FileUtil.writeZip(new File(getPlayerBackupFolder(), "revertbackup.zip"),  "playerdata/" + UUID + ".dat", oldData);

        FileUtil.rewriteData(new File("world/playerdata/" + UUID + ".dat"), data);

        sender.sendMessage("§aReverted inventory!");


        return true;
    }

}
