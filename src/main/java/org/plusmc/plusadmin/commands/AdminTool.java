package org.plusmc.plusadmin.commands;

import org.plusmc.plusadmin.items.PlusItem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class AdminTool implements PlusCommand {
    @Override
    public String getName() {
        return "admintool";
    }

    @Override
    public String getPermission() {
        return "plusadmin.admintool";
    }

    @Override
    public String getUsage() {
        return "§7/admintool <tool>";
    }

    @Override
    public String getDescription() {
        return "§7Gives you a admin tool.";
    }

    @Override
    public List<String> getCompletions(int page) {
        List<String> items = new ArrayList<>();
        if(page == 1)
            PlusItem.CUSTOM_ITEMS.forEach(item -> items.add(item.getID()));

        return items;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) return false;
        Player p = (Player) sender;

        if(args.length != 1) {
            return false;
        }

        PlusItem item = PlusItem.getCustomItem(args[0]);
        if(item == null)  {
            p.sendMessage("§cThat item does not exist!");
            return true;
        }
        p.getInventory().addItem(item.getItem());
        p.sendMessage("§aYou have been given a §e" + item.getID() + "§a!");
        return true;
    }


}
