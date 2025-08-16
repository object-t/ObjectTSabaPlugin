package com.objectt.economy;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class VaultManager {
    private static Economy economy = null;
    private static boolean enabled = false;
    private static SimpleEconomy simpleEconomy = null;

    public static boolean setupEconomy(JavaPlugin plugin) {
        if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
            plugin.getLogger().warning("Vault plugin not found! Economy features disabled.");
            return false;
        }

        // 既存の経済プロバイダーを探す
        RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp != null) {
            economy = rsp.getProvider();
            enabled = true;
            plugin.getLogger().info("Economy system initialized with existing provider: " + economy.getName());
            return true;
        }

        // 既存のプロバイダーがない場合、独自の経済システムを登録
        plugin.getLogger().info("No existing economy provider found. Registering SimpleEconomy...");
        simpleEconomy = new SimpleEconomy(plugin);
        plugin.getServer().getServicesManager().register(Economy.class, simpleEconomy, plugin, ServicePriority.Normal);
        
        economy = simpleEconomy;
        enabled = true;
        plugin.getLogger().info("Economy system initialized with SimpleEconomy");
        return true;
    }

    public static boolean isEnabled() {
        return enabled && economy != null;
    }

    public static double getBalance(Player player) {
        if (!isEnabled()) return -1;
        return economy.getBalance(player);
    }
    
    public static double getBalance(OfflinePlayer player) {
        if (!isEnabled()) return -1;
        return economy.getBalance(player);
    }

    public static boolean giveMoney(Player player, double amount) {
        if (!isEnabled()) return false;
        return economy.depositPlayer(player, amount).transactionSuccess();
    }

    public static boolean takeMoney(Player player, double amount) {
        if (!isEnabled()) return false;
        return economy.withdrawPlayer(player, amount).transactionSuccess();
    }

    public static boolean hasMoney(Player player, double amount) {
        if (!isEnabled()) return false;
        return economy.has(player, amount);
    }

    public static String getCurrencyNamePlural() {
        if (!isEnabled()) return "coins";
        return economy.currencyNamePlural();
    }

    public static String getCurrencyNameSingular() {
        if (!isEnabled()) return "coin";
        return economy.currencyNameSingular();
    }

    public static String format(double amount) {
        if (!isEnabled()) return String.valueOf(amount);
        return economy.format(amount);
    }
}