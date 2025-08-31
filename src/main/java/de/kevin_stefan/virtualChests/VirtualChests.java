package de.kevin_stefan.virtualChests;

import de.kevin_stefan.virtualChests.commands.ChestCommand;
import de.kevin_stefan.virtualChests.listeners.InventoryCloseListener;
import de.kevin_stefan.virtualChests.storage.StorageProvider;
import de.kevin_stefan.virtualChests.utils.MinecraftPlugin;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bstats.bukkit.Metrics;

public final class VirtualChests extends MinecraftPlugin {

    private static VirtualChests instance;

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this));
    }

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        StorageProvider.getInstance().testConnection();

        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);

        CommandAPI.onEnable();
        ChestCommand.register();

        new Metrics(this, 27119);
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
        StorageProvider.close();
    }

    public static VirtualChests getInstance() {
        return instance;
    }

}
