package org.plusmc.plusadmin.items;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.plusmc.pluslib.managers.PlusItemManager;
import org.plusmc.pluslib.plus.PlusItem;
import org.plusmc.pluslib.plus.Tickable;

import java.util.HashMap;
import java.util.Map;

public class GravityGun implements PlusItem, Tickable {
    HashMap<Player, LivingEntity> HELD = new HashMap<>();
    HashMap<Player, Long> LAST_USE = new HashMap<>();

    @Override
    public void load() {
        HELD = new HashMap<>();
        LAST_USE = new HashMap<>();
    }

    @Override
    public void unload() {
        HELD.clear();
        LAST_USE.clear();
    }

    @Override
    public String getID() {
        return "gravity_gun";
    }

    @Override
    public String getName() {
        return "Gravity Gun";
    }

    @Override
    public String[] getLore() {
        return new String[]{"Right Click Entity To Grab/Drop", "Left Click To Shoot"};
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_HOE;
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if (!HELD.containsKey(p)) return;
        if (LAST_USE.containsKey(p) && System.currentTimeMillis() - LAST_USE.get(p) < 1000) return;
        e.setCancelled(true);

        HELD.remove(p);
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: Entity Has Been Dropped"));
    }


    @Override
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if (!(e.getRightClicked() instanceof LivingEntity ee)) return;
        if (LAST_USE.containsKey(p) && System.currentTimeMillis() - LAST_USE.get(p) < 1000) return;

        if (!HELD.containsKey(p)) {
            HELD.put(p, ee);
            LAST_USE.put(p, System.currentTimeMillis());
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: You Are Holding " + ee.getName()));
        } else {
            LAST_USE.put(p, System.currentTimeMillis());
            HELD.remove(p);
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: Entity Has Been Dropped"));
        }
    }

    @Override
    public void onDamageEntity(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Player p)) return;
        if (HELD.containsKey(p)) {
            LivingEntity ee = HELD.get(p);
            if (ee != e.getEntity()) return;
            e.setCancelled(true);

            HELD.remove(p);
            Vector to = p.getEyeLocation().getDirection().multiply(3);
            ee.setVelocity(to);
        } else {
            if (!(e.getEntity() instanceof LivingEntity ee)) return;
            e.setCancelled(true);

            Vector to = p.getEyeLocation().getDirection().multiply(3);
            ee.setVelocity(to);
        }
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: Entity Has Been Thrown"));
    }

    @Override
    public void tick(long tick) {
        for (Map.Entry<Player, LivingEntity> entries : HELD.entrySet()) {
            Player p = entries.getKey();
            LivingEntity e = entries.getValue();
            if (e == null || !e.isValid()) {
                HELD.remove(p);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: Entity Has Died"));
                continue;
            }
            e.setFallDistance(0);

            ItemStack stack = p.getInventory().getItemInMainHand();
            if (PlusItemManager.hasCustomItem(stack, this.getID())) {
                Vector to = p.getEyeLocation().getDirection().multiply(2).add(p.getEyeLocation().toVector());
                Vector entity = e.getLocation().toVector();
                Vector dir = to.subtract(entity);
                dir.multiply(Double.min(p.getLocation().distance(e.getLocation()) / 5, 0.5));
                e.setVelocity(dir);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: You Are Holding " + e.getName()));
            } else {
                HELD.remove(p);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: Entity Has Been Dropped"));
            }
        }
    }
}
