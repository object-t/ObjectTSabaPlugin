package com.objectt.listener;

import com.objectt.gui.scoreboard.ScoreBoardManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {
    private final ScoreBoardManager scoreBoardManager;
    
    public PlayerJoinListener(ScoreBoardManager scoreBoardManager) {
        this.scoreBoardManager = scoreBoardManager;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        scoreBoardManager.createScoreboard(event.getPlayer());
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        scoreBoardManager.removeScoreboard(event.getPlayer());
    }
}