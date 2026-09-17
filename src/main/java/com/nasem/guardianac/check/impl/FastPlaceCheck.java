package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.entity.Player;

public class FastPlaceCheck extends Check {

    private final long minDelay;

    public FastPlaceCheck(GuardianAC plugin) {
        super(plugin, CheckType.FASTPLACE);
        this.minDelay = plugin.getConfig().getLong("checks.fastplace.min-delay", 100);
    }

    public void handle(Player player, PlayerData data) {
        if (!enabled) return;

        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long last = data.getLastBlockPlaceTime();

        // First place is always fine
        if (last == 0) return;

        long diff = now - last;

        // If diff is very small AND player is not spam-clicking (holding down)
        if (diff < minDelay && diff > 0) {
            flag(player, data, "delay=" + diff + "ms");
        }
    }
}
