package de.kevin_stefan.virtualChests.commands;

import de.kevin_stefan.virtualChests.Lang;
import de.kevin_stefan.virtualChests.VCManager;
import de.kevin_stefan.virtualChests.VirtualChests;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.OfflinePlayerArgument;
import dev.jorel.commandapi.arguments.SafeSuggestions;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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
                    })
                )
            )
            .register();
    }

}
