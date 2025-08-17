package dk.nether.asmpaddons.listeners;

import dk.nether.asmpaddons.AsmpAddons;
import dk.nether.asmpaddons.ModConfig;
import dk.nether.asmpaddons.core.ServerValidator;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayNetworkHandler;

public class ServerConnectionListener {
    public ServerConnectionListener() {
        ClientPlayConnectionEvents.JOIN.register(this::onJoinServer);
        ClientPlayConnectionEvents.DISCONNECT.register(this::onDisconnect);
    }

    private void onJoinServer(ClientPlayNetworkHandler handler, PacketSender sender, MinecraftClient client) {
        String serverIp = AsmpAddons.getConfig().serverIp;
        ServerInfo serverInfo = client.getCurrentServerEntry();

        if (serverInfo == null)
            return;

        boolean isModServer = client.getCurrentServerEntry().address.equals(serverIp);
        AsmpAddons.getState().setOnServer(isModServer);

        if (isModServer) {
            ServerValidator.getServerData();
        }
    }

    private void onDisconnect(ClientPlayNetworkHandler handler, MinecraftClient client) {
        AsmpAddons.getState().setOnServer(false);
    }
}
