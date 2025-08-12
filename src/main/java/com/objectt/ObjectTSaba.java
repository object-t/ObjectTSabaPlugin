package com.objectt;

import com.objectt.commands.HomeCommand;
import com.objectt.commands.SkullCommand;
import com.objectt.enums.Permissions;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ObjectTSaba extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info("""
                
                =================================================================================================================
                
                
                   _|_|    _|        _|                        _|      _|_|_|_|_|  _|_|_|    _|                      _|           \s
                 _|    _|  _|_|_|          _|_|      _|_|_|  _|_|_|_|      _|      _|    _|  _|  _|    _|    _|_|_|      _|_|_|   \s
                 _|    _|  _|    _|  _|  _|_|_|_|  _|          _|          _|      _|_|_|    _|  _|    _|  _|    _|  _|  _|    _| \s
                 _|    _|  _|    _|  _|  _|        _|          _|          _|      _|        _|  _|    _|  _|    _|  _|  _|    _| \s
                   _|_|    _|_|_|    _|    _|_|_|    _|_|_|      _|_|      _|      _|        _|    _|_|_|    _|_|_|  _|  _|    _| \s
                                     _|                                                                          _|               \s
                                   _|                                                                        _|_|                 \s
                =================================================================================================================""");
        registerPermissions();
        registerCommands();
    }

    private void registerPermissions() {
        getLogger().info("権限を登録中...");
        
        for (Permissions perm : Permissions.values()) {
            try {
                // 既に登録済みかチェック
                if (getServer().getPluginManager().getPermission(perm.getPermission()) == null) {
                    Permission permission = new Permission(
                        perm.getPermission(),
                        "ObjectT Plugin - " + perm.name() + " permission",
                        perm.isCanUseDefault() ? PermissionDefault.TRUE : PermissionDefault.OP
                    );
                    
                    getServer().getPluginManager().addPermission(permission);
                    getLogger().info("権限登録: " + perm.getPermission());
                }
            } catch (Exception e) {
                getLogger().warning("権限登録エラー: " + perm.getPermission() + " - " + e.getMessage());
            }
        }
        
        getLogger().info("権限登録完了！");
    }

    private void registerCommands() {
        getLogger().info("コマンドを登録中...");
        
        LifecycleEventManager<@NotNull Plugin> manager = this.getLifecycleManager();
        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            
            commands.register(new HomeCommand().getCommandNode());
            commands.register(new SkullCommand().getCommandNode());
            
            getLogger().info("コマンド登録完了");
        });
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
