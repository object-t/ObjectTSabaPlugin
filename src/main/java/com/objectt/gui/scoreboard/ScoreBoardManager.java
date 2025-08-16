package com.objectt.gui.scoreboard;

import com.objectt.economy.VaultManager;
import com.objectt.data.dao.Tip;
import com.objectt.repository.TipRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.HashMap;
import java.util.Map;

public class ScoreBoardManager {
    private final Map<Player, Scoreboard> playerScoreboards = new HashMap<>();
    private TipRepository tipRepository;
    private String currentTip = "　/info で非表示";
    private long lastTipChange = 0;
    private static final long TIP_CHANGE_INTERVAL = 30000; // 30秒
    
    public void setTipRepository(TipRepository tipRepository) {
        this.tipRepository = tipRepository;
    }
    
    public void createScoreboard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard scoreboard = manager.getNewScoreboard();
        
        Objective objective = scoreboard.registerNewObjective("serverinfo", Criteria.DUMMY,
                createGradientTitle());
        objective.setDisplaySlot(DisplaySlot.SIDEBAR);

        updateScoreboard(player, scoreboard, objective);
        
        player.setScoreboard(scoreboard);
        playerScoreboards.put(player, scoreboard);
    }
    
    public void updateScoreboard(Player player, Scoreboard scoreboard, Objective objective) {
        // 既存のスコアをクリア
        for (String entry : scoreboard.getEntries()) {
            scoreboard.resetScores(entry);
        }
        setScore(objective, Component.text("　・＝＝＝＝＝＝＝＝＝＝＝＝＝・", NamedTextColor.GRAY), 14);
        setScore(objective, Component.text("　  "), 13);
        setScore(objective, Component.text("　　プレイヤー:", NamedTextColor.YELLOW), 12);
        setScore(objective, Component.text(" 　　 " + ServerInfoManager.getOnlinePlayerCount() +
                "/" + ServerInfoManager.getMaxPlayerCount()), 11);
        setScore(objective, Component.text(" "), 10);
        
        setScore(objective, Component.text("　　所持金:", NamedTextColor.YELLOW), 9);
        setScore(objective, Component.text("　  　" + VaultManager.format(VaultManager.getBalance(player))), 8);
        setScore(objective, Component.text("　  "), 7);
        
        setScore(objective, Component.text("　　TPS:", NamedTextColor.YELLOW), 6);
        setScore(objective, Component.text("　  　" + ServerInfoManager.getServerTPS()), 5);
        setScore(objective, Component.text("   "), 4);

        // Tipを30秒ごとに切り替え
        updateTipIfNeeded();

        if (player.isOp()) {
            setScore(objective, Component.text("　　メモリ使用量:", NamedTextColor.YELLOW), 3);
            setScore(objective, Component.text(" 　 　" +  ServerInfoManager.getUsedMemory() +
                    "/" + ServerInfoManager.getMaxMemory()), 2);
            setScore(objective, Component.text("    "), 1);
            setScore(objective, Component.text(currentTip, NamedTextColor.GRAY), 0);
        } else {
            setScore(objective, Component.text(currentTip, NamedTextColor.GRAY), 3);
            setScore(objective, Component.text("    "), 2);
        }
        setScore(objective, Component.text("　・＝＝＝＝＝＝＝＝＝＝＝＝＝・", NamedTextColor.GRAY), -1);

    }

    private void setScore(Objective objective, Component text, int score) {
        String entry = "line" + score;
        Score scoreEntry = objective.getScore(entry);
        scoreEntry.setScore(score);
        scoreEntry.customName(text);
    }
    
    public void updateAllScoreboards() {
        for (Map.Entry<Player, Scoreboard> entry : playerScoreboards.entrySet()) {
            Player player = entry.getKey();
            Scoreboard scoreboard = entry.getValue();
            
            if (player.isOnline()) {
                Objective objective = scoreboard.getObjective("serverinfo");
                if (objective != null) {
                    updateScoreboard(player, scoreboard, objective);
                }
            }
        }
    }
    
    public void removeScoreboard(Player player) {
        playerScoreboards.remove(player);
    }
    
    public boolean hasScoreboard(Player player) {
        return playerScoreboards.containsKey(player);
    }
    
    public void toggleScoreboard(Player player) {
        if (hasScoreboard(player)) {
            player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
            removeScoreboard(player);
        } else {
            createScoreboard(player);
        }
    }
    
    private Component createGradientTitle() {
        String text = "　◆ おぶじぇってぃ鯖 ◆　";
        Component result = Component.empty();
        
        // #4287f5 (青) から #c242f5 (紫) への文字のグラデーション
        int startR = 0x42, startG = 0x87, startB = 0xf5;
        int endR = 0xc2, endG = 0x42, endB = 0xf5;
        
        for (int i = 0; i < text.length(); i++) {
            float ratio = (float) i / (text.length() - 1);
            int r = (int) (startR + (endR - startR) * ratio);
            int g = (int) (startG + (endG - startG) * ratio);
            int b = startB; // 青は固定
            
            result = result.append(Component.text(String.valueOf(text.charAt(i)), TextColor.color(r, g, b)));
        }
        
        return result;
    }
    
    private void updateTipIfNeeded() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTipChange >= TIP_CHANGE_INTERVAL) {
            if (tipRepository != null) {
                Tip randomTip = tipRepository.getRandomTip();
                if (randomTip != null) {
                    currentTip = "　" + randomTip.getContent();
                }
            }
            lastTipChange = currentTime;
        }
    }
}