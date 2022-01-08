package org.plusmc.plusadmin.items;

import org.plusmc.plusadmin.PlusAdmin;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;

public class GravityGun implements PlusItem {
    BukkitTask TASK;
    HashMap<Player, LivingEntity> HELD = new HashMap<>();
    HashMap<Player, Long> LAST_USE = new HashMap<>();

    @Override
    public void load() {
        TASK = Bukkit.getScheduler().runTaskTimer(PlusAdmin.getInstance(), this::tick, 0, 1);
    }

    @Override
    public void unload() {
        if(TASK != null) TASK.cancel();
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
        return new String[] { "Right Click Entity To Grab/Drop", "Left Click To Shoot" };
    }

    @Override
    public Material getMaterial() {
        return Material.DIAMOND_HOE;
    }

    @Override
    public void onInteractBlock(PlayerInteractEvent e) {
        Player p = e.getPlayer();
        if(!HELD.containsKey(p)) return;
        if(LAST_USE.containsKey(p) && System.currentTimeMillis() - LAST_USE.get(p) < 1000) return;
        e.setCancelled(true);

        HELD.remove(p);
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: Entity Has Been Dropped"));
    }


    @Override
    public void onInteractEntity(PlayerInteractEntityEvent e) {
        Player p = e.getPlayer();
        if(!(e.getRightClicked() instanceof LivingEntity)) return;
        LivingEntity ee = (LivingEntity) e.getRightClicked();
        if(LAST_USE.containsKey(p) && System.currentTimeMillis() - LAST_USE.get(p) < 1000) return;

        if(!HELD.containsKey(p)) {
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
        if(!(e.getDamager() instanceof Player)) return;
        Player p = (Player) e.getDamager();
        if(HELD.containsKey(p)) {
            LivingEntity ee = HELD.get(p);
            if (ee != e.getEntity()) return;
            e.setCancelled(true);

            HELD.remove(p);
            Vector to = p.getEyeLocation().getDirection().multiply(3);
            ee.setVelocity(to);
        } else {
            if(!(e.getEntity() instanceof LivingEntity)) return;
            e.setCancelled(true);

            LivingEntity ee = (LivingEntity) e.getEntity();
            Vector to = p.getEyeLocation().getDirection().multiply(3);
            ee.setVelocity(to);
        }
        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: Entity Has Been Thrown"));
    }

    public void tick() {
        for(Map.Entry<Player, LivingEntity> entries : HELD.entrySet()) {
            Player p = entries.getKey();
            LivingEntity e = entries.getValue();
            if(e == null || !e.isValid()) {
                HELD.remove(p);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: Entity Has Died"));
                continue;
            }
            e.setFallDistance(0);

            ItemStack stack = p.getInventory().getItemInMainHand();
            if(PlusItem.hasCustomItem(stack, this.getID())) {

                Vector to = p.getEyeLocation().getDirection().multiply(2).add(p.getEyeLocation().toVector());
                Vector entity = e.getLocation().toVector();
                Vector dir = to.subtract(entity);
                dir.multiply(1/p.getEyeLocation().distance(e.getLocation()));
                //dir.multiply(p.getLocation().distance(e.getLocation()) * 10);
                e.setVelocity(dir);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: You Are Holding " + e.getName()));
            } else {
                HELD.remove(p);
                p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent("Gravity Gun: Entity Has Been Dropped"));
            }
        }
    }

}
