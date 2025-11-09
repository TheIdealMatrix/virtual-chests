package de.kevin_stefan.virtualChests;

import de.kevin_stefan.virtualChests.storage.StorageProvider;
import de.kevin_stefan.virtualChests.storage.model.VirtualChest;
import de.kevin_stefan.virtualChests.storage.model.VirtualChestHistory;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class VCManager {

    private static final HashMap<UUID, VCViewInfo> openChests = new HashMap<>();

    private VCManager() {
    }

    public static void openChest(Player player, OfflinePlayer targetPlayer, int number) {
        if (!openChests.containsKey(targetPlayer.getUniqueId())) {
            Component title = VirtualChests.getPluginLanguage().getFormatted(new Lang.CHEST_NAME(targetPlayer.getName(), number));

            Inventory inventory;
            VirtualChest virtualChest = StorageProvider.getInstance().getVChest(targetPlayer.getUniqueId(), number);
            if (virtualChest == null) {
                virtualChest = new VirtualChest();
                virtualChest.setPlayer(targetPlayer.getUniqueId());
                virtualChest.setNumber(number);
                inventory = createVChestInventory(title, null);
            } else {
                inventory = createVChestInventory(title, virtualChest.getContent());
            }

            openChests.put(targetPlayer.getUniqueId(), new VCViewInfo(virtualChest, inventory));
        }

        player.openInventory(openChests.get(targetPlayer.getUniqueId()).inventory);
    }

    public static void closeInventory(Inventory inventory) {
        List<VCViewInfo> list = openChests.values().stream().filter(viewInfo -> viewInfo.inventory.equals(inventory)).toList();
        if (list.isEmpty()) {
            return; // Not a Virtual Chest Inventory
        }
        if (list.size() > 1) {
            VirtualChests.getInstance().getLogger().warning("Found multiple inventories in the list, this should not happen");
            return;
        }

        VCViewInfo viewInfo = list.getFirst();
        final byte[] serializedItems = ItemStack.serializeItemsAsBytes(inventory.getContents());
        if (inventory.isEmpty()) {
            StorageProvider.getInstance().deleteVChest(viewInfo.virtualChest);
        } else {
            viewInfo.virtualChest.setContent(serializedItems);
            VirtualChest savedVChest = StorageProvider.getInstance().setVChest(viewInfo.virtualChest);
            if (savedVChest != null) {
                viewInfo.virtualChest = savedVChest;
            } else {
                VirtualChests.getPluginLogger().severe("Unexpected error occurred: Could not save VirtualChest");
            }
        }

        addHistory(viewInfo.virtualChest.getPlayer(), viewInfo.virtualChest.getNumber(), serializedItems);

        if (inventory.getViewers().size() == 1) {
            // Only the player closing the inventory is viewing
            openChests.remove(viewInfo.virtualChest.getPlayer());
        }
    }

    public static Inventory createVChestInventory(Component title, @Nullable byte[] content) {
        int size = VirtualChests.getPluginConfig().getInt("chest_rows") * 9;
        Inventory inventory = Bukkit.createInventory(null, size, title);
        if (content != null) {
            ItemStack[] items = ItemStack.deserializeItemsFromBytes(content);
            if (items.length > inventory.getSize()) {
                throw new RuntimeException(String.format("More items than space in chest. Did you lower chest_rows in config? (expected %d, got %d)", items.length / 9, inventory.getSize() / 9));
            }
            inventory.setContents(items);
        }
        return inventory;
    }

    public static void addHistory(UUID player, int number, byte[] content) {
        Bukkit.getScheduler().runTaskAsynchronously(VirtualChests.getInstance(), () -> {
            VirtualChestHistory newHistory = new VirtualChestHistory();
            newHistory.setPlayer(player);
            newHistory.setNumber(number);
            newHistory.setContent(content);
            newHistory.setTimestamp(System.currentTimeMillis());

            VirtualChestHistory lastHistory = StorageProvider.getInstance().getLastVChestHistory(player, number);
            if (lastHistory == null || !Arrays.equals(content, lastHistory.getContent())) {
                StorageProvider.getInstance().addVChestHistory(newHistory);
            }
        });
    }

}
