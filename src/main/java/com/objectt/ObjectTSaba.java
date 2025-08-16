package com.objectt;

import com.objectt.commands.HomeCommand;
import com.objectt.commands.MoneyCommand;
import com.objectt.commands.ScoreboardCommand;
import com.objectt.commands.SkullCommand;
import com.objectt.commands.TipCommand;
import com.objectt.economy.VaultManager;
import com.objectt.enums.Permissions;
import com.objectt.listener.InventoryClickListener;
import com.objectt.listener.PlayerJoinListener;
import com.objectt.repository.HomeRepository;
import com.objectt.repository.HomeRepositoryImpl;
import com.objectt.repository.TipRepository;
import com.objectt.repository.TipRepositoryImpl;
import com.objectt.gui.scoreboard.ScoreBoardManager;
import com.objectt.gui.scoreboard.ScoreBoardUpdateTask;
import io.papermc.paper.command.brigadier.Commands;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public final class ObjectTSaba extends JavaPlugin {
    private final HomeRepository homeRepository = new HomeRepositoryImpl(this);
    private final TipRepository tipRepository = new TipRepositoryImpl(this);
    private final ScoreBoardManager scoreBoardManager = new ScoreBoardManager();
    private ScoreBoardUpdateTask scoreBoardUpdateTask;

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
        setupVault();
        registerPermissions();
        registerCommands();
        registerListeners();
        setupScoreBoard();
    }

    private void setupVault() {
        getLogger().info("Vault経済システムを初期化中...");
        if (VaultManager.setupEconomy(this)) {
            getLogger().info("Vault経済システムの初期化に成功しました");
        } else {
            getLogger().warning("Vault経済システムの初期化に失敗しました - 経済機能は無効になります");
        }
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

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new InventoryClickListener(homeRepository), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(scoreBoardManager), this);
    }
    
    private void setupScoreBoard() {
        getLogger().info("スコアボードシステムを初期化中...");
        scoreBoardManager.setTipRepository(tipRepository);
        scoreBoardUpdateTask = new ScoreBoardUpdateTask(scoreBoardManager);
        scoreBoardUpdateTask.start(this);
        getLogger().info("スコアボードシステムの初期化が完了しました");
    }

    private void registerCommands() {
        getLogger().info("コマンドを登録中...");
        LifecycleEventManager<@NotNull Plugin> manager = this.getLifecycleManager();

        manager.registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            final Commands commands = event.registrar();
            
            commands.register(new HomeCommand(homeRepository).getCommandNode());
            commands.register(new SkullCommand().getCommandNode());
            commands.register(new MoneyCommand().getCommandNode());
            commands.register(new ScoreboardCommand(scoreBoardManager).getCommandNode());
            commands.register(new TipCommand(tipRepository).getCommandNode());
            
            getLogger().info("コマンド登録完了");
        });
    }

    @Override
    public void onDisable() {
        if (scoreBoardUpdateTask != null) {
            scoreBoardUpdateTask.cancel();
        }
    }
}
