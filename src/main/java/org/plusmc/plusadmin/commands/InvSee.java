package org.plusmc.plusadmin.commands;

import org.plusmc.plusadmin.PlusAdmin;
import org.plusmc.plusadmin.Utils.OtherUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.inventory.InventoryMoveItemEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import static org.plusmc.plusadmin.Utils.OtherUtil.getKeys;

public class InvSee implements PlusCommand, Listener {
    private static HashMap<Inventory, Inventory> INVENTORIES = new HashMap<>();

    @Override
    public String getName() {
        return "invsee";
    }

    @Override
    public String getPermission() {
        return "plusadmin.invsee";
    }

    @Override
    public String getUsage() {
        return "§7/invsee <player> <Inventory|EnderChest>";
    }

    @Override
    public String getDescription() {
        return "§7Allows you to see another players inventory in real time, including their EnderChest.";
    }

    @Override
    public void load() {
        INVENTORIES = new HashMap<>();
    }

    @Override
    public void unload() {
        for(Inventory inv : INVENTORIES.keySet()) {
            for(Iterator<HumanEntity> it = inv.getViewers().iterator(); it.hasNext();) {
                HumanEntity he = it.next();
                it.remove();
                he.closeInventory();
            }
        }
        INVENTORIES.clear();
    }

    @Override
    public List<String> getCompletions(int page) {
        List<String> args = new ArrayList<>();
        args = switch (page) {
            case 1 -> OtherUtil.allPlayers();
            case 2 -> List.of("Enderchest", "Inventory");
        }
        return args;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player)) {
            sender.sendMessage("§cYou must be a player to use this command.");
            return false;
        }
        Player p = (Player) sender;
        if(args.length == 0) {
            return false;
        }
        Player target = Bukkit.getPlayer(args[0]);
        if(target == null) {
            p.sendMessage("§cPlayer not found.");
            return true;
        }
        if(args.length == 1) {
            openInventory(p, target);
            return true;
        }
        switch (args[1]) {
            case "EnderChest":
                openEnderChest(p, target);
                break;
            case "Inventory":
                openInventory(p, target);
                break;
            default:
                return false;
        }
        return true;
    }


    public void openEnderChest(Player viewer, Player target) {
        viewer.openInventory(target.getEnderChest());
    }

    public void openInventory(Player viewer, Player target) {
        Inventory inv = target.getInventory();
        Inventory viewing = Bukkit.createInventory(null, 45, "§7Viewing " + target.getName() + "'s Inventory");
        viewing.setContents(inv.getContents());
        ItemStack placeholder = new ItemStack(Material.BARRIER);
        ItemMeta meta = placeholder.getItemMeta();
        meta.setDisplayName("§c§lEmpty Slot");
        placeholder.setItemMeta(meta);
        viewing.setItem(41, placeholder);
        viewing.setItem(42, placeholder);
        viewing.setItem(43, placeholder);
        viewing.setItem(44, placeholder);
        viewer.openInventory(viewing);
        INVENTORIES.put(viewing, inv);
    }

    public static void onInvModification(Cancellable event, Inventory inv, int slot) {
        if(event.isCancelled()) return;

        if(INVENTORIES.containsKey(inv)) {
            if(slot > 40) {
                event.setCancelled(true);
                return;
            }

            Bukkit.getScheduler().runTask(PlusAdmin.getInstance(), () -> {
                Inventory inv1 = INVENTORIES.get(inv);
                ItemStack[] contents = inv.getContents();
                ItemStack[] newContents = new ItemStack[inv1.getSize()];
                for(int i = 0; i < contents.length; i++) {
                    if(i < inv1.getSize())
                        newContents[i] = contents[i];
                }
                inv1.setContents(newContents);
            });
        } else if(INVENTORIES.containsValue(inv)) {
            Bukkit.getScheduler().runTask(PlusAdmin.getInstance(), () -> {
                List<Inventory> invs = getKeys(INVENTORIES, inv);
                if(invs.size() == 0) return;
                Inventory inv1 = invs.get(0);
                ItemStack[] contents = inv.getContents();
                ItemStack[] newContents = inv1.getContents();
                for(int i = 0; i < contents.length; i++) {
                    if(i < inv1.getSize())
                        newContents[i] = contents[i];
                }
                for(Inventory inv2 : invs) inv2.setContents(newContents);
            });
        }
    }

    public static class Listener implements org.bukkit.event.Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if(e.isCancelled()) return;

            if(e.getClickedInventory() == null) return;
            onInvModification(e, e.getClickedInventory(), e.getSlot());
        }

        @EventHandler
        public void onPlayerPickupItem(EntityPickupItemEvent e) {
            if(e.isCancelled()) return;
            if(!(e.getEntity() instanceof Player)) return;
            Player p = (Player) e.getEntity();

            onInvModification(e, p.getInventory(), 0);
        }

        @EventHandler
        public void onPlayerDropItem(PlayerDropItemEvent e) {
            if(e.isCancelled()) return;
            onInvModification(e, e.getPlayer().getInventory(), 0);
        }

        @EventHandler
        public void onPlayerSwapHandItems(PlayerSwapHandItemsEvent e) {
            if(e.isCancelled()) return;
            onInvModification(e, e.getPlayer().getInventory(), 0);
        }

        @EventHandler
        public void onPlayerInventoryMoveItem(InventoryMoveItemEvent e) {
            if(e.isCancelled()) return;
            onInvModification(e, e.getDestination(), 0);
        }

        @EventHandler
        public void onPlayerInventoryDrag(InventoryDragEvent e) {
            if(e.isCancelled()) return;
            onInvModification(e, e.getInventory(), 0);
        }

        @EventHandler
        public void onItemConsume(PlayerItemConsumeEvent e) {
            if(e.isCancelled()) return;
            onInvModification(e, e.getPlayer().getInventory(), 0);
        }

        @EventHandler
        public void onBlockPlace(BlockPlaceEvent e) {
            if(e.isCancelled()) return;
            onInvModification(e, e.getPlayer().getInventory(), 0);
        }


        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            INVENTORIES.remove(e.getInventory());
        }
    }
}
