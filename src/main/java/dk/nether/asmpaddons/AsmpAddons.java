package dk.nether.asmpaddons;

import dk.nether.asmpaddons.core.ModState;
import dk.nether.asmpaddons.utils.Sender;
import dk.nether.asmpaddons.core.ServerValidator;
import dk.nether.asmpaddons.core.VersionManagement;
import dk.nether.asmpaddons.data.ShopDataHolder;
import dk.nether.asmpaddons.data.ShopDataManager;
import dk.nether.asmpaddons.data.ShopException;
import dk.nether.asmpaddons.data.WaystoneManager;
import dk.nether.asmpaddons.listeners.ServerConnectionListener;
import dk.nether.asmpaddons.utils.Utils;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.SignBlock;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.block.entity.SignText;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AsmpAddons implements ModInitializer {

    public static final String VERSION = "1.1.0";
    public static final String VERSION_URL = "https://kreiseljustus.com/asmp_version.txt";

    public static ModConfig s_Config;
    public static PlayerEntity s_Player;

    private static AsmpAddons instance;

    private ModConfig config;
    private ModState state;
    private ServerConnectionListener serverConnectionListener;
    private VersionManagement versionManagement;


    int tickInServer = 0;

    boolean checkedVersionOnStartup = false;

    ChunkPos lastChunkPosition = null;

    public AsmpAddons() {
        instance = this;
    }

    @Override
    public void onInitialize() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
        this.config = AutoConfig.getConfigHolder(ModConfig.class).getConfig();

        this.state = new ModState();
        this.versionManagement = new VersionManagement();

        this.registerListeners();
        this.versionManagement.start();


        Thread fetcherThread = new Thread(() -> {
            while (true) {
                try {
                    if(!config.enable) Thread.sleep(config.fetcherThreadInterval);
                    ServerValidator.getServerData();
                } catch (Exception e) {
                    Utils.debug("This will crash minecraft");
                }

                try {
                    Thread.sleep(config.fetcherThreadInterval);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        });

        fetcherThread.setDaemon(true);
        fetcherThread.start();
    }

    private void registerListeners() {
        this.serverConnectionListener = new ServerConnectionListener();

        ClientTickEvents.END_CLIENT_TICK.register(this::onClientTick);
        ClientTickEvents.END_CLIENT_TICK.register(WaystoneManager::waystoneTick);
    }




    public void onClientTick(MinecraftClient client) {
        if(!state.isActive()) return;
        if(client.player == null) return;

        s_Player = client.player;

        if(!checkedVersionOnStartup) {
            VersionManagement.checkAndWarnVersion(client.player);
            checkedVersionOnStartup = true;
        }

        if(config.ticksBetweenSends < 400) config.ticksBetweenSends = 600;

        ChunkPos currentChunkPosition = new ChunkPos(s_Player.getBlockPos());

        if(lastChunkPosition == null || !lastChunkPosition.equals(currentChunkPosition)) {
            onEnterNewChunk(currentChunkPosition);
            lastChunkPosition = currentChunkPosition;
        }

        // We should send our data because our timer is done
        // TODO: Re-enable this (disabled for safety while testing)
        /*
        if(tickInServer % config.ticksBetweenSends == 0) {
            if(!VersionManagement.s_UsingLatestVersion) {Utils.debug("Discarding- not up-to date!"); tickInServer++; return;}
            Utils.debug("Attempting to send cached shops");

            Sender.sendCachedData();
            ShopDataManager.s_CachedShops.clear();
            WaystoneManager.s_CachedWaystones.clear();
        }*/

        tickInServer++;
    }

    public static ModConfig getConfig() {
        return instance.config;
    }

    public static ModState getState() {
        return instance.state;
    }

    public void onEnterNewChunk(ChunkPos currentChunk) {
        Utils.debug("Entered new chunk");

        if(config.trackShops) {
            handleShopDetection(currentChunk);
        }
    }

    private void handleShopDetection(ChunkPos currentChunk) {
        World world = s_Player.getWorld();

        Chunk chunk = world.getChunk(currentChunk.getStartPos());

        List<ShopDataHolder> foundShops = new ArrayList<>();

        for (BlockPos pos : chunk.getBlockEntityPositions()) {
            BlockEntity entity = world.getBlockEntity(pos);
            Utils.debug("Entity: " + entity.getType().getRegistryEntry());
            Utils.debug("EntityPos: " + entity.getPos());

            if (!(entity instanceof SignBlockEntity)) {
                Utils.debug("No SignBlockEntity here");
                continue;
            }

            SignBlockEntity sign = (SignBlockEntity) entity;
            BlockState blockState = world.getBlockState(pos);

            Utils.debug("Block at pos: " + pos + " is " + blockState.getBlock().getTranslationKey());

            if (!(blockState.getBlock() instanceof SignBlock || blockState.getBlock() instanceof WallSignBlock)) {
                Utils.debug("No SignBlock here");
                continue;
            }

            SignText text = sign.getFrontText();

            String[] lines = Arrays.stream(text.getMessages(false)).map(Text::getString).toArray(String[]::new);

            if (lines.length != 4) continue;

            String owner = lines[0];
            if(owner.isEmpty()) continue;
            String sellBuyOOS = lines[1];
            if(sellBuyOOS.isEmpty()) continue;

            if (!(sellBuyOOS.contains("Selling") || sellBuyOOS.contains("Buying") || sellBuyOOS.contains("Out of Stock"))) {
                Utils.debug("not selling, buying, oos");
                continue;
            }

            String item = lines[2];
            if(item.isEmpty()) continue;
            String price = lines[3];
            if(price.isEmpty()) continue;

            //Utils.debug(owner + " is " + sellBuyOOS + " " + item + " for " + price);

            int[] position = {
                    pos.getX(), pos.getY(), pos.getZ()
            };

            int action = 0;
            if (sellBuyOOS.contains("Selling")) action = 1;
            else if (sellBuyOOS.contains("Out of Stock")) action = 2;

            Matcher matcher = Pattern.compile("(Selling|Buying)\\s(\\d+)").matcher(sellBuyOOS);

            int amount = 0;
            try {
                amount = matcher.find() ? Integer.parseInt(matcher.group(2)) : 0;
            } catch(Exception e) {
                Utils.debug(e.getMessage());
            }

            int dimension = switch (world.getDimensionEntry().toString()) {
                case "minecraft:the_nether" -> 1;
                case "minecraft:the_end" -> 2;
                default -> 0;
            };

            if(!price.contains(" each")) continue;

            //Utils.debug("Dimension is " + dimension);
            ShopDataHolder shop = null;
            try {
                shop = new ShopDataHolder(owner, position, Float.parseFloat(price.substring(1).replace(" each", "").replace(",", "")), item, action, amount, dimension);
            } catch (ShopException e) {
                throw new RuntimeException(e);
            }

            ShopDataManager.addShop(shop);
            foundShops.add(shop);
        }

        List<ShopDataHolder> shops = ServerValidator.getExpectedShopsInChunk(chunk.getPos().x, chunk.getPos().z);

        for(ShopDataHolder expectedShop : shops) {
            if(foundShops.contains(expectedShop)) continue;

            //Send update to server
            // TODO: Re-enable this (disabled for safety while testing)
            // Sender.sendDeleteRequest(expectedShop);
        }
    }
}
