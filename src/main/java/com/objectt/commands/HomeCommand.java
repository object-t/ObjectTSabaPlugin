package com.objectt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.objectt.enums.Permissions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HomeCommand {
    private static final Map<UUID, Map<String, Location>> playerHomes = new HashMap<>();

    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return Commands.literal("home")
                .executes(this::teleportHome)
                .requires(Permissions.HOME_USE_COMMAND::hasPermission)

                // /home <名前> - 指定ホームにテレポート
                .then(Commands.argument("name", StringArgumentType.word())
                        .suggests(this::suggestHomeNames)
                        .executes(this::teleportToNamedHome)
                        .requires(Permissions.HOME_USE_COMMAND::hasPermission)
                )

                // /home add <名前> - ホーム追加
                .then(Commands.literal("add")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .executes(this::addHome)
                                .requires(Permissions.HOME_ADD_COMMAND::hasPermission)
                        )
                )

                // /home remove <名前> - ホーム削除
                .then(Commands.literal("remove")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(this::suggestHomeNames)
                                .executes(this::removeHome)
                                .requires(Permissions.HOME_REMOVE_COMMAND::hasPermission)
                        )
                )

                // /home list - ホーム一覧
                .then(Commands.literal("list")
                        .executes(this::listHomes)
                        .requires(Permissions.HOME_LIST_COMMAND::hasPermission)
                )

                // /home info <名前> - ホーム情報
                .then(Commands.literal("info")
                        .then(Commands.argument("name", StringArgumentType.word())
                                .suggests(this::suggestHomeNames)
                                .executes(this::homeInfo)
                                .requires(Permissions.HOME_INFO_COMMAND::hasPermission)
                        )
                )
                .build();
    }

    private record HomeResult(Player player, UUID playerId, Map<String, Location> homes, boolean exists) {}

    private HomeResult getPlayerHome(CommandContext<CommandSourceStack> ctx, String homeName) {
        Player player = (Player) ctx.getSource().getSender();
        UUID playerId = player.getUniqueId();
        
        Map<String, Location> homes = playerHomes.get(playerId);
        if (homes == null || !homes.containsKey(homeName)) {
            player.sendPlainMessage("§c'" + homeName + "'というホームは存在しません。");
            return new HomeResult(null, null, null, false);
        }
        
        return new HomeResult(player, playerId, homes, true);
    }

    private int teleportHome(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        UUID playerId = player.getUniqueId();

        Map<String, Location> homes = playerHomes.get(playerId);
        if (homes == null || homes.isEmpty()) {
            player.sendPlainMessage("§cホームが設定されていません。/home add <名前> でホームを追加してください。");
            return 0;
        }

        Location homeLocation = homes.get("home");
        if (homeLocation == null) {
            homeLocation = homes.values().iterator().next();
        }

        player.teleport(homeLocation);
        player.sendPlainMessage("§aホームにテレポートしました。");

        return Command.SINGLE_SUCCESS;
    }

    private int teleportToNamedHome(CommandContext<CommandSourceStack> ctx) {
        if (!Permissions.requiresPlayerWithLevel(ctx.getSource(), Permissions.HOME_USE_COMMAND)) {
            return 0;
        }

        String homeName = StringArgumentType.getString(ctx, "name");
        HomeResult result = getPlayerHome(ctx, homeName);
        
        if (!result.exists) {
            return 0;
        }

        Location homeLocation = result.homes.get(homeName);
        result.player.teleport(homeLocation);
        result.player.sendPlainMessage("§a'" + homeName + "'ホームにテレポートしました。");

        return Command.SINGLE_SUCCESS;
    }

    private int addHome(CommandContext<CommandSourceStack> ctx) {
        if (!Permissions.requiresPlayerWithLevel(ctx.getSource(), Permissions.HOME_ADD_COMMAND)) {
            return 0;
        }

        Player player = (Player) ctx.getSource().getSender();
        String homeName = StringArgumentType.getString(ctx, "name");
        UUID playerId = player.getUniqueId();

        if (homeName.length() > 16) {
            player.sendPlainMessage("§cホーム名は16文字以内にしてください。");
            return 0;
        }

        Map<String, Location> homes = playerHomes.computeIfAbsent(playerId, k -> new HashMap<>());

        if (!player.isOp() && homes.size() >= 5) {
            player.sendPlainMessage("§cホーム数の上限（5個）に達しています。");
            return 0;
        }

        Location currentLocation = player.getLocation();
        homes.put(homeName, currentLocation);

        player.sendPlainMessage("§a'" + homeName + "'ホームを追加しました。");
        player.sendPlainMessage("§7座標: " +
                String.format("%.1f, %.1f, %.1f (%s)",
                        currentLocation.getX(),
                        currentLocation.getY(),
                        currentLocation.getZ(),
                        currentLocation.getWorld().getName())
        );

        return Command.SINGLE_SUCCESS;
    }

    private int removeHome(CommandContext<CommandSourceStack> ctx) {
        if (!Permissions.requiresPlayerWithLevel(ctx.getSource(), Permissions.HOME_REMOVE_COMMAND)) {
            return 0;
        }

        String homeName = StringArgumentType.getString(ctx, "name");
        HomeResult result = getPlayerHome(ctx, homeName);
        
        if (!result.exists) {
            return 0;
        }

        result.homes.remove(homeName);
        if (result.homes.isEmpty()) {
            playerHomes.remove(result.playerId);
        }

        result.player.sendPlainMessage("§a'" + homeName + "'ホームを削除しました。");

        return Command.SINGLE_SUCCESS;
    }

    private int listHomes(CommandContext<CommandSourceStack> ctx) {
        if (!Permissions.requiresPlayerWithLevel(ctx.getSource(), Permissions.HOME_LIST_COMMAND)) {
            return 0;
        }

        Player player = (Player) ctx.getSource().getSender();
        UUID playerId = player.getUniqueId();

        Map<String, Location> homes = playerHomes.get(playerId);
        if (homes == null || homes.isEmpty()) {
            player.sendPlainMessage("§cホームが設定されていません。");
            return 0;
        }

        player.sendPlainMessage("§e=== あなたのホーム一覧 ===");
        homes.forEach((name, location) -> player.sendPlainMessage("§a" + name + " §7- " +
                String.format("%.0f, %.0f, %.0f (%s)",
                        location.getX(),
                        location.getY(),
                        location.getZ(),
                        location.getWorld().getName())));
        player.sendPlainMessage("§7合計: " + homes.size() + " 個のホーム");

        return Command.SINGLE_SUCCESS;
    }

    private int homeInfo(CommandContext<CommandSourceStack> ctx) {
        if (!Permissions.requiresPlayerWithLevel(ctx.getSource(), Permissions.HOME_INFO_COMMAND)) {
            return 0;
        }

        String homeName = StringArgumentType.getString(ctx, "name");
        HomeResult result = getPlayerHome(ctx, homeName);
        
        if (!result.exists) {
            return 0;
        }

        Location location = result.homes.get(homeName);
        result.player.sendPlainMessage("§e=== '" + homeName + "' ホーム情報 ===");
        result.player.sendPlainMessage("§7ワールド: §f" + location.getWorld().getName());
        result.player.sendPlainMessage("§7座標: §f" +
                String.format("X: %.1f, Y: %.1f, Z: %.1f",
                        location.getX(), location.getY(), location.getZ())
        );
        result.player.sendPlainMessage("§7方角: §f" +
                String.format("Yaw: %.1f, Pitch: %.1f",
                        location.getYaw(), location.getPitch())
        );

        return Command.SINGLE_SUCCESS;
    }

    private CompletableFuture<Suggestions> suggestHomeNames(CommandContext<CommandSourceStack> ctx, SuggestionsBuilder builder) {
        if (!(ctx.getSource().getSender() instanceof Player player)) {
            return builder.buildFuture();
        }
        
        UUID playerId = player.getUniqueId();
        Map<String, Location> homes = playerHomes.get(playerId);
        
        if (homes == null || homes.isEmpty()) {
            return builder.buildFuture();
        }
        
        String input = builder.getRemaining().toLowerCase();
        homes.keySet().stream()
                .filter(homeName -> homeName.toLowerCase().startsWith(input))
                .forEach(builder::suggest);
        
        return builder.buildFuture();
    }
}
