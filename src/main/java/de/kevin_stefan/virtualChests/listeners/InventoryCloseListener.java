package de.kevin_stefan.virtualChests.listeners;

import de.kevin_stefan.virtualChests.VCManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void on(InventoryCloseEvent event) {
        VCManager.closeInventory(event.getInventory());
    }
}
