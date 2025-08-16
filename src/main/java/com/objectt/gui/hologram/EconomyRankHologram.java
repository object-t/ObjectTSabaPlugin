package com.objectt.gui.hologram;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;
import com.objectt.economy.VaultManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.stream.Collectors;

public class EconomyRankHologram {
    private final Plugin plugin;
    
    public EconomyRankHologram(Plugin plugin) {
        this.plugin = plugin;
    }
    
    public void updateRanking() {
        Hologram hologram = findEconomyRankHologram();
        if (hologram == null) {
            plugin.getLogger().info("economy_rank ホログラムが見つかりません");
            return;
        }

        List<Component> lines = new ArrayList<>();

        lines.add(createGradientTitle());
        lines.add(Component.text("=".repeat(25), NamedTextColor.DARK_GRAY));
        lines.add(Component.empty());

        List<PlayerBalance> rankings = getTopPlayers();
        
        for (int i = 0; i < rankings.size(); i++) {
            PlayerBalance playerBalance = rankings.get(i);
            Component rankComponent = createRankComponent(i + 1, playerBalance);
            lines.add(rankComponent);
        }
        
        lines.add(Component.empty());
        lines.add(Component.text()
                .append(Component.text("更新日時: ", NamedTextColor.DARK_GRAY))
                .append(Component.text(getCurrentTime(), NamedTextColor.GRAY))
                .build());

        List<String> stringLines = lines.stream()
                .map(component -> LegacyComponentSerializer.legacySection().serialize(component))
                .collect(Collectors.toList());
        DHAPI.setHologramLines(hologram, stringLines);
    }
    
    private Component createGradientTitle() {
        return Component.text()
                .append(Component.text("経済ランキング ", TextColor.color(255, 215, 0), TextDecoration.BOLD))
                .append(Component.text("TOP10", TextColor.color(255, 255, 0), TextDecoration.BOLD))
                .build();
    }
    
    private Component createRankComponent(int rank, PlayerBalance playerBalance) {
        return Component.text()
                .append(Component.text(getRankSymbol(rank), getRankTextColor(rank)))
                .append(Component.text(" "))
                .append(Component.text(playerBalance.getPlayerName(), NamedTextColor.WHITE))
                .append(Component.text(" - ", NamedTextColor.GRAY))
                .append(Component.text(VaultManager.format(playerBalance.getBalance()), NamedTextColor.GREEN))
                .build();
    }
    
    private TextColor getRankTextColor(int rank) {
        return switch (rank) {
            case 1 -> TextColor.color(255, 215, 0);
            case 2 -> TextColor.color(192, 192, 192);
            case 3 -> TextColor.color(205, 127, 50);
            default -> NamedTextColor.WHITE;
        };
    }
    
    private Hologram findEconomyRankHologram() {
        return DHAPI.getHologram("economy_rank");
    }
    
    private List<PlayerBalance> getTopPlayers() {
        List<PlayerBalance> balances = new ArrayList<>();
        
        for (OfflinePlayer player : Bukkit.getOfflinePlayers()) {
            if (player.hasPlayedBefore() && player.getName() != null) {
                double balance = VaultManager.getBalance(player);
                if (balance > 0) {
                    balances.add(new PlayerBalance(player.getName(), balance));
                }
            }
        }
        
        return balances.stream()
                .sorted((a, b) -> Double.compare(b.getBalance(), a.getBalance()))
                .limit(10)
                .collect(Collectors.toList());
    }
    
    
    private String getRankSymbol(int rank) {
        return switch (rank) {
            case 1 -> "👑";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> rank + ".";
        };
    }
    
    
    private String getCurrentTime() {
        return java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("MM/dd HH:mm"));
    }
    
    private static class PlayerBalance {
        private final String playerName;
        private final double balance;
        
        public PlayerBalance(String playerName, double balance) {
            this.playerName = playerName;
            this.balance = balance;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public double getBalance() {
            return balance;
        }
    }
}