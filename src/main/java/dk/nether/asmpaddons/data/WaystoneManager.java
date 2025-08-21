package dk.nether.asmpaddons.data;

import dk.nether.asmpaddons.AsmpAddons;
import dk.nether.asmpaddons.utils.Utils;
import net.minecraft.block.Block;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class WaystoneManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(WaystoneManager.class.getName());
    public static LinkedList<WaystoneDataHolder> s_CachedWaystones = new LinkedList<>();
    private static LinkedList<String> checkedInventories = new LinkedList<>();

    private static ChunkSectionPos lastChunkSectionPos;
    private static ArrayList<ChunkSectionPos> cachedChunkSections = new ArrayList<>();
    private static Map<ChunkSectionPos, WaystoneDataHolder> nearbyWaystones = new HashMap<>();

    public static void addWaystone(WaystoneDataHolder waystone) {
        if(!s_CachedWaystones.contains(waystone)) {
            s_CachedWaystones.add(waystone);
        }
    }

    public static boolean isWaystoneUI(ScreenHandler handler) {
        int waystoneGuiSlots = 6*9;
        if(handler.slots.size() < waystoneGuiSlots + 36 ) return false; //36 is player inv size
        Utils.debug("The screen has the correct size");

        ItemStack head = handler.getSlot(5).getStack();
        if(!(head.getItem() == Items.PLAYER_HEAD)) return false;

        Utils.debug("The screen has a head in the right position");

        if(!(handler.getSlot(49).getStack().getItem() == Items.SPYGLASS)) return false;
        Utils.debug("The screen has a spyglass in the right slot");

        return true;
    }

    public static String getWaystoneOwner(ScreenHandler handler) {
        ItemStack head = handler.getSlot(5).getStack();
        return head.getCustomName() == null ? "Unknown" : head.getCustomName().getString();
    }

    public static void handleWaystoneDetection(MinecraftClient client) {
        /*if (client.player == null)
            return;

        ChunkSectionPos playerSection = ChunkSectionPos.from(client.player.getBlockPos());

        if (playerSection == lastChunkSectionPos)
            return;

        List<ChunkSectionPos> sections = new ArrayList<>();
        sections.add(playerSection);

        for (int x = -1; x < 1; x += 2)
            for (int y = -1; y < 1; y += 2)
                for (int z = -1; y < -1; y += 2)
                    sections.add(playerSection.add(16 * x, 16 * y, 16 * z));

        for (ChunkSectionPos section : nearbyWaystones.keySet()) {
            if ((sections.contains(section))) {
                sections.remove(section);
            } else {
                nearbyWaystones.remove(section);
            }
        }


        lastChunkSectionPos = playerSection;*/
    }

    public static void waystoneTick(MinecraftClient client) {
        if(!AsmpAddons.getConfig().trackWaystones)
            return;

        handleWaystoneDetection(client);

        if(client.currentScreen instanceof HandledScreen<?> screen) {

            Utils.debug("Theres a screen open");

            if(checkedInventories.contains(screen.getTitle().toString()))
                return;

            ScreenHandler handler = screen.getScreenHandler();

            if(isWaystoneUI(handler)) {
                Utils.debug("The screen is a waystone");
                String owner = getWaystoneOwner(handler);
                String name = screen.getTitle().getString();

                BlockHitResult hit = (client.crosshairTarget instanceof BlockHitResult br) ? br : null;
                BlockPos pos = (hit != null) ? hit.getBlockPos() : BlockPos.ORIGIN;

                Utils.debug("Waystone detected! Owner: " + owner + ", Name: " + name + ", Pos: " + pos);

                int[] position = {pos.getX(), pos.getY(), pos.getZ()};
                addWaystone(new WaystoneDataHolder(owner,name, position));

                checkedInventories.add(screen.getTitle().getString());
            }
        }
    }
}
