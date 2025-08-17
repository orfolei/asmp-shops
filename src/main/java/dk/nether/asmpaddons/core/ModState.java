package dk.nether.asmpaddons.core;

import dk.nether.asmpaddons.AsmpAddons;
import dk.nether.asmpaddons.ModConfig;

public class ModState {
    /**
     * A flag to indicate if the client is connected to the server (AtriocSMP).
     */
    private boolean onServer = false;

    public void setOnServer(boolean onServer) {
        this.onServer = onServer;
    }

    public boolean isOnServer() {
        return onServer;
    }

    public boolean isActive() {
        if (!AsmpAddons.getConfig().enable) {
            return false;
        }

        return AsmpAddons.getConfig().allowOnAllServers || this.onServer;
    }
}
