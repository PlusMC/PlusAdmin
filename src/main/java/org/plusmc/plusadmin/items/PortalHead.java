package org.plusmc.plusadmin.items;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.TileState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.persistence.PersistentDataType;
import org.plusmc.plusadmin.PlusAdmin;
import org.plusmc.pluslib.bukkit.managed.PlusItem;
import org.plusmc.pluslib.bukkit.managed.Tickable;
import org.plusmc.pluslib.bukkit.managing.PlusItemManager;
import org.plusmc.pluslib.bukkit.util.BungeeUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.*;

public class PortalHead implements PlusItem, Tickable {
    private static List<Portal> TICKING_PORTALS = new ArrayList<>();
    private static HashMap<Player, Long> LAST_USE = new HashMap<>();

    private static void updateStand(ArmorStand stand, Block block, String name) {
        BungeeUtil.updateServer(name);
        BungeeUtil.PlayerList playerList = BungeeUtil.getPlayerList(name);
        BungeeUtil.ServerInfo serverInfo = BungeeUtil.getInfo(name);
        if (!serverInfo.online()) {
            block.getWorld().spawnParticle(Particle.BLOCK_MARKER, block.getLocation().add(0.5, 2, 0.5), 1, Material.BARRIER.createBlockData());
            stand.setCustomName("§cOffline");
        } else {
            block.getWorld().spawnParticle(Particle.BLOCK_MARKER, block.getLocation().add(0.5, 2, 0.5), 1, Material.GREEN_STAINED_GLASS.createBlockData());
            stand.setCustomName("§aOnline: §6§l" + playerList.playerCount() + " Players");
        }
    }

    private static void remove(Portal portal) {
        portal.armorStands().forEach(uuid -> {
            Entity stand = Bukkit.getEntity(uuid);
            if (stand instanceof ArmorStand)
                stand.remove();
        });
        TICKING_PORTALS.remove(portal);
    }

    private static void helix(Location loc, long tick) {
        if (loc.getWorld() == null) return;
        loc.add(0.5, 0, 0.5);
        double x = Math.cos(Math.toRadians(tick)) * 1.5;
        double z = Math.sin(Math.toRadians(tick)) * 1.5;
        loc.add(x, (tick % 3) + 0.25, z);
        loc.getWorld().spawnParticle(Particle.VILLAGER_HAPPY, loc, 1);
    }

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
        return new String[]{"Sends You To A Different Server"};
    }

    @Override
    public Material getMaterial() {
        return Material.PLAYER_HEAD;
    }

    @Override
    public void load() {
        LAST_USE = new HashMap<>();
        TICKING_PORTALS = new ArrayList<>();
        loadBlocks();
    }

    @Override
    public void unload() {
        save();
        TICKING_PORTALS.clear();
        LAST_USE.clear();
    }

    public void loadBlocks() {
        File blocks = new File(PlusAdmin.getInstance().getDataFolder() + "/ticking_blocks.json");
        FileInputStream fis;
        try {
            if (!blocks.exists()) {
                blocks.createNewFile();
                return;
            }
            fis = new FileInputStream(blocks);
            String json = new String(fis.readAllBytes());
            JsonArray array = new Gson().fromJson(json, JsonArray.class);
            for (int i = 0; i < array.size(); i++) {
                JsonArray data = array.get(i).getAsJsonArray();
                Location location = new Location(Bukkit.getWorld(data.get(3).getAsString()), data.get(0).getAsDouble(), data.get(1).getAsDouble(), data.get(2).getAsDouble());
                Block block = location.getBlock();
                List<UUID> uuids = new ArrayList<>();
                data.get(5).getAsJsonArray().forEach(uuid -> uuids.add(UUID.fromString(uuid.getAsString())));
                if (block.getState() instanceof TileState) {
                    TICKING_PORTALS.add(new Portal(data.get(4).getAsString(), uuids, block));
                }
            }
            fis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void save() {
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(PlusAdmin.getInstance().getDataFolder() + "/ticking_blocks.json");
            JsonArray array = new JsonArray();
            for (Portal portal : TICKING_PORTALS) {
                JsonArray data = new JsonArray();
                Block block = portal.block();
                data.add(block.getX());
                data.add(block.getY());
                data.add(block.getZ());
                data.add(block.getWorld().getName());
                data.add(portal.server());
                JsonArray uuids = new JsonArray();
                portal.armorStands().forEach(uuid -> uuids.add(uuid.toString()));
                data.add(uuids);
                array.add(data);
            }
            fos.write(array.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBlockPlace(BlockPlaceEvent e) {
        if (e.getItemInHand().getItemMeta() == null) return;

        TileState state = (TileState) e.getBlockPlaced().getState();
        state.getPersistentDataContainer().set(PlusItemManager.itemKey, PersistentDataType.STRING, this.getID());
        state.update();
        World world = e.getBlockPlaced().getWorld();
        ArmorStand stand = world.spawn(e.getBlockPlaced().getLocation().add(0.5, 1.5, 0.5), ArmorStand.class);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setSmall(true);
        stand.setCustomName("§6§l" + e.getItemInHand().getItemMeta().getDisplayName());
        stand.setCustomNameVisible(true);

        ArmorStand stand1 = world.spawn(e.getBlockPlaced().getLocation().add(0.5, 1.25, 0.5), ArmorStand.class);
        stand1.setVisible(false);
        stand1.setGravity(false);
        stand1.setSmall(true);
        stand1.setCustomName("Loading...");
        stand1.setCustomNameVisible(true);

        world.strikeLightningEffect(e.getBlockPlaced().getLocation().add(0.5, 0, 0.5));
        world.playSound(e.getBlockPlaced().getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1, 1);
        TICKING_PORTALS.add(new Portal(e.getItemInHand().getItemMeta().getDisplayName(), List.of(stand.getUniqueId(), stand1.getUniqueId()), e.getBlockPlaced()));
    }

    @Override
    public void tick(long tick) {
        for (Iterator<Portal> it = TICKING_PORTALS.iterator(); it.hasNext(); ) {
            Portal portal = it.next();
            Block block = portal.block();
            if (!block.getChunk().isEntitiesLoaded()) continue;
            if (!(block.getState() instanceof TileState state)) {
                it.remove();
                remove(portal);
                continue;
            }

            if (!state.getPersistentDataContainer().has(PlusItemManager.itemKey, PersistentDataType.STRING)) {
                it.remove();
                remove(portal);
                continue;
            }
            String name = portal.server();
            List<UUID> uuids = portal.armorStands();
            UUID id = uuids.get(1);
            if (id == null) continue;

            Entity e = Bukkit.getEntity(id);
            if (!(e instanceof ArmorStand stand)) {
                it.remove();
                remove(portal);
                continue;
            }

            block.getWorld().getNearbyEntities(block.getLocation(), 2, 2, 2).forEach(entity -> {
                if (entity instanceof Player p) {
                    if (!(LAST_USE.containsKey(p) && tick - LAST_USE.get(p) < 100)) {
                        LAST_USE.put(p, tick);
                        BungeeUtil.connectServer(p, name);
                    }
                }
            });
            helix(block.getLocation(), tick);
            if (tick % 75 == 0)
                updateStand(stand, block, name);
        }
    }

    private record Portal(String server, List<UUID> armorStands, Block block) {
    }
}
