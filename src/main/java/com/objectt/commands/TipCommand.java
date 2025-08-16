package com.objectt.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.objectt.data.dao.Tip;
import com.objectt.enums.Permissions;
import com.objectt.repository.TipRepository;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

import java.util.List;

public class TipCommand implements com.objectt.commands.Command {
    
    private final TipRepository tipRepository;
    
    public TipCommand(TipRepository tipRepository) {
        this.tipRepository = tipRepository;
    }

    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return Commands.literal("tip")
                .requires(Permissions::isPlayerOp)
                .then(Commands.literal("add")
                        .then(Commands.argument("content", StringArgumentType.greedyString())
                                .executes(this::addTip)))
                .then(Commands.literal("delete")
                        .then(Commands.argument("id", IntegerArgumentType.integer(1))
                                .executes(this::deleteTip)))
                .then(Commands.literal("list")
                        .executes(this::listTips))
                .build();
    }
    
    private int addTip(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        String content = StringArgumentType.getString(context, "content");
        
        tipRepository.addTip(content);
        player.sendMessage(Component.text("Tipを追加しました: " + content, NamedTextColor.GREEN));
        
        return Command.SINGLE_SUCCESS;
    }
    
    private int deleteTip(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        int id = IntegerArgumentType.getInteger(context, "id");
        
        List<Tip> tips = tipRepository.getAllTips();
        boolean found = tips.stream().anyMatch(tip -> tip.getId() == id);
        
        if (found) {
            tipRepository.deleteTip(id);
            player.sendMessage(Component.text("Tip ID " + id + " を削除しました", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Tip ID " + id + " が見つかりません", NamedTextColor.RED));
        }
        
        return Command.SINGLE_SUCCESS;
    }
    
    private int listTips(CommandContext<CommandSourceStack> context) {
        Player player = (Player) context.getSource().getSender();
        List<Tip> tips = tipRepository.getAllTips();
        
        if (tips.isEmpty()) {
            player.sendMessage(Component.text("Tipが登録されていません", NamedTextColor.YELLOW));
        } else {
            player.sendMessage(Component.text("=== Tips一覧 ===", NamedTextColor.YELLOW));
            for (Tip tip : tips) {
                player.sendMessage(Component.text("ID:" + tip.getId() + " - " + tip.getContent(), NamedTextColor.WHITE));
            }
        }
        
        return Command.SINGLE_SUCCESS;
    }
}