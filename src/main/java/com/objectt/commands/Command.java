package com.objectt.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;

public interface Command {
    LiteralCommandNode<CommandSourceStack> getCommandNode();
}
