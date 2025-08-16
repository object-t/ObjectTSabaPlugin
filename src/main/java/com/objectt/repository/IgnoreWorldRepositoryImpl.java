package com.objectt.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class IgnoreWorldRepositoryImpl implements IgnoreWorldRepository {
    private final Plugin plugin;
    private final File ignoreWorldsFile;
    private final Gson gson;
    private List<String> ignoreWorlds;
    
    public IgnoreWorldRepositoryImpl(Plugin plugin) {
        this.plugin = plugin;
        this.ignoreWorldsFile = new File(plugin.getDataFolder(), "homes/ignore_worlds.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.ignoreWorlds = new ArrayList<>();
        loadIgnoreWorlds();
    }
    
    @Override
    public void addIgnoreWorld(String worldName) {
        if (!ignoreWorlds.contains(worldName)) {
            ignoreWorlds.add(worldName);
            saveIgnoreWorlds();
        }
    }
    
    @Override
    public void removeIgnoreWorld(String worldName) {
        if (ignoreWorlds.remove(worldName)) {
            saveIgnoreWorlds();
        }
    }
    
    @Override
    public List<String> getIgnoreWorlds() {
        return new ArrayList<>(ignoreWorlds);
    }
    
    @Override
    public boolean isIgnoreWorld(String worldName) {
        return ignoreWorlds.contains(worldName);
    }
    
    @Override
    public void loadIgnoreWorlds() {
        if (!ignoreWorldsFile.exists()) {
            saveIgnoreWorlds();
            return;
        }
        
        try (FileReader reader = new FileReader(ignoreWorldsFile)) {
            Type listType = new TypeToken<List<String>>(){}.getType();
            List<String> loadedWorlds = gson.fromJson(reader, listType);
            if (loadedWorlds != null) {
                ignoreWorlds = loadedWorlds;
            }
        } catch (IOException e) {
            plugin.getLogger().warning("無視ワールドリスト読み込みエラー: " + e.getMessage());
        }
    }
    
    @Override
    public void saveIgnoreWorlds() {
        try {
            if (!ignoreWorldsFile.getParentFile().exists()) {
                ignoreWorldsFile.getParentFile().mkdirs();
            }
            
            try (FileWriter writer = new FileWriter(ignoreWorldsFile)) {
                gson.toJson(ignoreWorlds, writer);
            }
        } catch (IOException e) {
            plugin.getLogger().warning("無視ワールドリスト保存エラー: " + e.getMessage());
        }
    }
}