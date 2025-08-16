package com.objectt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.objectt.economy.VaultManager;
import com.objectt.enums.Permissions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import static com.objectt.enums.Permissions.isPlayerOnly;

public class MoneyCommand implements com.objectt.commands.Command {

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTIONS = (context, builder) -> {
        String remaining = builder.getRemaining().toLowerCase();
        Bukkit.getOnlinePlayers().forEach(player -> {
            String name = player.getName();
            if (name.toLowerCase().startsWith(remaining)) {
                builder.suggest(name);
            }
        });
        return builder.buildFuture();
    };

    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        if (!isEconomyEnabled()) {
            return null;
        }
        return Commands.literal("money")
                .executes(this::executeOwnBalance)
                .then(Commands.literal("balance")
                        .executes(this::executeOwnBalance)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                                .suggests(PLAYER_SUGGESTIONS)
                                .requires(Permissions.MONEY_ADMIN::hasPermission)
                                .executes(this::executeOtherBalance)))
                .then(Commands.literal("give")
                        .requires(Permissions.MONEY_ADMIN::hasPermission)
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                                .suggests(PLAYER_SUGGESTIONS)
                                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(this::executeGiveMoney))))
                .then(Commands.literal("pay")
                        .then(RequiredArgumentBuilder.<CommandSourceStack, String>argument("player", StringArgumentType.word())
                                .suggests(PLAYER_SUGGESTIONS)
                                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("amount", DoubleArgumentType.doubleArg(0.01))
                                        .executes(this::executePayMoney)))).build();
    }

    private boolean isEconomyEnabled() {
        return VaultManager.isEnabled();
    }

    private int executeOwnBalance(CommandContext<CommandSourceStack> context) {
        if (!isPlayerOnly(context)) return Command.SINGLE_SUCCESS;
        
        Player player = (Player) context.getSource().getSender();
        double balance = VaultManager.getBalance(player);

        if (balance < 0) {
            player.sendMessage(Component.text("残高の取得に失敗しました", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        player.sendMessage(Component.text("現在の残高: ", NamedTextColor.GREEN)
                .append(Component.text(VaultManager.format(balance), NamedTextColor.YELLOW)));
        
        return Command.SINGLE_SUCCESS;
    }

    private int executeOtherBalance(CommandContext<CommandSourceStack> context) {
        if (!isPlayerOnly(context)) return Command.SINGLE_SUCCESS;
        
        Player player = (Player) context.getSource().getSender();
        String targetName = StringArgumentType.getString(context, "player");

        double balance = VaultManager.getBalance(player);
        
        if (balance < 0) {
            player.sendMessage(Component.text("プレイヤー「" + targetName + "」の残高取得に失敗しました", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        player.sendMessage(Component.text(targetName + "の残高: ", NamedTextColor.GREEN)
                .append(Component.text(VaultManager.format(balance), NamedTextColor.YELLOW)));
        
        return Command.SINGLE_SUCCESS;
    }

    private int executeGiveMoney(CommandContext<CommandSourceStack> context) {
        if (!isPlayerOnly(context)) return Command.SINGLE_SUCCESS;
        
        Player player = (Player) context.getSource().getSender();
        String targetName = StringArgumentType.getString(context, "player");

        double amount = DoubleArgumentType.getDouble(context, "amount");

        if (VaultManager.giveMoney(player, amount)) {
            player.sendMessage(Component.text(targetName + "に" + VaultManager.format(amount) + "を付与しました", NamedTextColor.GREEN));
            
            Player targetPlayer = Bukkit.getPlayer(targetName);
            if (targetPlayer != null) {
                targetPlayer.sendMessage(Component.text(VaultManager.format(amount) + "を受け取りました", NamedTextColor.GREEN));
            }
        } else {
            player.sendMessage(Component.text("お金の付与に失敗しました", NamedTextColor.RED));
        }
        
        return Command.SINGLE_SUCCESS;
    }

    private int executePayMoney(CommandContext<CommandSourceStack> context) {
        if (!isPlayerOnly(context)) return Command.SINGLE_SUCCESS;
        
        Player player = (Player) context.getSource().getSender();
        String targetName = StringArgumentType.getString(context, "player");
        OfflinePlayer targetPlayer = Bukkit.getOfflinePlayer(targetName);

        double amount = DoubleArgumentType.getDouble(context, "amount");

        if (targetName.equalsIgnoreCase(player.getName())) {
            player.sendMessage(Component.text("自分自身にお金を送ることはできません", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        if (!VaultManager.hasMoney(player, amount)) {
            player.sendMessage(Component.text("送金するのに十分なお金を持っていません", NamedTextColor.RED));
            return Command.SINGLE_SUCCESS;
        }

        if (VaultManager.takeMoney(player, amount) && VaultManager.giveMoney((Player) targetPlayer, amount)) {
            player.playSound(player.getLocation(), Sound.ENTITY_SNIFFER_DROP_SEED, 1, 1);
            player.sendMessage(Component.text(targetName + "に" + VaultManager.format(amount) + "を送金しました", NamedTextColor.GREEN));
            
            Player onlineTarget = Bukkit.getPlayer(targetName);
            if (onlineTarget != null) {
                onlineTarget.playSound(onlineTarget.getLocation(), Sound.ENTITY_CHICKEN_EGG, 1, 1);
                onlineTarget.sendMessage(Component.text(player.getName() + "から" + VaultManager.format(amount) + "を受け取りました", NamedTextColor.GREEN));
            }
        } else {
            player.sendMessage(Component.text("送金に失敗しました", NamedTextColor.RED));
            VaultManager.giveMoney(player, amount);
        }
        
        return Command.SINGLE_SUCCESS;
    }
}