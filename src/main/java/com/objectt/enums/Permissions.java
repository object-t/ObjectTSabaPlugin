package com.objectt.enums;

import com.mojang.brigadier.context.CommandContext;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;

public enum Permissions {
    HOME_INFO_COMMAND("objectt.home.get", true),
    HOME_ADD_COMMAND("objectt.home.add", true),
    HOME_REMOVE_COMMAND("objectt.home.remove", true),
    HOME_LIST_COMMAND("objectt.home.list", true),
    HOME_USE_COMMAND("objectt.home.use", true),

    SKULL("objectt.skull", true),

    MONEY_ADMIN("objectt.money.admin", false),

    SCOREBOARD("objectt.scoreboard", true),

    FLY("objectt.fly", true);

    private final String permission;
    private final boolean canUseDefault;

    Permissions(String permission, boolean canUseDefault) {
        this.permission = permission;
        this.canUseDefault = canUseDefault;
    }

    public String getPermission() {
        return this.permission;
    }

    public boolean isCanUseDefault() {
        return this.canUseDefault;
    }

    /**
     * 指定されたPermissionを持っているか
     */
    public boolean hasPermission(CommandSourceStack stack) {
        if (!(stack.getSender() instanceof Player)) {
            return true;
        }
        return hasPermission((Player) stack.getSender());
    }
    
    /**
     * プレイヤーが指定されたPermissionを持っているか
     */
    public boolean hasPermission(Player player) {
        return player.hasPermission(permission) || player.isOp();
    }

    /**
     * プレイヤーが指定されたPermissionを持っているか
     */
    public static boolean requiresPlayerWithLevel(CommandSourceStack stack, Permissions permission) {
        if (!(stack.getSender() instanceof Player)) {
            stack.getSender().sendPlainMessage("§cこのコマンドはプレイヤーのみ実行できます。");
            return false;
        }

        return requiresLevel(stack, permission);
    }

    /**
     * 指定されたPermissionを持っているか
     */
    public static boolean requiresLevel(CommandSourceStack stack, Permissions permission) {
        if (!permission.hasPermission(stack)) {
            stack.getSender().sendPlainMessage("§c権限が不足しています。必要レベル: " + permission.name());
            return false;
        }
        
        return true;
    }

    public static boolean isPlayerOnly(CommandContext<CommandSourceStack> context) {
        if (!(context.getSource().getSender() instanceof Player)) {
            context.getSource().getSender().sendMessage(Component.text("このコマンドはプレイヤーのみ実行できます", NamedTextColor.RED));
            return false;
        }
        return true;
    }
    
    public static boolean isPlayerOp(CommandSourceStack source) {
        return source.getSender() instanceof Player && ((Player) source.getSender()).isOp();
    }
}
