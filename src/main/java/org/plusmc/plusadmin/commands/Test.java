package org.plusmc.plusadmin.commands;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.PacketPlayOutOpenWindow;
import net.minecraft.server.level.EntityPlayer;
import net.minecraft.world.IInventory;
import net.minecraft.world.entity.player.EntityHuman;
import net.minecraft.world.entity.player.PlayerInventory;
import net.minecraft.world.inventory.Container;
import net.minecraft.world.inventory.Containers;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang.Validate;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftContainer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventory;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftInventoryPlayer;
import org.bukkit.craftbukkit.v1_18_R1.inventory.CraftItemStack;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftChatMessage;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class Test implements PlusCommand{
    @Override
    public String getName() {
        return "test";
    }

    @Override
    public String getPermission() {
        return "plusadmin.test";
    }

    @Override
    public String getUsage() {
        return "/test";
    }

    @Override
    public String getDescription() {
        return "Test command";
    }

    @Override
    public List<String> getCompletions(int page) {
        return null;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {

            if(!(sender instanceof Player p)) return false;
            CraftPlayer craftPlayer = (CraftPlayer) p;
            Field f = CraftHumanEntity.class.getDeclaredField("inventory");
            f.setAccessible(true);
            CraftInventoryPlayer craftInv = (CraftInventoryPlayer) f.get(craftPlayer);
            PlayerInventory inv = craftInv.getInventory();
            Inventory inventory = new CraftInventory(new CustomInv(null, "test", inv.h, inv.i, inv.j));
            craftPlayer.getInventory();
            EntityPlayer player = craftPlayer.getHandle();
            Container container = new CraftContainer(inventory, craftPlayer.getHandle(), craftPlayer.getHandle().nextContainerCounter());
            String title = container.getBukkitView().getTitle();
            player.b.a(new PacketPlayOutOpenWindow(container.j, Containers.e, CraftChatMessage.fromString(title)[0]));
            player.bW = container;
            player.a(container);
            System.out.println("work");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static class CustomInv implements IInventory {
        public NonNullList<ItemStack> contents;
        public NonNullList<ItemStack> armor;
        public NonNullList<ItemStack> extra;
        public List<NonNullList<ItemStack>> all;
        private int maxStack;
        private final List<HumanEntity> viewers;
        private final InventoryHolder owner;


        public CustomInv(InventoryHolder owner, String title, NonNullList<ItemStack> contents, NonNullList<ItemStack> armor, NonNullList<ItemStack> extra) {
            this.maxStack = 64;
            Validate.notNull(title, "Title cannot be null");
            this.contents = contents;
            this.armor = armor;
            this.extra = extra;
            this.all = new ArrayList<>();
            this.all.add(this.contents);
            this.all.add(this.armor);
            this.all.add(this.extra);
            this.viewers = new ArrayList<>();
            this.owner = owner;
        }

        public int b() // getSize {
            return 45;
        }

        public ItemStack a(int i) //getItem {
            if(i < this.contents.size()) {
                return this.contents.get(i);
            } else if(i < this.contents.size() + this.armor.size()) {
                return this.armor.get(i - this.contents.size());
            } else if (i < this.contents.size() + this.armor.size() + this.extra.size()) {
                return this.extra.get(i - this.contents.size() - this.armor.size());
            } else {
                return ItemStack.b;
            }
        }

        public ItemStack a(int i, int j) //??? {
            ItemStack stack = this.a(i); //gets the stack at i
            if (stack == ItemStack.b) { //returns if stack is null
                return stack;
            } else {
                ItemStack result; 
                if (stack.I() <= j) {
                    this.a(i, ItemStack.b);
                    result = stack;
                } else {
                    result = CraftItemStack.copyNMSStack(stack, j);
                    stack.g(j);
                }

                this.e();
                return result;
            }
        }

        public ItemStack b(int i) //???? looks similar to the top one { 
            ItemStack stack = this.a(i);
            if (stack == ItemStack.b) {
                return stack;
            } else {
                ItemStack result;
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

        public void a(int i, ItemStack itemstack) { //set item 
            if(i < this.contents.size()) {
                this.contents.set(i, itemstack);
            } else if(i < this.contents.size() + this.armor.size()) {
                this.armor.set(i - this.contents.size(), itemstack);
            } else if (i < this.contents.size() + this.armor.size() + this.extra.size()) {
                this.extra.set(i - this.contents.size() - this.armor.size(), itemstack);
            }

            if (itemstack != ItemStack.b && this.M_() > 0 && itemstack.I() > this.M_()) { //idk
                itemstack.e(this.M_());
            }

        }

        public int M_() { //max stack size
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

        public List<ItemStack> getContents() { 
            List<ItemStack> stacks = new ArrayList<>();
            stacks.addAll(this.contents);
            stacks.addAll(this.armor);
            stacks.addAll(this.extra);
            stacks.add(ItemStack.b);
            stacks.add(ItemStack.b);
            stacks.add(ItemStack.b);
            stacks.add(ItemStack.b);
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
            return this.owner;
        }

        public boolean b(int i, ItemStack itemstack) { //idk
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
            for(NonNullList<ItemStack> list : this.all) {
                for(ItemStack stack : list) {
                    if(!stack.b()) {
                        return false;
                    }
                }
            }
            return true;
        }
    }
}
