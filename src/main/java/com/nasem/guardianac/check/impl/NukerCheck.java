package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class NukerCheck extends Check {

    private final int maxBlocksPerTick;

    public NukerCheck(GuardianAC plugin) {
        super(plugin, CheckType.NUKER);
        this.maxBlocksPerTick = plugin.getConfig().getInt("checks.nuker.max-blocks-per-tick", 3);
    }

    public void handle(Player player, PlayerData data, BlockBreakEvent event) {
        if (!enabled) return;

        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Location current = event.getBlock().getLocation();
        Location last = data.getLastBlockBreakLocation();

        if (last == null) return;

        // If breaking blocks that are far apart in a short time → Nuker
        long now = System.currentTimeMillis();
        long timeDiff = now - data.getLastBlockBreakTime();

        if (timeDiff > 0 && timeDiff < 100) {
            double blockDist = current.distance(last);
            // If blocks are > 3 apart within 100ms → suspicious
            if (blockDist > 3.0) {
                flag(player, data, "dist=" + Math.round(blockDist * 10) / 10.0
                        + " in " + timeDiff + "ms");
            }
        }
    }
}
