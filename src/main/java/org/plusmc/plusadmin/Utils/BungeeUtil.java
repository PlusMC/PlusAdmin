package org.plusmc.plusadmin.utils;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import org.plusmc.plusadmin.PlusAdmin;
import org.bukkit.entity.Player;

import org.bukkit.plugin.messaging.PluginMessageListener;

public class BungeeUtil implements PluginMessageListener {
    HashMap<String, ServerInfo> SERVER_INFO = new HashMap<>();
    HashMap<String, PlayerList> PLAYER_LIST = new HashMap<>();


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

    public static void updateServer(String server) {
        ByteArrayDataOutput out = ByteStreams.newDataOutput();
        out.writeUTF("PlayerList");
        out.writeUTF(server);
        PlusAdmin.getInstance().getServer().sendPluginMessage(PlusAdmin.getInstance(), "BungeeCord", out.toByteArray());

        out = ByteStreams.newDataOutput();
        out.writeUTF("ServerIP");
        out.writeUTF(server);
        PlusAdmin.getInstance().getServer().sendPluginMessage(PlusAdmin.getInstance(), "BungeeCord", out.toByteArray());
    }

    public void onPluginMessageReceived(String channel, Player player, byte[] message) {
        ByteArrayDataInput in = ByteStreams.newDataInput(message);
        switch(channel) {
            case "PlayerList" -> handlePlayerList(in);
            case "ServerIP"-> handleServerIP(in);
        };
    }

    private void handlePlayerList(ByteArrayDataInput in) {
        String server = in.readUTF();
        String[] playerList = in.readUTF().split(", ");
        PLAYER_LIST.put(server, new PlayerList(server, playerList, playerList.length));
    }

    private void handleServerIP(ByteArrayDataInput in) {
        String server = in.readUTF();
        String ip = in.readUTF();
        int port = in.readUnsignedShort();
        SERVER_INFO.put(server, new ServerInfo(server, ip, port));
    }

    public static record ServerInfo(String server, String ip, int port) {}
    public static record PlayerList(String server, String[] playerList, int playerCount) {}
}
