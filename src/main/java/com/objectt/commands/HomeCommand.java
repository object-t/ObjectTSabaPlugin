package com.objectt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.objectt.data.dao.UserHome;
import com.objectt.enums.Permissions;
import com.objectt.gui.inventory.HomeCustomInventory;
import com.objectt.repository.HomeRepository;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class HomeCommand implements com.objectt.commands.Command {
    private final HomeRepository homeRepository;

    public HomeCommand(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return Commands.literal("home")
                .executes(this::openHomeUI)
                .requires(Permissions.HOME_USE_COMMAND::hasPermission)
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

    private UserHome getPlayerHome(Player player, String homeName) {
        UUID playerId = player.getUniqueId();
        
        UserHome homes = this.homeRepository.findById(playerId);
        if (!homes.homes().containsKey(homeName)) {
            player.sendPlainMessage("§c'" + homeName + "'というホームは存在しません。");
            return homes;
        }
        
        return homes;
    }

    private int openHomeUI(CommandContext<CommandSourceStack> ctx) {
        Player player = (Player) ctx.getSource().getSender();
        UUID playerId = player.getUniqueId();

        UserHome userHome = this.homeRepository.findById(playerId);
        player.openInventory(new HomeCustomInventory(userHome).getInventory());

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

        UserHome userHome = homeRepository.findById(playerId);

        if (userHome.homes().size() >= userHome.level()) {
            player.sendPlainMessage("§cホーム数の上限（" + userHome.level() + "個）に達しています。");
            return 0;
        }

        Location currentLocation = player.getLocation();
        userHome.homes().put(homeName, new UserHome.Home(currentLocation));

        player.sendPlainMessage("§a'" + homeName + "'ホームを追加しました。");
        player.sendPlainMessage("§7座標: " +
                String.format("%.1f, %.1f, %.1f (%s)",
                        currentLocation.getX(),
                        currentLocation.getY(),
                        currentLocation.getZ(),
                        currentLocation.getWorld().getName())
        );

        this.homeRepository.save(userHome);

        return Command.SINGLE_SUCCESS;
    }

    private int removeHome(CommandContext<CommandSourceStack> ctx) {
        if (!Permissions.requiresPlayerWithLevel(ctx.getSource(), Permissions.HOME_REMOVE_COMMAND)) {
            return 0;
        }

        Player player = (Player) ctx.getSource().getSender();
        String homeName = StringArgumentType.getString(ctx, "name");
        UserHome userHome = getPlayerHome(player, homeName);

        userHome.homes().remove(homeName);
        this.homeRepository.save(userHome);
        player.sendPlainMessage("§a'" + homeName + "'ホームを削除しました。");

        return Command.SINGLE_SUCCESS;
    }

    private int listHomes(CommandContext<CommandSourceStack> ctx) {
        if (!Permissions.requiresPlayerWithLevel(ctx.getSource(), Permissions.HOME_LIST_COMMAND)) {
            return 0;
        }

        Player player = (Player) ctx.getSource().getSender();
        UUID playerId = player.getUniqueId();

        UserHome userHome = this.homeRepository.findById(playerId);
        if (userHome.homes().isEmpty()) {
            player.sendPlainMessage("§cホームが設定されていません。");
            return 0;
        }

        player.sendPlainMessage("§e=== あなたのホーム一覧 ===");
        userHome.homes().forEach((name, home) -> {
            Location location = home.location();
            player.sendPlainMessage("§a" + name + " §7- " +
                    String.format("%.0f, %.0f, %.0f (%s)",
                            location.getX(),
                            location.getY(),
                            location.getZ(),
                            location.getWorld().getName()));
        });
        player.sendPlainMessage("§7合計: " + userHome.homes().size() + " 個のホーム");

        return Command.SINGLE_SUCCESS;
    }

    private int homeInfo(CommandContext<CommandSourceStack> ctx) {
        if (!Permissions.requiresPlayerWithLevel(ctx.getSource(), Permissions.HOME_INFO_COMMAND)) {
            return 0;
        }

        Player player = (Player) ctx.getSource().getSender();
        String homeName = StringArgumentType.getString(ctx, "name");
        UserHome userHome = getPlayerHome(player, homeName);

        Location location = userHome.homes().get(homeName).location();
        player.sendPlainMessage("§e=== '" + homeName + "' ホーム情報 ===");
        player.sendPlainMessage("§7ワールド: §f" + location.getWorld().getName());
        player.sendPlainMessage("§7座標: §f" +
                String.format("X: %.1f, Y: %.1f, Z: %.1f",
                        location.getX(), location.getY(), location.getZ())
        );
        player.sendPlainMessage("§7方角: §f" +
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
        UserHome userHome = this.homeRepository.findById(playerId);
        
        if (userHome.homes().isEmpty()) {
            return builder.buildFuture();
        }
        
        String input = builder.getRemaining().toLowerCase();
        userHome.homes().keySet().stream()
                .filter(homeName -> homeName.toLowerCase().startsWith(input))
                .forEach(builder::suggest);
        
        return builder.buildFuture();
    }
}
