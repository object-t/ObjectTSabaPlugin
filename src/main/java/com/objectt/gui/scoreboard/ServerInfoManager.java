package com.objectt.gui.scoreboard;

import org.bukkit.Bukkit;
import org.bukkit.Server;

import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;

public class ServerInfoManager {
    private static final DecimalFormat df = new DecimalFormat("#.##");
    
    public static String getServerName() {
        return Bukkit.getServer().getName();
    }
    
    public static String getOnlinePlayerCount() {
        return String.valueOf(Bukkit.getOnlinePlayers().size());
    }
    
    public static String getMaxPlayerCount() {
        return String.valueOf(Bukkit.getMaxPlayers());
    }
    
    public static String getServerVersion() {
        return Bukkit.getVersion();
    }
    
    public static String getServerUptime() {
        long uptimeMillis = ManagementFactory.getRuntimeMXBean().getUptime();
        long uptimeSeconds = uptimeMillis / 1000;
        long hours = uptimeSeconds / 3600;
        long minutes = (uptimeSeconds % 3600) / 60;
        long seconds = uptimeSeconds % 60;
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
    
    public static String getServerTPS() {
        try {
            Server server = Bukkit.getServer();
            double[] tps = server.getTPS();
            return df.format(tps[0]);
        } catch (Exception e) {
            return "N/A";
        }
    }
    
    public static String getUsedMemory() {
        Runtime runtime = Runtime.getRuntime();
        long usedMemory = runtime.totalMemory() - runtime.freeMemory();
        return formatBytes(usedMemory);
    }
    
    public static String getMaxMemory() {
        Runtime runtime = Runtime.getRuntime();
        return formatBytes(runtime.maxMemory());
    }
    
    private static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        String pre = "KMGTPE".charAt(exp - 1) + "";
        return String.format("%.1f %sB", bytes / Math.pow(1024, exp), pre);
    }
}