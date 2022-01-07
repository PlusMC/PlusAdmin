package com.playplusmc.plusadmin.items;

import com.playplusmc.plusadmin.PlusAdmin;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public interface PlusItem {
    NamespacedKey CUSTOM_ITEM_KEY = new NamespacedKey(PlusAdmin.getInstance(), "custom_item");
    List<PlusItem> CUSTOM_ITEMS = Arrays.asList(
            new SlayButton(),
            new GravityGun(),
            new PortalHead()
    );

    String getID();
    String getName();
    String[] getLore();
    Material getMaterial();

    default void unload() {
        //ignore
    }

    default void load() {
        //ignore
    }

    default void onDamageEntity(EntityDamageByEntityEvent e) {
         //ignore
    }
    default void onInteractEntity(PlayerInteractEntityEvent e) {
         //ignore
    }
    default void onInteractBlock(PlayerInteractEvent e) {
         //ignore
    }
    default void onInteractAsBlock(PlayerInteractEvent e) {
        //ignore
    }
    default void onBlockPlace(BlockPlaceEvent e) {
        e.setCancelled(true);
    }

    default ItemStack getItem() {
        ItemStack stack = new ItemStack(getMaterial());
        ItemMeta meta = stack.getItemMeta();
        assert meta != null;
        meta.setDisplayName(this.getName());
        meta.setLore(Arrays.asList(this.getLore()));
        meta.getPersistentDataContainer().set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, this.getID());
        stack.setItemMeta(meta);
        return stack;
    }

    static void loadAll() {
        CUSTOM_ITEMS.forEach(PlusItem::load);
    }


    static void unloadAll() {
        for (PlusItem item : CUSTOM_ITEMS) {
            item.unload();
        }
    }

    static PlusItem getCustomItem(String id) {
        for (PlusItem item : CUSTOM_ITEMS) {
            if (item.getID().equals(id))
                return item;
        }
        return null;
    }

    static boolean hasCustomItem(ItemStack stack, String id) {
        if(stack == null) return false;
        if(stack.getItemMeta() == null) return false;
        if(!stack.getItemMeta().getPersistentDataContainer().has(CUSTOM_ITEM_KEY, PersistentDataType.STRING)) return false;
        return Objects.equals(stack.getItemMeta().getPersistentDataContainer().get(CUSTOM_ITEM_KEY, PersistentDataType.STRING), id);
     }

    static PlusItem getCustomItem(ItemStack stack) {
        if(stack == null) return null;
        if(stack.getItemMeta() == null) return null;
        if(!stack.getItemMeta().getPersistentDataContainer().has(CUSTOM_ITEM_KEY, PersistentDataType.STRING)) return null;
        return getCustomItem(stack.getItemMeta().getPersistentDataContainer().get(CUSTOM_ITEM_KEY, PersistentDataType.STRING));
    }
}
