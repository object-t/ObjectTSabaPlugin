package com.objectt.data.dao;

import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;

import java.util.Date;
import java.util.Map;
import java.util.UUID;

public record UserHome(UUID playerId, int level, @NotNull Map<String, Home> homes) {
    public Location getLocation(String locationName) {
        return homes.get(locationName).location();
    }

    public record Home(Location location, Date date) {
        public Home(Location location) {
            this(location, new Date());
        }
    }
}
