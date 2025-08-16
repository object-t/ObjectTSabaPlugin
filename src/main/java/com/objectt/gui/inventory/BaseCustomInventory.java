package com.objectt.gui.inventory;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.jetbrains.annotations.NotNull;

public class BaseCustomInventory implements InventoryHolder {
    protected final Inventory inventory;

    public BaseCustomInventory(Component title, InventoryType type) {
        this.inventory = Bukkit.createInventory(this, type, title);
    }

    public BaseCustomInventory(Component title, int size) {
        this.inventory = Bukkit.createInventory(this, size, title);
    }


    @Override
    public @NotNull Inventory getInventory() {
        return this.inventory;
    }
}
