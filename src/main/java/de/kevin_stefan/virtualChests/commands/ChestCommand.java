package de.kevin_stefan.virtualChests.commands;

import de.kevin_stefan.virtualChests.Lang;
import de.kevin_stefan.virtualChests.VCManager;
import de.kevin_stefan.virtualChests.VirtualChests;
import de.kevin_stefan.virtualChests.storage.StorageProvider;
import de.kevin_stefan.virtualChests.storage.model.VirtualChest;
import de.kevin_stefan.virtualChests.storage.model.VirtualChestHistory;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.LiteralArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public final class ChestCommand {

    public static void register() {
        new CommandTree(VirtualChests.getPluginConfig().getString("command_name"))
            .then(new IntegerArgument("number", 1)
                .withPermission("virtualchests.use")
                .executesPlayer((player, args) -> {
                    int number = (int) args.get("number");
                    if (!player.hasPermission("virtualchests.open." + number)) {
                        player.sendMessage(VirtualChests.getPluginLanguage().getFormatted(new Lang.NO_CHEST(number)));
                        return;
                    }

                    player.sendMessage(VirtualChests.getPluginLanguage().getFormatted(new Lang.OPEN_CHEST(number)));
                    VCManager.openChest(player, player, number);
                }).then(new OfflinePlayerArgument("player")
                    .replaceSafeSuggestions(SafeSuggestions.suggest(info ->
                        Bukkit.getOnlinePlayers().toArray(Player[]::new)) // new Player[0]
                    )
                    .withPermission("virtualchests.admin")
                    .executesPlayer((player, args) -> {
                        int number = (int) args.get("number");
                        OfflinePlayer targetPlayer = (OfflinePlayer) args.get("player");
                        player.sendMessage(VirtualChests.getPluginLanguage().getFormatted(new Lang.OPEN_CHEST_OTHER(number, targetPlayer.getName())));
                        VCManager.openChest(player, targetPlayer, number);
                    }).then(new LiteralArgument("history")
                        .executesPlayer((player, args) -> {
                            int number = (int) args.get("number");
                            OfflinePlayer targetPlayer = (OfflinePlayer) args.get("player");
                            List<VirtualChestHistory> history = StorageProvider.getInstance().getVChestHistory(targetPlayer.getUniqueId(), number);
                            if (history.isEmpty()) {
                                Component message = VirtualChests.getPluginLanguage().getFormatted(new Lang.HISTORY_NOT_FOUND(number, targetPlayer.getName()));
                                player.sendMessage(message);
                                return;
                            }
                            Component message = buildHistoryMessage(history, number, targetPlayer.getName());
                            player.sendMessage(message);
                        }).then(new IntegerArgument("id")
                            .then(new LiteralArgument("view")
                                .executesPlayer((player, args) -> {
                                    int number = (int) args.get("number");
                                    OfflinePlayer targetPlayer = (OfflinePlayer) args.get("player");
                                    int id = (int) args.get("id");
                                    VirtualChestHistory history = StorageProvider.getInstance().getVChestHistory(id, targetPlayer.getUniqueId(), number);
                                    if (history == null) {
                                        Component message = VirtualChests.getPluginLanguage().getFormatted(new Lang.HISTORY_NOT_FOUND_ID(number, targetPlayer.getName(), id));
                                        player.sendMessage(message);
                                        return;
                                    }
                                    var lang = new Lang.HISTORY_CHEST_NAME(number, targetPlayer.getName(), id, formatDate(history.getTimestamp()));
                                    Component title = VirtualChests.getPluginLanguage().getFormatted(lang);
                                    Inventory inventory = VCManager.createVChestInventory(title, history.getContent());
                                    player.openInventory(inventory);
                                })
                            )
                            .then(new LiteralArgument("restore")
                                .executesPlayer((player, args) -> {
                                    int number = (int) args.get("number");
                                    OfflinePlayer targetPlayer = (OfflinePlayer) args.get("player");
                                    int id = (int) args.get("id");

                                    VirtualChestHistory history = StorageProvider.getInstance().getVChestHistory(id, targetPlayer.getUniqueId(), number);
                                    if (history == null) {
                                        Component message = VirtualChests.getPluginLanguage().getFormatted(new Lang.HISTORY_NOT_FOUND_ID(number, targetPlayer.getName(), id));
                                        player.sendMessage(message);
                                        return;
                                    }

                                    VirtualChest virtualChest = StorageProvider.getInstance().getVChest(targetPlayer.getUniqueId(), number);
                                    if (virtualChest == null) {
                                        virtualChest = new VirtualChest();
                                        virtualChest.setPlayer(targetPlayer.getUniqueId());
                                        virtualChest.setNumber(number);
                                    }
                                    virtualChest.setContent(history.getContent());

                                    StorageProvider.getInstance().setVChest(virtualChest);
                                    VCManager.addHistory(targetPlayer.getUniqueId(), number, history.getContent());

                                    var lang = new Lang.HISTORY_RESTORED(number, targetPlayer.getName(), id, formatDate(history.getTimestamp()));
                                    Component message = VirtualChests.getPluginLanguage().getFormatted(lang);
                                    player.sendMessage(message);
                                })
                            )
                        )
                    )
                )
            )
            .register();
    }

    private static String formatDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(VirtualChests.getPluginLanguage().get(new Lang.HISTORY_DATE_FORMAT()));
        return dateFormat.format(new Date(timestamp));
    }

    private static Component buildHistoryMessage(List<VirtualChestHistory> history, int number, String player) {
        String header = VirtualChests.getPluginLanguage().get(new Lang.HISTORY_LIST_HEADER(number, player));
        String footer = VirtualChests.getPluginLanguage().get(new Lang.HISTORY_LIST_FOOTER(number, player));

        String lines = history.stream().map(h -> {
            String date = formatDate(h.getTimestamp());
            return VirtualChests.getPluginLanguage().get(new Lang.HISTORY_LIST_LINE(number, player, h.getId(), date));
        }).collect(Collectors.joining("<br>"));

        return MiniMessage.miniMessage().deserialize(header + "<br>" + lines + "<br>" + footer);
    }

}
