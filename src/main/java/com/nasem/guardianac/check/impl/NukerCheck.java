package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class NukerCheck extends Check {

    public NukerCheck(GuardianAC plugin) {
        super(plugin, CheckType.NUKER);
    }

    public void handle(Player player, PlayerData data, BlockBreakEvent event) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long last = data.getLastBlockBreakTime();

        if (last > 0 && (now - last) < 200) {
            if (data.getLastBlockBreakLocation() != null) {
                double dist = event.getBlock().getLocation()
                        .distance(data.getLastBlockBreakLocation());
                if (dist > 2.0) {
                    flag(player, data, "bukkit dist=" + MathUtil.round(dist, 2));
                }
            }
        }
    }

    public void handlePacket(Player player, PlayerData data, double dist, long timeDiff) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        flag(player, data, "nuker dist=" + MathUtil.round(dist, 2)
                + " in " + timeDiff + "ms");
    }
}
