package com.objectt.listener;

import com.objectt.ObjectTSaba;
import com.objectt.data.dao.UserHome;
import com.objectt.economy.VaultManager;
import com.objectt.gui.inventory.HomeCustomInventory;
import com.objectt.item.HomeItem;
import com.objectt.repository.HomeRepository;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class InventoryClickListener implements Listener {
    private final HomeRepository homeRepository;
    private final Plugin plugin;

    public InventoryClickListener(HomeRepository homeRepository) {
        this.homeRepository = homeRepository;
        this.plugin = JavaPlugin.getPlugin(ObjectTSaba.class);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory inventory = event.getInventory();
        Player player = (Player) event.getWhoClicked();

        switch (inventory.getHolder()) {
            case HomeCustomInventory i -> {
                event.setCancelled(true);
                int rawSlot = event.getRawSlot();
                if (i.getInventory().getSize() <= rawSlot) {
                    return;
                }

                ItemStack item = event.getCurrentItem();
                if (item == null || !item.hasItemMeta()) {
                    return;
                }

                PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
                switch (item.getType()) {
                    // スポナー(アンロック)を選択
                    case Material.SPAWNER -> {
                        if (!pdc.has(HomeItem.HOME_UNLOCK_AMOUNT, PersistentDataType.INTEGER)) {
                            break;
                        }

                        int level = pdc.get(HomeItem.HOME_UNLOCK_AMOUNT, PersistentDataType.INTEGER);
                        double balance = VaultManager.getBalance(player);
                        int amount = HomeItem.getUnlockLevelAmount(level);
                        if (balance < amount) {
                            player.sendMessage(Component.text("残高が不足しています。",  NamedTextColor.RED));
                            player.playSound(player.getLocation(), Sound.BLOCK_ANVIL_LAND, 1, 1);
                            break;
                        }

                        VaultManager.takeMoney(player, amount);
                        UserHome userHome = this.homeRepository.findById(player.getUniqueId());
                        player.sendMessage(Component.text("Homeの枠を増やしました！ " + userHome.level() + "→" + (userHome.level() + 1),  NamedTextColor.GREEN));
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);

                        this.homeRepository.save(new UserHome(userHome.playerId(), userHome.level() + 1, userHome.homes()));
                    }
                    // 虚無を選択
                    case Material.AIR, Material.BARRIER -> {}
                    // ホームを選択
                    default -> {
                        if (!pdc.has(HomeItem.HOME_NAME, PersistentDataType.STRING)) {
                            break;
                        }
                        String name = pdc.get(HomeItem.HOME_NAME, PersistentDataType.STRING);
                        Location location = this.homeRepository.findById(player.getUniqueId()).getLocation(name);

                        player.teleport(location);

                        new BukkitRunnable() {
                            @Override
                            public void run() {
                                player.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
                                player.spawnParticle(Particle.END_ROD, location, 100, 0.1, 0.1, 0.1);
                                player.sendPlainMessage(Component.text(name + "にテレポートしました。", NamedTextColor.GREEN).content());
                            }
                        }.runTaskLater(plugin, 10L);
                    }
                }

                player.closeInventory();
            }
            case null,
            default -> {}
        }
    }
}
