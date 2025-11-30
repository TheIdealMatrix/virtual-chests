package de.kevin_stefan.virtualChests;

import de.kevin_stefan.virtualChests.commands.ChestCommand;
import de.kevin_stefan.virtualChests.listeners.InventoryCloseListener;
import de.kevin_stefan.virtualChests.storage.StorageProvider;
import de.kevin_stefan.virtualChests.utils.MinecraftPlugin;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bstats.bukkit.Metrics;

public final class VirtualChests extends MinecraftPlugin {

    private static VirtualChests instance;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        StorageProvider.getInstance().testConnection();

        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commands.registrar().register(ChestCommand.build());
        });

        new Metrics(this, 27119);
    }

    @Override
    public void onDisable() {
        StorageProvider.close();
    }

    public static VirtualChests getInstance() {
        return instance;
    }

}
