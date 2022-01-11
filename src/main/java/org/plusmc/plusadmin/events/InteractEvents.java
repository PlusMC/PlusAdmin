package org.plusmc.plusadmin.events;

import org.plusmc.plusadmin.items.PlusItem;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class InteractEvents implements Listener {

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent e) {
        ItemStack stack = e.getItem();
        if (stack == null) return;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        try {
            String id = meta.getPersistentDataContainer().get(PlusItem.CUSTOM_ITEM_KEY, PersistentDataType.STRING);
            PlusItem item = PlusItem.getCustomItem(id);
            if (item == null) return;
            item.onInteractBlock(e);
        } catch (Exception ex) {
            //ignore
        }

        Block block = e.getClickedBlock();
        if (block == null) return;
        if(!(block.getState() instanceof TileState state)) return;
        try {
            String id = state.getPersistentDataContainer().get(PlusItem.CUSTOM_ITEM_KEY, PersistentDataType.STRING);
            PlusItem item = PlusItem.getCustomItem(id);
            if (item == null) return;
            item.onInteractAsBlock(e);
        } catch (Exception ex) {
            //ignore
        }
    }

    @EventHandler
    public void onEntityInteract(PlayerInteractEntityEvent e) {
        ItemStack stack = e.getPlayer().getInventory().getItemInMainHand();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        try {
            String id = meta.getPersistentDataContainer().get(PlusItem.CUSTOM_ITEM_KEY, PersistentDataType.STRING);
            PlusItem item = PlusItem.getCustomItem(id);
            if (item == null) return;
            item.onInteractEntity(e);
        } catch (Exception ex) {
            //ignore
        }

    }

    @EventHandler
    public void onPlayerHit(EntityDamageByEntityEvent e) {
        if(!(e.getDamager() instanceof Player p)) return;
        ItemStack stack = p.getInventory().getItemInMainHand();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        try {
            String id = meta.getPersistentDataContainer().get(PlusItem.CUSTOM_ITEM_KEY, PersistentDataType.STRING);
            PlusItem item = PlusItem.getCustomItem(id);
            if (item == null) return;
            item.onDamageEntity(e);
        } catch (Exception ex) {
            //ignore
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent e) {
        ItemStack stack = e.getItemInHand();
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        try {
            String id = meta.getPersistentDataContainer().get(PlusItem.CUSTOM_ITEM_KEY, PersistentDataType.STRING);
            PlusItem item = PlusItem.getCustomItem(id);
            if (item == null) return;
            item.onBlockPlace(e);
        } catch (Exception ex) {
            //ignore
        }
    }

}
