package de.kevin_stefan.virtualChests;

import de.kevin_stefan.virtualChests.storage.StorageProvider;
import de.kevin_stefan.virtualChests.storage.model.VirtualChest;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public class VCManager {

    private static final HashMap<UUID, VCViewInfo> openChests = new HashMap<>();

    private VCManager() {
    }

    public static void openChest(Player player, OfflinePlayer targetPlayer, int number) {
        if (!openChests.containsKey(targetPlayer.getUniqueId())) {
            String message = VirtualChests.getPluginLanguage().get(Lang.CHEST_NAME, targetPlayer.getName(), number);
            int size = VirtualChests.getPluginConfig().getInt("chest_rows") * 9;
            Inventory inventory = Bukkit.createInventory(null, size, Component.text(message));

            VirtualChest virtualChest = StorageProvider.getInstance().getVChest(targetPlayer.getUniqueId(), number);
            if (virtualChest == null) {
                virtualChest = new VirtualChest();
                virtualChest.setPlayer(targetPlayer.getUniqueId());
                virtualChest.setNumber(number);
            } else {
                ItemStack[] items = ItemStack.deserializeItemsFromBytes(virtualChest.getContent());
                inventory.setContents(items);
            }

            openChests.put(targetPlayer.getUniqueId(), new VCViewInfo(virtualChest, inventory));
        }

        player.openInventory(openChests.get(targetPlayer.getUniqueId()).inventory);
    }

    public static void closeInventory(Inventory inventory) {
        var list = openChests.entrySet().stream().filter(e -> e.getValue().inventory.equals(inventory)).toList();
        if (list.isEmpty()) {
            return; // Not a Virtual Chest Inventory
        }
        if (list.size() > 1) {
            VirtualChests.getInstance().getLogger().warning("Found multiple inventories in the list, this should not happen");
            return;
        }

        VCViewInfo viewInfo = list.getFirst().getValue();
        if (inventory.isEmpty()) {
            StorageProvider.getInstance().deleteVChest(viewInfo.virtualChest);
        } else {
            viewInfo.virtualChest.setContent(ItemStack.serializeItemsAsBytes(inventory.getContents()));
            viewInfo.virtualChest = StorageProvider.getInstance().setVChest(viewInfo.virtualChest);
        }

        if (inventory.getViewers().size() == 1) {
            // Only the player closing the inventory is viewing
            openChests.remove(viewInfo.virtualChest.getPlayer());
        }
    }

}
