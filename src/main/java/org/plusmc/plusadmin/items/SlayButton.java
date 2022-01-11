package org.plusmc.plusadmin.items;

import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.player.PlayerInteractEntityEvent;

public class SlayButton implements PlusItem {
    @Override
    public String getID() {
        return "slay_button";
    }

    @Override
    public String getName() {
        return "Slay Button";
    }

    @Override
    public String[] getLore() {
        return new String[]{"SLAY!!!!"};
    }

    @Override
    public Material getMaterial() {
        return Material.STONE_BUTTON;
    }

    @Override
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        if(e.getRightClicked() instanceof LivingEntity entity) {
            entity.setHealth(0);
        } else {
            e.getRightClicked().remove();
        }
    }


}
