package de.kevin_stefan.virtualChests.listeners;

import de.kevin_stefan.virtualChests.Lang;
import de.kevin_stefan.virtualChests.VirtualChests;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.Nullable;

public class PlayerJoinUpdateNotifierListener implements Listener {

    private final String projectVersion;
    private @Nullable String newVersion;

    public PlayerJoinUpdateNotifierListener(String projectVersion) {
        this.projectVersion = projectVersion;
    }

    public void setNewVersion(@Nullable String newVersion) {
        this.newVersion = newVersion;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (newVersion == null || !event.getPlayer().hasPermission("virtualchests.update")) {
            return;
        }

        event.getPlayer().sendMessage(VirtualChests.getPluginLanguage().getFormatted(new Lang.UPDATE(newVersion, projectVersion)));
    }

}
