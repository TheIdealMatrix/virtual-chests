package de.kevin_stefan.virtualChests;

import de.kevin_stefan.virtualChests.storage.StorageProvider;
import de.kevin_stefan.virtualChests.utils.MinecraftPlugin;

public final class VirtualChests extends MinecraftPlugin {

    private static VirtualChests instance;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        StorageProvider.getInstance().testConnection();
    }

    @Override
    public void onDisable() {
        StorageProvider.close();
    }

    public static VirtualChests getInstance() {
        return instance;
    }

}
