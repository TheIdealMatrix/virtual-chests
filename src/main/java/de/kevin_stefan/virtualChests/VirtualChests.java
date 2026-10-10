package de.kevin_stefan.virtualChests;

import com.mojang.brigadier.CommandDispatcher;
import de.kevin_stefan.virtualChests.commands.ChestCommand;
import de.kevin_stefan.virtualChests.listeners.InventoryCloseListener;
import de.kevin_stefan.virtualChests.listeners.PlayerJoinUpdateNotifierListener;
import de.kevin_stefan.virtualChests.storage.StorageProvider;
import de.kevin_stefan.virtualChests.utils.MinecraftPlugin;
import de.kevin_stefan.virtualChests.utils.UpdateNotifier;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bstats.bukkit.Metrics;

public final class VirtualChests extends MinecraftPlugin {

    private static VirtualChests instance;
    private CommandDispatcher<CommandSourceStack> commandDispatcher;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;
        StorageProvider.getInstance().testConnection();

        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);

        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            commandDispatcher = commands.registrar().getDispatcher();
            commands.registrar().register(ChestCommand.getCommand());
        });

        new Metrics(this, 27119);

        if (getPluginConfig().getBoolean("update_notifier", true)) {
            var listener = new PlayerJoinUpdateNotifierListener(getPluginMeta().getVersion());
            new UpdateNotifier("V5AV5LfW")
                    .projectVersion(getPluginMeta().getVersion())
                    .minecraftVersion(getServer().getMinecraftVersion())
                    .logger(getLogger())
                    .onNewVersion(listener::setNewVersion)
                    .run();
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    @Override
    public void onDisable() {
        StorageProvider.close();
    }

    public static VirtualChests getInstance() {
        return instance;
    }

    public CommandDispatcher<CommandSourceStack> getCommandDispatcher() {
        return commandDispatcher;
    }

}
