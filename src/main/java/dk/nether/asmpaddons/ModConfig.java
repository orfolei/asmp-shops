package dk.nether.asmpaddons;

import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.ConfigData;
import me.shedaniel.autoconfig.annotation.Config;
import me.shedaniel.autoconfig.annotation.ConfigEntry;
import me.shedaniel.autoconfig.serializer.GsonConfigSerializer;

@Config(name = "asmpshopget")
public class ModConfig implements ConfigData {
    public ModConfig() {
        AutoConfig.register(ModConfig.class, GsonConfigSerializer::new);
    }

    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.Tooltip
    public boolean enable = true;
    @ConfigEntry.Category("General")
    public String serverIp = "asmp.cc";
    @ConfigEntry.Category("Tracking")
    @ConfigEntry.Gui.Tooltip
    public boolean trackShops = true;
    @ConfigEntry.Category("Tracking")
    @ConfigEntry.Gui.Tooltip
    public boolean trackWaystones = true;
    @ConfigEntry.Category("General")
    public String postUrl = "https://kreiseljustus.com/asmp/post";
    @ConfigEntry.Category("General")
    @ConfigEntry.Gui.Tooltip
    public int ticksBetweenSends = 600;
    @ConfigEntry.Category("General")
    public int fetcherThreadInterval = 15 * 60 * 1000;

    @ConfigEntry.Category("Dev")
    public boolean enableDebugMode = false;
    @ConfigEntry.Category("Dev")
    public boolean allowOnAllServers = false;
    @ConfigEntry.Category("Dev")
    public String shopRoute = "https://kreiseljustus.com/asmp/api/shops";
    @ConfigEntry.Category("Dev")
    public String waystoneRoute = "https://kreiseljustus.com/asmp/api/shops";
    @ConfigEntry.Category("Dev")
    public String deleteRoute = "https://kreiseljustus.com/asmp/api/delete";



}
