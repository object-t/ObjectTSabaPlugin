package com.objectt.item;

import com.objectt.ObjectTSaba;
import com.objectt.data.dao.UserHome;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public class HomeItem {
    public static final NamespacedKey HOME_NAME;
    public static final NamespacedKey HOME_UNLOCK_AMOUNT;

    static {
        JavaPlugin plugin = JavaPlugin.getPlugin(ObjectTSaba.class);
        HOME_NAME = new NamespacedKey(plugin, "home");
        HOME_UNLOCK_AMOUNT = new  NamespacedKey(plugin, "home_unlock");
    }

    public static int getUnlockLevelAmount(int level) {
        return switch (level) {
            case 1 -> 1000;
            case 2 -> 2500;
            case 3 -> 5000;
            case 4 -> 10000;
            case 5 -> 25000;
            case 6 -> 50000;
            case 7 -> 100000;
            case 8 -> 250000;
            case 9 -> 1000000;
            default -> 0;
        };
    }

    public static ItemStack getUnlockItem(int level) {
        ItemStack unlockItem =  new ItemStack(Material.SPAWNER);
        ItemMeta meta = unlockItem.getItemMeta();

        meta.displayName(Component.text( "クリックでアンロック"));
        meta.lore(List.of(
                Component.text("料金: " + getUnlockLevelAmount(level), NamedTextColor.GOLD)
        ));

        PersistentDataContainer pdc =  meta.getPersistentDataContainer();
        pdc.set(HOME_UNLOCK_AMOUNT, PersistentDataType.INTEGER, level);
        unlockItem.setItemMeta(meta);

        return unlockItem;
    }

    public static ItemStack getBarrierItem() {
        ItemStack barrierItem =  new ItemStack(Material.BARRIER);
        ItemMeta meta = barrierItem.getItemMeta();
        meta.displayName(Component.text(" "));

        barrierItem.setItemMeta(meta);
        return barrierItem;
    }

    private static Material getMaterial(String worldName) {
        if (worldName.equalsIgnoreCase("world")) {
            return Material.GRASS_BLOCK;
        } else if (worldName.contains("nether")) {
            return Material.NETHERRACK;
        } else if (worldName.contains("end")) {
            return Material.END_STONE;
        }

        return Material.BEDROCK;
    }

    public static ItemStack getHomeItem(String name, UserHome.Home home) {
        ItemStack item = new ItemStack(getMaterial(home.location().getWorld().getName()));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(Component.text(name).color(TextColor.color(63, 232, 63)).decoration(TextDecoration.ITALIC, false));
        meta.lore(List.of(
                Component.text("クリックでテレポート").color(NamedTextColor.GRAY).decoration(TextDecoration.ITALIC, false),
                Component.text(""),
                Component.text("ワールド: ", NamedTextColor.GRAY)
                        .append(Component.text(home.location().getWorld().getName(), NamedTextColor.WHITE)),
                Component.text("座標: ", NamedTextColor.GRAY)
                        .append(Component.text(home.location().getBlockX() + " " + home.location().getBlockY() + " " + home.location().getBlockZ(), NamedTextColor.WHITE))
        ));

        PersistentDataContainer pcd = meta.getPersistentDataContainer();
        pcd.set(HOME_NAME, PersistentDataType.STRING, name);

        item.setItemMeta(meta);
        return item;
    }

    public static List<ItemStack> getHomeItems(UserHome home) {
        return home.homes().entrySet().stream()
                .map(h -> getHomeItem(h.getKey(), h.getValue()))
                .toList();
    }
}
