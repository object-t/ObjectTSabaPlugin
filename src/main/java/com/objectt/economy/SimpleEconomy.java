package com.objectt.economy;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleEconomy implements Economy {
    private final JavaPlugin plugin;
    private final Map<String, Double> balances = new ConcurrentHashMap<>();
    private final File dataFile;
    private final double startingBalance = 1000.0;
    private final Object fileLock = new Object();

    public SimpleEconomy(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "economy.dat");
        loadData();
    }

    private void loadData() {
        synchronized (fileLock) {
            if (!dataFile.exists()) {
                return;
            }

            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(dataFile))) {
                @SuppressWarnings("unchecked")
                Map<String, Double> loaded = (Map<String, Double>) ois.readObject();
                balances.putAll(loaded);
            } catch (IOException | ClassNotFoundException e) {
                plugin.getLogger().warning("Failed to load economy data: " + e.getMessage());
            }
        }
    }

    private void saveData() {
        synchronized (fileLock) {
            try {
                if (!dataFile.getParentFile().exists() && !dataFile.getParentFile().mkdirs()) {
                    throw new IOException("Failed to create parent directories");
                }
                try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(dataFile))) {
                    oos.writeObject(new HashMap<>(balances));
                }
            } catch (IOException e) {
                plugin.getLogger().severe("Failed to save economy data: " + e.getMessage());
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public String getName() {
        return "SimpleEconomy";
    }

    @Override
    public boolean hasBankSupport() {
        return false;
    }

    @Override
    public int fractionalDigits() {
        return 2;
    }

    @Override
    public String format(double amount) {
        return String.format("%.0f円", amount);
    }

    @Override
    public String currencyNamePlural() {
        return "円";
    }

    @Override
    public String currencyNameSingular() {
        return "円";
    }

    @Override
    public boolean hasAccount(@NotNull String playerName) {
        return balances.containsKey(playerName.toLowerCase());
    }

    @Override
    public boolean hasAccount(@NotNull OfflinePlayer player) {
        if (player.getName() == null) return false;
        return hasAccount(player.getName());
    }

    @Override
    public boolean hasAccount(String playerName, String worldName) {
        return hasAccount(playerName);
    }

    @Override
    public boolean hasAccount(OfflinePlayer player, String worldName) {
        return hasAccount(player);
    }

    @Override
    public double getBalance(@NotNull String playerName) {
        return balances.getOrDefault(playerName.toLowerCase(), startingBalance);
    }

    @Override
    public double getBalance(@NotNull OfflinePlayer player) {
        if (player.getName() == null) return 0;
        return getBalance(player.getName());
    }

    @Override
    public double getBalance(String playerName, String world) {
        return getBalance(playerName);
    }

    @Override
    public double getBalance(@NotNull OfflinePlayer player, String world) {
        if (player.getName() == null) return 0;
        return getBalance(player.getName());
    }

    @Override
    public boolean has(String playerName, double amount) {
        return getBalance(playerName) >= amount;
    }

    @Override
    public boolean has(@NotNull OfflinePlayer player, double amount) {
        if (player.getName() == null) return false;
        return has(player.getName(), amount);
    }

    @Override
    public boolean has(String playerName, String worldName, double amount) {
        return has(playerName, amount);
    }

    @Override
    public boolean has(@NotNull OfflinePlayer player, String worldName, double amount) {
        if (player.getName() == null) return false;
        return has(player.getName(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot withdraw negative funds");
        }

        synchronized (balances) {
            double balance = getBalance(playerName);
            if (balance < amount) {
                return new EconomyResponse(0, balance, EconomyResponse.ResponseType.FAILURE, "Insufficient funds");
            }

            double newBalance = balance - amount;
            balances.put(playerName.toLowerCase(), newBalance);
            saveData();
            
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
        }
    }

    @Override
    public EconomyResponse withdrawPlayer(@NotNull OfflinePlayer player, double amount) {
        if (player.getName() == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Invalid player");
        }
        return withdrawPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(String playerName, String worldName, double amount) {
        return withdrawPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse withdrawPlayer(@NotNull OfflinePlayer player, String worldName, double amount) {
        return withdrawPlayer(player, amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, double amount) {
        if (amount < 0) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Cannot deposit negative funds");
        }

        synchronized (balances) {
            double balance = getBalance(playerName);
            double newBalance = balance + amount;
            balances.put(playerName.toLowerCase(), newBalance);
            saveData();
            
            return new EconomyResponse(amount, newBalance, EconomyResponse.ResponseType.SUCCESS, "");
        }
    }

    @Override
    public EconomyResponse depositPlayer(@NotNull OfflinePlayer player, double amount) {
        if (player.getName() == null) {
            return new EconomyResponse(0, 0, EconomyResponse.ResponseType.FAILURE, "Invalid player");
        }
        return depositPlayer(player.getName(), amount);
    }

    @Override
    public EconomyResponse depositPlayer(String playerName, String worldName, double amount) {
        return depositPlayer(playerName, amount);
    }

    @Override
    public EconomyResponse depositPlayer(@NotNull OfflinePlayer player, String worldName, double amount) {
        return depositPlayer(player, amount);
    }

    @Override
    public boolean createPlayerAccount(String playerName) {
        synchronized (balances) {
            if (!hasAccount(playerName)) {
                balances.put(playerName.toLowerCase(), startingBalance);
                saveData();
                return true;
            }
            return false;
        }
    }

    @Override
    public boolean createPlayerAccount(@NotNull OfflinePlayer player) {
        if (player.getName() == null) return false;
        return createPlayerAccount(player.getName());
    }

    @Override
    public boolean createPlayerAccount(String playerName, String worldName) {
        return createPlayerAccount(playerName);
    }

    @Override
    public boolean createPlayerAccount(@NotNull OfflinePlayer player, String worldName) {
        return createPlayerAccount(player);
    }

    // Bank methods (not supported)
    @Override
    public EconomyResponse deleteBank(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank system not supported");
    }

    @Override
    public EconomyResponse bankBalance(String name) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank system not supported");
    }

    @Override
    public EconomyResponse bankHas(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank system not supported");
    }

    @Override
    public EconomyResponse bankWithdraw(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank system not supported");
    }

    @Override
    public EconomyResponse bankDeposit(String name, double amount) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank system not supported");
    }

    @Override
    public EconomyResponse isBankOwner(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank system not supported");
    }

    @Override
    public EconomyResponse isBankOwner(String name, @NotNull OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank system not supported");
    }

    @Override
    public EconomyResponse isBankMember(String name, String playerName) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank system not supported");
    }

    @Override
    public EconomyResponse isBankMember(String name, @NotNull OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank system not supported");
    }

    @Override
    public List<String> getBanks() {
        return List.of();
    }

    @Override
    public EconomyResponse createBank(String name, String player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank system not supported");
    }

    @Override
    public EconomyResponse createBank(String name, @NotNull OfflinePlayer player) {
        return new EconomyResponse(0, 0, EconomyResponse.ResponseType.NOT_IMPLEMENTED, "Bank system not supported");
    }
}