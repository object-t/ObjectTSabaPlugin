package com.objectt.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.objectt.data.dao.Tip;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TipRepositoryImpl implements TipRepository {
    private final Plugin plugin;
    private final File tipsFile;
    private final Gson gson;
    private List<Tip> tips;
    private int nextId;
    private final Random random;
    
    public TipRepositoryImpl(Plugin plugin) {
        this.plugin = plugin;
        this.tipsFile = new File(plugin.getDataFolder(), "tips.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.tips = new ArrayList<>();
        this.nextId = 1;
        this.random = new Random();
        loadTips();
    }
    
    @Override
    public void addTip(String content) {
        tips.add(new Tip(nextId++, content));
        saveTips();
    }
    
    @Override
    public void deleteTip(int id) {
        tips.removeIf(tip -> tip.getId() == id);
        saveTips();
    }
    
    @Override
    public List<Tip> getAllTips() {
        return new ArrayList<>(tips);
    }
    
    @Override
    public Tip getRandomTip() {
        if (tips.isEmpty()) {
            return null;
        }
        return tips.get(random.nextInt(tips.size()));
    }
    
    @Override
    public void loadTips() {
        if (!tipsFile.exists()) {
            // デフォルトのTipを追加
            tips.add(new Tip(nextId++, "/info で非表示"));
            saveTips();
            return;
        }
        
        try (FileReader reader = new FileReader(tipsFile)) {
            Type listType = new TypeToken<List<Tip>>(){}.getType();
            List<Tip> loadedTips = gson.fromJson(reader, listType);
            if (loadedTips != null) {
                tips = loadedTips;
                // 最大IDを計算
                nextId = tips.stream().mapToInt(Tip::getId).max().orElse(0) + 1;
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Tips読み込みエラー: " + e.getMessage());
        }
    }
    
    @Override
    public void saveTips() {
        try {
            if (!tipsFile.getParentFile().exists()) {
                tipsFile.getParentFile().mkdirs();
            }
            
            try (FileWriter writer = new FileWriter(tipsFile)) {
                gson.toJson(tips, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Tips保存エラー: " + e.getMessage());
        }
    }
}