package com.nasem.guardianac.data;

import com.nasem.guardianac.GuardianAC;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PlayerDataManager {

    private final GuardianAC plugin;
    private final Map<UUID, PlayerData> dataMap = new ConcurrentHashMap<>();
    private BukkitTask decayTask;

    public PlayerDataManager(GuardianAC plugin) {
        this.plugin = plugin;
        startDecayTask();
    }

    /**
     * Get or create PlayerData for a player.
     */
    public PlayerData get(Player player) {
        return dataMap.computeIfAbsent(player.getUniqueId(),
                uuid -> new PlayerData(uuid, player.getName()));
    }

    /**
     * Get data by UUID (may be null).
     */
    public PlayerData get(UUID uuid) {
        return dataMap.get(uuid);
    }

    /**
     * Remove player data (on quit).
     */
    public void remove(Player player) {
        dataMap.remove(player.getUniqueId());
    }

    /**
     * Start a repeating task that decays violations.
     */
    private void startDecayTask() {
        decayTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PlayerData data : dataMap.values()) {
                // Use a general decay of 10 seconds
                data.decayViolations(10);
            }
        }, 20L * 10, 20L * 5); // Run every 5 seconds after initial 10 second delay
    }

    /**
     * Shutdown and cleanup.
     */
    public void shutdown() {
        if (decayTask != null) {
            decayTask.cancel();
            decayTask = null;
        }
        dataMap.clear();
    }

    public Map<UUID, PlayerData> getAllData() {
        return dataMap;
    }
}
