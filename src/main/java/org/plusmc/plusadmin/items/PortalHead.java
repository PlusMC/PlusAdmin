package org.plusmc.plusadmin.items;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.plusmc.plusadmin.PlusAdmin;
import org.plusmc.plusadmin.Utils.BungeeUtil;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PortalHead implements PlusItem {
    private static List<Block> TICKING_BLOCKS = new ArrayList<>();
    private static HashMap<Player, Long> LAST_USE = new HashMap<>();
    private static BukkitTask TASK = null;
    private static final NamespacedKey PORTAL_KEY = new NamespacedKey(PlusAdmin.getInstance(), "portal_head");
    private static long TICK = 0;

    @Override
    public String getID() {
        return "portal_head";
    }

    @Override
    public String getName() {
        return "Rename To Server Name";
    }

    @Override
    public String[] getLore() {
        return new String[] { "Sends You To A Different Server" };
    }

    @Override
    public Material getMaterial() {
        return Material.PLAYER_HEAD;
    }


    @Override
    public void load() {
        LAST_USE = new HashMap<>();
        TICKING_BLOCKS = new ArrayList<>();
        loadBlocks();
        TICK = 0;
        TASK = Bukkit.getScheduler().runTaskTimer(PlusAdmin.getInstance(), PortalHead::tick, 0, 1);
    }

    @Override
    public void unload() {
        save();
        TICKING_BLOCKS.clear();
        LAST_USE.clear();
        if(TASK != null) TASK.cancel();
    }

    public void loadBlocks() {
        File blocks = new File(PlusAdmin.getInstance().getDataFolder() + "/ticking_blocks.json");
        FileInputStream fis;
        try {
            if(!blocks.exists()) {
                blocks.createNewFile();
                return;
            }
            fis = new FileInputStream(blocks);
            String json = new String(fis.readAllBytes());
            JsonArray array = new Gson().fromJson(json, JsonArray.class);
            for(int i = 0; i < array.size(); i++) {
                JsonArray loc = array.get(i).getAsJsonArray();
                Location location = new Location(Bukkit.getWorld(loc.get(3).getAsString()), loc.get(0).getAsDouble(), loc.get(1).getAsDouble(), loc.get(2).getAsDouble());
                Block block = location.getBlock();
                if(block.getState() instanceof TileState) {
                    TICKING_BLOCKS.add(block);
                }
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save(){
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(PlusAdmin.getInstance().getDataFolder() + "/ticking_blocks.json");
            JsonArray array = new JsonArray();
            for(Block block : TICKING_BLOCKS) {
                JsonArray loc = new JsonArray();
                loc.add(block.getX());
                loc.add(block.getY());
                loc.add(block.getZ());
                loc.add(block.getWorld().getName());
                array.add(loc);
            }
            fos.write(array.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInteractAsBlock(PlayerInteractEvent e) {
        if(!TICKING_BLOCKS.contains(e.getClickedBlock())) TICKING_BLOCKS.add(e.getClickedBlock());
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent e) {
        if(e.getItemInHand().getItemMeta() == null) return;

        TileState state = (TileState) e.getBlockPlaced().getState();
        state.getPersistentDataContainer().set(CUSTOM_ITEM_KEY, PersistentDataType.STRING, this.getID());
        state.getPersistentDataContainer().set(PORTAL_KEY, PersistentDataType.STRING, e.getItemInHand().getItemMeta().getDisplayName());
        state.update();
        World world = e.getBlockPlaced().getWorld();
        ArmorStand stand = world.spawn(e.getBlockPlaced().getLocation().add(0.5, 0.5, 0.5), ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setCustomName("To " + e.getItemInHand().getItemMeta().getDisplayName());
        stand.setCustomNameVisible(true);
        world.strikeLightningEffect(e.getBlockPlaced().getLocation().add(0.5,0,0.5));
        world.playSound(e.getBlockPlaced().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
        TICKING_BLOCKS.add(e.getBlockPlaced());
    }

    private static void tick() {
        for (Block block : TICKING_BLOCKS) {
            if(!block.getChunk().isLoaded()) continue;
            if(!(block.getState() instanceof TileState)) continue;
            TileState state = (TileState) block.getState();
            String name = state.getPersistentDataContainer().get(PORTAL_KEY, PersistentDataType.STRING);
            if(name == null) continue;

            block.getWorld().getNearbyEntities(block.getLocation(), 2, 2, 2).forEach(entity -> {
                if(entity instanceof Player) {
                    Player p = (Player) entity;
                    if(!(LAST_USE.containsKey(p) && System.currentTimeMillis() - LAST_USE.get(p) < 5000)) {
                        LAST_USE.put(p, System.currentTimeMillis());
                        BungeeUtil.connectServer(p, name);
                    }
                }
            });
            helix(block.getLocation(), TICK);
            TICK++;
        }
    }

    private static void helix(Location loc, long tick) {
        if(loc.getWorld() == null) return;
        loc.add(0.5, 0, 0.5);
        double x = Math.cos(Math.toRadians(tick)) * 1.5;
        double z = Math.sin(Math.toRadians(tick)) * 1.5;
        loc.add(x, (tick % 3) + 0.25, z);
        loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1);
    }

}
