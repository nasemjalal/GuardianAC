package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class FastBreakCheck extends Check {

    private final long minDelay;

    public FastBreakCheck(GuardianAC plugin) {
        super(plugin, CheckType.FASTBREAK);
        this.minDelay = plugin.getConfig().getLong("checks.fastbreak.min-delay", 100);
    }

    public void handle(Player player, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long last = data.getLastBlockBreakTime();

        data.setLastBlockBreakTime(now);

        if (last == 0) return;

        long diff = now - last;

        if (diff < minDelay && diff > 0) {
            flag(player, data, "bukkit delay=" + diff + "ms");
        }
    }

    public void handlePacket(Player player, PlayerData data, long diff) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        flag(player, data, "packet delay=" + diff + "ms < " + minDelay + "ms");
    }
}
