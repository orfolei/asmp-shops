package dk.nether.asmpaddons.core;

import dk.nether.asmpaddons.data.ShopDataHolder;
import dk.nether.asmpaddons.data.WaystoneDataHolder;

import java.util.List;

public class DataUploadPacket {
    public List<ShopDataHolder> shops;
    public List<WaystoneDataHolder> waystones;

    public DataUploadPacket(List<ShopDataHolder> shops, List<WaystoneDataHolder> waystones) {
        this.shops = shops;
        this.waystones = waystones;
    }
}
