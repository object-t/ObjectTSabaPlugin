package com.objectt.repository;

import java.util.List;

public interface IgnoreWorldRepository {
    void addIgnoreWorld(String worldName);
    void removeIgnoreWorld(String worldName);
    List<String> getIgnoreWorlds();
    boolean isIgnoreWorld(String worldName);
    void loadIgnoreWorlds();
    void saveIgnoreWorlds();
}