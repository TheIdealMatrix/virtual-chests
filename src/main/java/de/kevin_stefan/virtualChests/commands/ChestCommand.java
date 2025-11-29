package de.kevin_stefan.virtualChests.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.LiteralCommandNode;
import de.kevin_stefan.virtualChests.Lang;
import de.kevin_stefan.virtualChests.VCManager;
import de.kevin_stefan.virtualChests.VirtualChests;
import de.kevin_stefan.virtualChests.storage.StorageProvider;
import de.kevin_stefan.virtualChests.storage.model.VirtualChest;
import de.kevin_stefan.virtualChests.storage.model.VirtualChestHistory;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.selector.PlayerSelectorArgumentResolver;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public final class ChestCommand {

    public static LiteralCommandNode<CommandSourceStack> build() {
        return Commands.literal(VirtualChests.getPluginConfig().getString("command_name"))
            .then(Commands.argument("number", IntegerArgumentType.integer(1))
                .requires(source -> source.getSender().hasPermission("virtualchests.use"))
                .executes(ctx -> handlePlayerOnly(ctx, ChestCommand::runOpenCommand))
                .then(Commands.argument("player", ArgumentTypes.player())
                    .requires(source -> source.getSender().hasPermission("virtualchests.admin"))
                    .suggests((ctx, builder) -> {
                        Bukkit.getOnlinePlayers().forEach(player -> builder.suggest(player.getName()));
                        return builder.buildFuture();
                    })
                    .executes(ctx -> handlePlayerOnly(ctx, ChestCommand::runOpenOtherCommand))
                    .then(Commands.literal("history")
                        .executes(ctx -> handlePlayerOnly(ctx, ChestCommand::runShowHistoryList))
                        .then(Commands.literal("-page")
                            .then(Commands.argument("page", IntegerArgumentType.integer(1))
                                .executes(ctx -> handlePlayerOnly(ctx, ChestCommand::runShowHistoryList))
                            )
                        )
                        .then(Commands.argument("id", IntegerArgumentType.integer())
                            .then(Commands.literal("view")
                                .executes(ctx -> handlePlayerOnly(ctx, ChestCommand::runViewHistory))
                            )
                            .then(Commands.literal("restore")
                                .executes(ctx -> handlePlayerOnly(ctx, ChestCommand::runRestoreHistory))
                            )
                        )
                    )
                )
            ).build();
    }

    private interface PlayerOnlyCommand {
        int run(CommandContext<CommandSourceStack> context, Player player) throws CommandSyntaxException;
    }

    private static int handlePlayerOnly(CommandContext<CommandSourceStack> ctx, PlayerOnlyCommand command) throws CommandSyntaxException {
        CommandSender sender = ctx.getSource().getSender();
        if (!(sender instanceof Player player)) {
            sender.sendPlainMessage("This command can only be used as a player");
            return Command.SINGLE_SUCCESS;
        }
        return command.run(ctx, player);
    }

    private static int runOpenCommand(CommandContext<CommandSourceStack> ctx, Player player) {
        int number = IntegerArgumentType.getInteger(ctx, "number");
        if (!player.hasPermission("virtualchests.open." + number)) {
            player.sendMessage(VirtualChests.getPluginLanguage().getFormatted(new Lang.NO_CHEST(number)));
            return Command.SINGLE_SUCCESS;
        }

        player.sendMessage(VirtualChests.getPluginLanguage().getFormatted(new Lang.OPEN_CHEST(number)));
        VCManager.openChest(player, player, number);
        return Command.SINGLE_SUCCESS;
    }

    private static int runOpenOtherCommand(CommandContext<CommandSourceStack> ctx, Player player) throws CommandSyntaxException {
        int number = IntegerArgumentType.getInteger(ctx, "number");
        final Player targetPlayer = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();

        player.sendMessage(VirtualChests.getPluginLanguage().getFormatted(new Lang.OPEN_CHEST_OTHER(number, targetPlayer.getName())));
        VCManager.openChest(player, targetPlayer, number);
        return Command.SINGLE_SUCCESS;
    }

    private static int runShowHistoryList(CommandContext<CommandSourceStack> ctx, Player player) throws CommandSyntaxException {
        int number = IntegerArgumentType.getInteger(ctx, "number");
        final Player targetPlayer = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
        int page = 1;
        try {
            page = IntegerArgumentType.getInteger(ctx, "page");
        } catch (IllegalArgumentException ignored) {
        }

        List<VirtualChestHistory> history = StorageProvider.getInstance().getVChestHistory(targetPlayer.getUniqueId(), number, page);
        if (history.isEmpty()) {
            if (page != 1) {
                Component message = VirtualChests.getPluginLanguage().getFormatted(new Lang.HISTORY_NOT_FOUND_PAGE(number, targetPlayer.getName(), page));
                player.sendMessage(message);
                return Command.SINGLE_SUCCESS;
            }
            Component message = VirtualChests.getPluginLanguage().getFormatted(new Lang.HISTORY_NOT_FOUND(number, targetPlayer.getName()));
            player.sendMessage(message);
            return Command.SINGLE_SUCCESS;
        }
        Component message = buildHistoryMessage(history, number, targetPlayer, page);
        player.sendMessage(message);
        return Command.SINGLE_SUCCESS;
    }

    private static int runViewHistory(CommandContext<CommandSourceStack> ctx, Player player) throws CommandSyntaxException {
        int number = IntegerArgumentType.getInteger(ctx, "number");
        final Player targetPlayer = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
        int id = IntegerArgumentType.getInteger(ctx, "id");

        VirtualChestHistory history = StorageProvider.getInstance().getVChestHistory(id, targetPlayer.getUniqueId(), number);
        if (history == null) {
            Component message = VirtualChests.getPluginLanguage().getFormatted(new Lang.HISTORY_NOT_FOUND_ID(number, targetPlayer.getName(), id));
            player.sendMessage(message);
            return Command.SINGLE_SUCCESS;
        }
        var lang = new Lang.HISTORY_CHEST_NAME(number, targetPlayer.getName(), id, formatDate(history.getTimestamp()));
        Component title = VirtualChests.getPluginLanguage().getFormatted(lang);
        Inventory inventory = VCManager.createVChestInventory(title, history.getContent());
        player.openInventory(inventory);
        return Command.SINGLE_SUCCESS;
    }

    private static int runRestoreHistory(CommandContext<CommandSourceStack> ctx, Player player) throws CommandSyntaxException {
        int number = IntegerArgumentType.getInteger(ctx, "number");
        final Player targetPlayer = ctx.getArgument("player", PlayerSelectorArgumentResolver.class).resolve(ctx.getSource()).getFirst();
        int id = IntegerArgumentType.getInteger(ctx, "id");

        VirtualChestHistory history = StorageProvider.getInstance().getVChestHistory(id, targetPlayer.getUniqueId(), number);
        if (history == null) {
            Component message = VirtualChests.getPluginLanguage().getFormatted(new Lang.HISTORY_NOT_FOUND_ID(number, targetPlayer.getName(), id));
            player.sendMessage(message);
            return Command.SINGLE_SUCCESS;
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
        return Command.SINGLE_SUCCESS;
    }

    private static String formatDate(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(VirtualChests.getPluginLanguage().get(new Lang.HISTORY_DATE_FORMAT()));
        return dateFormat.format(new Date(timestamp));
    }

    private static Component buildHistoryMessage(List<VirtualChestHistory> history, int number, Player player, int page) {
        long entries = StorageProvider.getInstance().getVChestHistoryCount(player.getUniqueId(), number);
        int pageSize = VirtualChests.getPluginConfig().getInt("history_page_size");
        int maxPage = Math.ceilDiv(Math.toIntExact(entries), pageSize);

        String header = VirtualChests.getPluginLanguage().get(new Lang.HISTORY_LIST_HEADER(number, player.getName()));
        String footer = VirtualChests.getPluginLanguage().get(new Lang.HISTORY_LIST_FOOTER(number, player.getName(), page, maxPage, page - 1, page + 1));

        String lines = history.stream().map(h -> {
            String date = formatDate(h.getTimestamp());
            return VirtualChests.getPluginLanguage().get(new Lang.HISTORY_LIST_LINE(number, player.getName(), h.getId(), date));
        }).collect(Collectors.joining("<br>"));

        return MiniMessage.miniMessage().deserialize(header + "<br>" + lines + "<br>" + footer);
    }

}
