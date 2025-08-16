package com.objectt.gui.scoreboard;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ScoreBoardUpdateTask extends BukkitRunnable {
    private final ScoreBoardManager scoreBoardManager;
    
    public ScoreBoardUpdateTask(ScoreBoardManager scoreBoardManager) {
        this.scoreBoardManager = scoreBoardManager;
    }
    
    @Override
    public void run() {
        scoreBoardManager.updateAllScoreboards();
    }
    
    public void start(Plugin plugin) {
        this.runTaskTimer(plugin, 0L, 10L);
    }
}