package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;

public class NukerCheck extends Check {

    // ⭐ عدد البلوكات في آخر ثانية
    private static final long WINDOW_MS = 1000;
    private static final int MAX_BREAKS = 5;

    public NukerCheck(GuardianAC plugin) {
        super(plugin, CheckType.NUKER);
    }

    public void handle(Player player, PlayerData data, BlockBreakEvent event) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long last = data.getLastBlockBreakTime();

        Location current = event.getBlock().getLocation();
        Location lastLoc = data.getLastBlockBreakLocation();

        // ⭐ 1. كسر متتالي في وقت قصير
        if (last > 0) {
            long diff = now - last;

            if (diff < 200 && lastLoc != null) {
                double dist = current.distance(lastLoc);

                // ⭐ بلوكين متباعدين > 2 في وقت < 200ms = Nuker
                if (dist > 2.0) {
                    flag(player, data, "nuker dist=" + MathUtil.round(dist, 2)
                            + " time=" + diff + "ms");
                    return;
                }

                // ⭐ بلوكين متتاليين — نزيد العداد
                data.addViolation(CheckType.NUKER);

                if (data.getViolation(CheckType.NUKER) >= MAX_BREAKS) {
                    flag(player, data, "nuker rapid=" + data.getViolation(CheckType.NUKER) + " breaks");
                    data.setViolation(CheckType.NUKER, 0);
                }
            } else {
                // ⭐ انتهت النافذة — نصفّر إذا مر وقت
                if (diff > 2000) {
                    data.setViolation(CheckType.NUKER, 0);
                }
            }
        }

        data.setLastBlockBreakTime(now);
        data.setLastBlockBreakLocation(current);
    }
}
