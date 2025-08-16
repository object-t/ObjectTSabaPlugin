package com.objectt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.objectt.gui.scoreboard.ScoreBoardManager;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public class ScoreboardCommand implements com.objectt.commands.Command {
    
    private final ScoreBoardManager scoreBoardManager;
    
    public ScoreboardCommand(ScoreBoardManager scoreBoardManager) {
        this.scoreBoardManager = scoreBoardManager;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return Commands.literal("info")
                .requires(source -> source.getSender() instanceof Player)
                .executes(context -> {
                    Player player = (Player) context.getSource().getSender();
                    scoreBoardManager.toggleScoreboard(player);
                    
                    if (scoreBoardManager.hasScoreboard(player)) {
                        player.sendMessage(Component.text("スコアボードを表示しました", NamedTextColor.GREEN));
                    } else {
                        player.sendMessage(Component.text("スコアボードを非表示にしました", NamedTextColor.RED));
                    }
                    
                    return Command.SINGLE_SUCCESS;
                })
                .build();
    }
}