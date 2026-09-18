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

    // ⭐ أقصى مسافة بين البلوكات المكسورة في وقت قصير
    private static final double MAX_DISTANCE = 2.5;
    private static final long MAX_TIME = 200; // ms

    public NukerCheck(GuardianAC plugin) {
        super(plugin, CheckType.NUKER);
    }

    public void handle(Player player, PlayerData data, BlockBreakEvent event) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE) return;

        Location current = event.getBlock().getLocation();
        Location last = data.getLastBlockBreakLocation();

        if (last == null) return;

        long now = System.currentTimeMillis();
        long timeDiff = now - data.getLastBlockBreakTime();

        // ⭐ إذا كسر بلوك خلال وقت قصير جداً
        if (timeDiff > 0 && timeDiff < MAX_TIME) {
            double dist = current.distance(last);

            // ⭐ إذا البلوكين متباعدين > 2.5 بلوك = Nuker
            if (dist > MAX_DISTANCE) {
                flag(player, data, "dist=" + MathUtil.round(dist, 2)
                        + " in " + timeDiff + "ms");
                return;
            }

            // ⭐ كسر 3+ بلوكات في 200ms = Nuker
            long recentBreakKey = 0;
            // نحسب عدد البلوكات المكسورة مؤخراً
            if (data.getViolation(CheckType.NUKER) > 0) {
                // ⭐ نزيد العداد
                data.addViolation(CheckType.NUKER);
                if (data.getViolation(CheckType.NUKER) > 5) {
                    flag(player, data, "multi-break dist=" + MathUtil.round(dist, 2));
                }
            }
        }
    }
}
