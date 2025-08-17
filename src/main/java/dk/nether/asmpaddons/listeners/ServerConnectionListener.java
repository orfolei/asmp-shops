package dk.nether.asmpaddons.listeners;

import dk.nether.asmpaddons.AsmpAddons;
import dk.nether.asmpaddons.ModConfig;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class ServerConnectionListener {
    public ServerConnectionListener() {
        ServerPlayConnectionEvents.JOIN.register(this::onJoinServer);
        ServerPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);
    }

    private void onJoinServer(ServerPlayNetworkHandler handler, PacketSender sender, MinecraftServer server) {
        String serverIp = AsmpAddons.getConfig().serverIp;
        boolean isModServer = server.getServerIp().equals(serverIp);

        AsmpAddons.getState().setOnServer(isModServer);
    }

    private void onDisconnect(ServerPlayNetworkHandler handler, MinecraftServer server) {
        AsmpAddons.getState().setOnServer(false);
    }
}
