package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.entity.Player;

public class FastBreakCheck extends Check {

    private final long minDelay;

    public FastBreakCheck(GuardianAC plugin) {
        super(plugin, CheckType.FASTBREAK);
        this.minDelay = plugin.getConfig().getLong("checks.fastbreak.min-delay", 50);
    }

    public void handle(Player player, PlayerData data) {
        if (!enabled) return;

        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long last = data.getLastBlockBreakTime();

        if (last == 0) return;

        long diff = now - last;

        if (diff < minDelay && diff > 0) {
            flag(player, data, "delay=" + diff + "ms");
        }
    }
}
