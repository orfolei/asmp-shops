package dk.nether.asmpaddons.utils;

import dk.nether.asmpaddons.AsmpAddons;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;


public class Utils {
    public static void debug(String message) {
        if(!AsmpAddons.getConfig().enableDebugMode || !AsmpAddons.getConfig().enable) return;
        AsmpAddons.s_Player.sendMessage(Text.of(message), false);
    }
}
