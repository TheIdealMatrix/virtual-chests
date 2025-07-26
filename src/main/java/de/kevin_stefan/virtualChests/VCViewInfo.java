package de.kevin_stefan.virtualChests;

import de.kevin_stefan.virtualChests.storage.model.VirtualChest;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;

public class VCViewInfo {

    @NotNull VirtualChest virtualChest;
    final @NotNull Inventory inventory;

    public VCViewInfo(@NotNull VirtualChest virtualChest, @NotNull Inventory inventory) {
        this.virtualChest = virtualChest;
        this.inventory = inventory;
    }

    @Override
    public String toString() {
        return "VCViewInfo{" + "virtualChest=" + virtualChest + ", inventory=" + inventory + '}';
    }
}
