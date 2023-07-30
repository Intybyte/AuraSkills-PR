package dev.aurelium.auraskills.bukkit.listeners;

import dev.aurelium.auraskills.bukkit.AuraSkills;
import dev.aurelium.auraskills.common.config.Option;
import dev.aurelium.auraskills.common.storage.sql.SqlStorageProvider;
import dev.aurelium.auraskills.common.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

public class PlayerJoinQuit implements Listener {

    private final AuraSkills plugin;

    public PlayerJoinQuit(AuraSkills plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (plugin.getStorageProvider() instanceof SqlStorageProvider) { // Handles MySQL storage
            if (plugin.configBoolean(Option.MYSQL_ALWAYS_LOAD_ON_JOIN) || !plugin.getUserManager().hasUser(player.getUniqueId())) {
                int loadDelay = plugin.configInt(Option.MYSQL_LOAD_DELAY);
                if (loadDelay == 0) {
                    // Load immediately
                    loadPlayerDataAsync(player);
                } else {
                    // Delay loading
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            loadPlayerDataAsync(player);
                        }
                    }.runTaskLater(plugin, loadDelay);
                }
            }
        } else { // Yaml storage
            if (!plugin.getUserManager().hasUser(player.getUniqueId())) {
                loadPlayerDataAsync(player);
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        User user = plugin.getUser(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    plugin.getStorageProvider().save(user);
                    plugin.getUserManager().removeUser(player.getUniqueId());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }

    private void loadPlayerDataAsync(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try {
                    User user = plugin.getStorageProvider().load(player.getUniqueId());
                    plugin.getUserManager().addUser(user);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(plugin);
    }


}
