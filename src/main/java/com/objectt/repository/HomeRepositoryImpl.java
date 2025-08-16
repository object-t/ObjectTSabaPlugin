package com.objectt.repository;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.objectt.data.adapter.LocationAdapter;
import com.objectt.data.dao.UserHome;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.UUID;
import java.util.logging.Level;

public class HomeRepositoryImpl implements HomeRepository {
    private final JavaPlugin plugin;
    private final File folder;
    private final Gson gson;

    public HomeRepositoryImpl(JavaPlugin plugin) {
        this.plugin = plugin;
        this.folder = new File(plugin.getDataFolder(), "homes");
        if (!folder.exists()) {
            folder.mkdirs();
        }

        this.gson = new GsonBuilder()
                .registerTypeAdapter(Location.class, new LocationAdapter())
                .setPrettyPrinting()
                .create();
    }

    private File getHomeFile(UUID playerId) {
        return new File(folder, playerId +  ".json");
    }

    @Override
    public @NotNull UserHome findById(UUID id) {
        File file = getHomeFile(id);
        if (!file.exists()) {
            UserHome newUserHome = new UserHome(id, 1, new HashMap<>());
            save(newUserHome);
            return newUserHome;
        }

        try (FileReader reader = new FileReader(file)) {
            Type type = new TypeToken<UserHome>(){}.getType();
            UserHome userHome = gson.fromJson(reader, type);
            return userHome != null ? userHome : new UserHome(id, 1, new HashMap<>());
        } catch (IOException | JsonSyntaxException e) {
            plugin.getLogger().log(Level.WARNING, "Error reading homes file", e);
            return new UserHome(id, 1, new HashMap<>());
        }
    }

    @Override
    public void save(UserHome obj) {
        File file = getHomeFile(obj.playerId());
        try (FileWriter writer = new FileWriter(file)) {
            gson.toJson(obj, writer);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save UserHome for " + obj.playerId(), e);
        }
    }
}
