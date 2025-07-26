package de.kevin_stefan.virtualChests;

import de.kevin_stefan.virtualChests.utils.MinecraftPlugin;

public final class VirtualChests extends MinecraftPlugin {

    private static VirtualChests instance;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static VirtualChests getInstance() {
        return instance;
    }

}
