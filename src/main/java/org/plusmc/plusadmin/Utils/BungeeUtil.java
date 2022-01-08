package org.plusmc.plusadmin.Utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.plusmc.plusadmin.PlusAdmin;
import org.bukkit.entity.Player;

public class BungeeUtil {
    public static void connectServer(Player player, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("Connect");
        out.writeUTF(server);

        player.sendPluginMessage(PlusAdmin.getInstance(), "BungeeCord", out.toByteArray());
    }

    public static void connectServer(String name, String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("ConnectOther");
        out.writeUTF(name);
        out.writeUTF(server);

        PlusAdmin.getInstance().getServer().sendPluginMessage(PlusAdmin.getInstance(), "BungeeCord", out.toByteArray());
    }


}
