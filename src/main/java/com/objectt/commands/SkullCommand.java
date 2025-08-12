package com.objectt.commands;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.objectt.enums.Permissions;
import com.objectt.repository.SkinRepository;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class SkullCommand implements Command {
    @Override
    public LiteralCommandNode<CommandSourceStack> getCommandNode() {
        return Commands.literal("skull")
                .requires(Permissions.SKULL::hasPermission)
                .then(Commands.argument("name", StringArgumentType.word())
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 64))
                                .executes(this::getSkull)
                        )
                )
                .build();
    }

    private int getSkull(CommandContext<CommandSourceStack> ctx) {
        if (!Permissions.requiresPlayerWithLevel(ctx.getSource(), Permissions.SKULL)) {
            return 0;
        }
        
        Player player = (Player) ctx.getSource().getSender();
        String name = StringArgumentType.getString(ctx, "name");
        int amount = IntegerArgumentType.getInteger(ctx, "amount");

        player.sendPlainMessage("§7" + name + "のスキンを取得中...");
        
        CompletableFuture.supplyAsync(() -> {
            try {
                UUID uuid = Bukkit.getOfflinePlayer(name).getUniqueId();
                SkinRepository skinRepo = new SkinRepository(uuid);
                SkinRepository.Skin skin = skinRepo.fetch();
                
                PlayerProfile profile = Bukkit.createProfile(uuid, skin.name());
                profile.setProperty(new ProfileProperty("textures", skin.texture()));
                
                return profile;
            } catch (Exception e) {
                return Bukkit.getOfflinePlayer(name).getPlayerProfile();
            }
        }).thenAccept(targetProfile -> {
            Bukkit.getScheduler().runTask(Bukkit.getPluginManager().getPlugin("ObjectTSaba"), () -> {
                ItemStack item = new ItemStack(Material.PLAYER_HEAD);
                item.setAmount(amount);
                item.editMeta(meta -> ((SkullMeta) meta).setPlayerProfile(targetProfile));

                Inventory inv = player.getInventory();
                if (inv.firstEmpty() == -1) {
                    player.sendPlainMessage("§cインベントリがいっぱいです。");
                    return;
                }

                inv.addItem(item);
                player.sendPlainMessage("§a" + name + "の頭を" + amount + "個入手しました。");
            });
        });

        return com.mojang.brigadier.Command.SINGLE_SUCCESS;
    }
}
