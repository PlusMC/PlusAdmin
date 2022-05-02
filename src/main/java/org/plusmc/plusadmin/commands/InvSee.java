package org.plusmc.plusadmin.commands;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.Containers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_18_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R2.event.CraftEventFactory;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftContainer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_18_R2.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R2.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.plusmc.pluslib.bukkit.managed.PlusCommand;
import org.plusmc.pluslib.bukkit.util.BukkitUtil;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class InvSee implements PlusCommand {
    private static HashMap<Inventory, Inventory> INVENTORIES = new HashMap<>();

    @Override
    public void load() {
        INVENTORIES = new HashMap<>();
    }

    @Override
    public void unload() {
        for (Inventory inv : INVENTORIES.values())
            for (Iterator<HumanEntity> it = inv.getViewers().iterator(); it.hasNext(); ) {
                HumanEntity humanEntity = it.next();
                it.remove();
                humanEntity.closeInventory();
            }
        INVENTORIES.clear();
    }

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
    public List<String> getCompletions(int page) {
        return switch (page) {
            case 1 -> BukkitUtil.allPlayers();
            case 2 -> List.of("Enderchest", "Inventory");
            default -> null;
        };
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player p)) {
            sender.sendMessage("§cYou must be a player to use this command.");
            return true;
        }

        if (args.length == 0) {
            return false;
        }

        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            p.sendMessage("§cPlayer not found.");
            return true;
        }

        if (target == p) {
            p.sendMessage("§cYou can't see your own inventory.");
            return true;
        }

        if (args.length == 1) {
            openInventory(p, target);
            return true;
        }

        switch (args[1]) {
            case "Enderchest" -> openEnderchest(p, target);
            case "Inventory" -> openInventory(p, target);
            default -> p.sendMessage("§cInvalid inventory type.");
        }
        return true;
    }


    public void openEnderchest(Player viewer, Player target) {
        viewer.openInventory(target.getEnderChest());
    }

    public void openInventory(Player viewer, Player target) {
        try {
            CraftPlayer craftPlayer = (CraftPlayer) target;

            Field f = CraftHumanEntity.class.getDeclaredField("inventory");
            f.setAccessible(true);

            PlayerInventory inv = ((CraftInventoryPlayer) f.get(craftPlayer)).getInventory();

            Inventory inventory = new CraftInventory(new LinkedInv(inv.h, inv.i, inv.j));

            EntityPlayer ePlayer = ((CraftPlayer) viewer).getHandle();

            Container container = new CraftContainer(inventory, ePlayer, ePlayer.nextContainerCounter());
            container = CraftEventFactory.callInventoryOpenEvent(ePlayer, container);
            if (container != null) {
                String title = "§6§l" + target.getName() + "§7's Inventory";
                ePlayer.b.a(new PacketPlayOutOpenWindow(container.j, Containers.e, CraftChatMessage.fromString(title)[0]));
                ePlayer.bV = container;
                ePlayer.a(container);
                INVENTORIES.put(target.getInventory(), viewer.getOpenInventory().getTopInventory());
            }
        } catch (Exception e) {
            viewer.sendMessage("§cAn error occurred while trying to open the inventory.");
        }
    }

    public static class Listener implements org.bukkit.event.Listener {
        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            if (INVENTORIES.containsValue(e.getClickedInventory()))
                if (e.getSlot() > 40) e.setCancelled(true);
        }

        @EventHandler
        public void onPlayerLeave(PlayerQuitEvent e) {
            if (INVENTORIES.containsKey(e.getPlayer().getInventory())) {
                for (Iterator<HumanEntity> it = INVENTORIES.get(e.getPlayer().getInventory()).getViewers().iterator(); it.hasNext(); ) {
                    HumanEntity humanEntity = it.next();
                    it.remove();
                    humanEntity.closeInventory();
                }
                INVENTORIES.remove(e.getPlayer().getInventory());
            }
        }

        @EventHandler
        public void onInventoryClose(InventoryCloseEvent e) {
            INVENTORIES.remove(e.getInventory());
        }
    }

    static class LinkedInv implements IInventory {
        private final NonNullList<net.minecraft.world.item.ItemStack> contents;
        private final NonNullList<net.minecraft.world.item.ItemStack> armor;
        private final NonNullList<net.minecraft.world.item.ItemStack> extra;
        private final List<NonNullList<net.minecraft.world.item.ItemStack>> all;
        private final net.minecraft.world.item.ItemStack placeholder;
        private final List<HumanEntity> viewers;
        private int maxStack;


        public LinkedInv(NonNullList<net.minecraft.world.item.ItemStack> contents, NonNullList<net.minecraft.world.item.ItemStack> armor, NonNullList<net.minecraft.world.item.ItemStack> extra) {
            this.maxStack = 64;
            ItemStack placeholder = new ItemStack(Material.BARRIER);
            ItemMeta meta = placeholder.getItemMeta();
            if (meta != null) meta.setDisplayName("§c§lEmpty Slot");
            placeholder.setItemMeta(meta);
            this.placeholder = CraftItemStack.asNMSCopy(placeholder);
            this.contents = contents;
            this.armor = armor;
            this.extra = extra;
            this.all = new ArrayList<>();
            this.all.add(this.contents);
            this.all.add(this.armor);
            this.all.add(this.extra);
            this.viewers = new ArrayList<>();
        }

        public int b() { // getSize
            return 45;
        }

        public net.minecraft.world.item.ItemStack a(int i) { //getItem
            if (i < 36) {
                return this.contents.get(i);
            } else if (i < 40) {
                return this.armor.get(i - 36);
            } else if (i < 41) {
                return this.extra.get(0);
            } else if (i < 45) {
                return placeholder;
            }
            return net.minecraft.world.item.ItemStack.b;
        }

        public net.minecraft.world.item.ItemStack a(int i, int j) { //???
            net.minecraft.world.item.ItemStack stack = this.a(i); //gets the stack at i
            if (stack == net.minecraft.world.item.ItemStack.b) { //returns if stack is null
                return stack;
            } else {
                net.minecraft.world.item.ItemStack result;
                if (stack.I() <= j) {
                    this.a(i, net.minecraft.world.item.ItemStack.b);
                    result = stack;
                } else {
                    result = CraftItemStack.copyNMSStack(stack, j);
                    stack.g(j);
                }

                this.e();
                return result;
            }
        }

        public net.minecraft.world.item.ItemStack b(int i) { //???? looks similar to the top one
            net.minecraft.world.item.ItemStack stack = this.a(i);
            if (stack == net.minecraft.world.item.ItemStack.b) {
                return stack;
            } else {
                net.minecraft.world.item.ItemStack result;
                if (stack.I() <= 1) {
                    this.a(i, null);
                    result = stack;
                } else {
                    result = CraftItemStack.copyNMSStack(stack, 1);
                    stack.g(1);
                }

                return result;
            }
        }

        public void a(int i, net.minecraft.world.item.ItemStack itemstack) { //set item
            if (i < 36) {
                this.contents.set(i, itemstack);
            } else if (i < 40) {
                this.armor.set(i - 36, itemstack);
            } else if (i < 41) {
                this.extra.set(0, itemstack);
            }

            if (itemstack != net.minecraft.world.item.ItemStack.b && this.N_() > 0 && itemstack.I() > this.N_()) { //idk probably checks if the itemstack is of max size
                itemstack.e(this.N_());
            }

        }

        @Override
        public int N_() {  //getMaxStackSize
            return this.maxStack;
        }

        public void setMaxStackSize(int size) {
            this.maxStack = size;
        }

        public void e() { //????
        }

        public boolean a(EntityHuman entityhuman) { //?? im guessing gets the inventory holder
            return true;
        }

        public List<net.minecraft.world.item.ItemStack> getContents() {
            List<net.minecraft.world.item.ItemStack> stacks = new ArrayList<>();
            stacks.addAll(this.contents);
            stacks.addAll(this.armor);
            stacks.addAll(this.extra);
            stacks.add(placeholder);
            stacks.add(placeholder);
            stacks.add(placeholder);
            stacks.add(placeholder);
            return stacks;
        }

        public void onOpen(CraftHumanEntity who) {
            this.viewers.add(who);
        }

        public void onClose(CraftHumanEntity who) {
            this.viewers.remove(who);
        }

        public List<HumanEntity> getViewers() {
            return this.viewers;
        }

        public InventoryHolder getOwner() { //always null
            return null;
        }

        public boolean b(int i, net.minecraft.world.item.ItemStack itemstack) { //idk
            return true;
        }

        public void b_(EntityHuman entityHuman) { //idk
        }

        public void c_(EntityHuman entityHuman) { //idk
        }

        public void a() { //clear
            this.contents.clear();
            this.armor.clear();
            this.extra.clear();
        }

        public Location getLocation() {
            return null;
        }

        public boolean c() { //isEmpty
            for (NonNullList<net.minecraft.world.item.ItemStack> list : this.all) {
                for (net.minecraft.world.item.ItemStack stack : list) {
                    if (!stack.b())
                        return false;
                }
            }
            return true;
        }
    }
}
