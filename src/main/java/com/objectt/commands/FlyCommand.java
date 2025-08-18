package com.objectt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.objectt.enums.Permissions;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class FlyCommand implements com.objectt.commands.Command {
    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return Commands.literal("fly")
                .requires(Permissions.FLY::hasPermission)
                .executes(context -> {
                    // プレイヤーチェック
                    if (!Permissions.isPlayerOnly(context)) {
                        return 0;
                    }
                    
                    Player player = (Player) context.getSource().getSender();
                    togglePlayerFlight(player);

                    // フライト状態に応じてメッセージをプレイヤーに通知
                    if (player.getAllowFlight()) {
                        player.sendMessage(Component.text("フライトが有効になりました！", NamedTextColor.GREEN));
                    } else {
                        player.sendMessage(Component.text("フライトが無効になりました！", NamedTextColor.RED));
                    }
                    
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }

    public static void togglePlayerFlight(Player player) {
        boolean playerFlightState = player.getAllowFlight();
        player.setAllowFlight(!playerFlightState);
        
        // 実際の飛行状態も制御
        player.setFlying(!playerFlightState);
    }
}
