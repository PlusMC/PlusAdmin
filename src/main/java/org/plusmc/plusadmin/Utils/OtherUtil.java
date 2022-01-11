package org.plusmc.plusadmin.Utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class OtherUtil {

    public static <T> List<T> getKeys(Map<T, T> map, T value ) {
        List<T> keys = new ArrayList<>();
        for (Map.Entry<T, T> entry : map.entrySet()) {
            if (entry.getValue().equals(value)) {
                keys.add(entry.getKey());
            }
        }
        return keys;
    }

    public static List<String> allPlayers() {
        List<String> players = new ArrayList<>();
        Bukkit.getOnlinePlayers().forEach(player -> players.add(player.getName()));
        return players;
    }
}
