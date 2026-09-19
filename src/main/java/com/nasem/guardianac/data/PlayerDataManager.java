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

    public PlayerData get(Player player) {
        return dataMap.computeIfAbsent(player.getUniqueId(),
                uuid -> new PlayerData(uuid, player.getName()));
    }

    public PlayerData get(UUID uuid) {
        return dataMap.get(uuid);
    }

    public void remove(Player player) {
        dataMap.remove(player.getUniqueId());
    }

    private void startDecayTask() {
        // ⭐⭐ كل 3 ثواني — VL ينزل بسرعة
        decayTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (PlayerData data : dataMap.values()) {
                // ⭐ كل 3 ثواني بدون flag → VL ينزل 1
                data.decayViolations(3);
            }
        }, 20L * 3, 20L * 3);
    }

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
