package dk.nether.asmpaddons.utils;

import com.google.gson.Gson;
import dk.nether.asmpaddons.AsmpAddons;
import dk.nether.asmpaddons.ModConfig;
import dk.nether.asmpaddons.core.DataUploadPacket;
import dk.nether.asmpaddons.data.ShopDataHolder;
import dk.nether.asmpaddons.data.ShopDataManager;
import dk.nether.asmpaddons.data.WaystoneDataHolder;
import dk.nether.asmpaddons.data.WaystoneManager;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Sender {
    public static final Logger LOGGER = LoggerFactory.getLogger("AtriocSMP Log");

    static Gson gson = new Gson();

    public static void sendDeleteRequest(WaystoneDataHolder waystone) {
        sendDeleteRequest(null, waystone);
    }

    public static void sendDeleteRequest(ShopDataHolder shop) {
        sendDeleteRequest(shop, null);
    }

    public static void sendDeleteRequest(ShopDataHolder shop, WaystoneDataHolder waystone) {
        if(shop != null && waystone != null) return;

        String dataJson = shop == null ? gson.toJson(waystone) : gson.toJson(shop);

        String requestBody = String.format("{\"type\":\"shop\",\"data\":%s}", dataJson);

        HttpPost post = new HttpPost(AsmpAddons.getConfig().deleteRoute);
        new Thread(() -> {
            try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
                StringEntity postString = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
                post.setEntity(postString);
                post.setHeader("Content-Type", "application/json");

                LOGGER.info("Sending delete request");
                LOGGER.info("Post URL: {}", post.getURI().toString());
                LOGGER.info("Request Body: {}", requestBody);
                LOGGER.info("Post String: {}", postString);

                client.execute(post);
                LOGGER.info(("Delete request sent"));
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
        }).start();
    }

    public static void sendCachedData() {
        List<ShopDataHolder> shops = new ArrayList<>(ShopDataManager.s_CachedShops);
        List<WaystoneDataHolder> waystones = new ArrayList<>(WaystoneManager.s_CachedWaystones);

        if(shops.isEmpty() && waystones.isEmpty()) {
            Utils.debug("No cached data to send");
            return;
        }

        ModConfig config = AsmpAddons.getConfig();
        if(config.postUrl == null || config.postUrl.isEmpty()) return;

        HttpPost post = new HttpPost(config.postUrl);
        new Thread(() -> {
            try(CloseableHttpClient client = HttpClientBuilder.create().build()) {
                DataUploadPacket packet = new DataUploadPacket(shops,waystones);
                StringEntity postString = new StringEntity(gson.toJson(packet), ContentType.APPLICATION_JSON);

                post.setEntity(postString);
                post.setHeader("Content-Type", "application/json");

                LOGGER.info("Post URL: {}", post.getURI().toString());
                LOGGER.info("Request Body: {}", gson.toJson(packet));
                LOGGER.info("Post String: {}", postString);

                client.execute(post);
                LOGGER.info("Shop & waystone data request sent.");
            } catch (Exception e) {
                e.printStackTrace();
                LOGGER.error(e.getMessage());
            }
        }).start();
    }
}
