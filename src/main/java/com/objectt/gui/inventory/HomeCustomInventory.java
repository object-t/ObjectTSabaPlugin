package com.objectt.gui.inventory;

import com.objectt.data.dao.UserHome;
import com.objectt.item.HomeItem;
import net.kyori.adventure.text.Component;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.objectt.item.HomeItem.getUnlockItem;

public class HomeCustomInventory extends BaseCustomInventory {
    private final UserHome userHome;

    public HomeCustomInventory(UserHome userHome) {
        super(Component.text("ホーム"), 9);
        this.userHome = userHome;
    }

    @Override
    public @NotNull Inventory getInventory() {
        int i = 0;
        for (ItemStack item : HomeItem.getHomeItems(this.userHome)) {
            this.inventory.setItem(i++, item);
        }
        int level = this.userHome.level();
        if  (level < 9) {
            this.inventory.setItem(level, getUnlockItem(level));
        }

        for (i = this.userHome.level() + 1; i < inventory.getSize(); i++) {
            this.inventory.setItem(i, HomeItem.getBarrierItem());
        }
        return super.getInventory();
    }
}
