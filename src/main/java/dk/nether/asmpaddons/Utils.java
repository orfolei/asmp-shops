package dk.nether.asmpaddons;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.text.Text;


public class Utils {
    public static boolean onASMP() {
        MinecraftClient client = MinecraftClient.getInstance();

        ServerInfo server = client.getCurrentServerEntry();
        if(server == null) return false;
        return server.address.equals("asmp.cc");
    }

    public static void debug(String message) {
        if(!AsmpAddons.s_Config.enableDebugMode || !AsmpAddons.s_Config.enable) return;
        AsmpAddons.s_Player.sendMessage(Text.of(message), false);
    }
}
