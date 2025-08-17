package dk.nether.asmpaddons;

import dk.nether.asmpaddons.data.ShopDataHolder;

import java.util.List;

public class DataUploadPacket {
    public List<ShopDataHolder> shops;
    public List<WaystoneDataHolder> waystones;

    public DataUploadPacket(List<ShopDataHolder> shops, List<WaystoneDataHolder> waystones) {
        this.shops = shops;
        this.waystones = waystones;
    }
}
