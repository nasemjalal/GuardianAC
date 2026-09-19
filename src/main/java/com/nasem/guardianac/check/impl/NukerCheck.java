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

    // ⭐ عدد البلوكات في الثانية
    private static final long WINDOW_MS = 1000;
    private static final int MAX_BLOCKS = 4;

    public NukerCheck(GuardianAC plugin) {
        super(plugin, CheckType.NUKER);
    }

    public void handle(Player player, PlayerData data, BlockBreakEvent event) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long last = data.getLastBlockBreakTime();

        // ⭐⭐⭐ الطريقة الجديدة: نعد كسرات في الثانية
        long lastActualBreak = data.getLastActualMoveTime();
        if (lastActualBreak == 0) {
            data.setLastActualMoveTime(now);
            data.setFastPlaceStreak(1);
            return;
        }

        long windowTime = now - lastActualBreak;

        // ⭐ إذا مر أكثر من ثانية → نصفّر
        if (windowTime > WINDOW_MS) {
            data.setLastActualMoveTime(now);
            data.setFastPlaceStreak(1);
            return;
        }

        // ⭐ نزيد العداد
        int count = data.getFastPlaceStreak() + 1;
        data.setFastPlaceStreak(count);

        // ⭐⭐⭐ إذا 4+ بلوكات في ثانية → flag
        if (count > MAX_BLOCKS) {
            flag(player, data, "nuker " + count + " blocks/sec");
            data.setFastPlaceStreak(0);
            data.setLastActualMoveTime(now);
        }
    }

    public void handlePacket(Player player, PlayerData data, double dist, long timeDiff) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        flag(player, data, "nuker dist=" + MathUtil.round(dist, 2)
                + " in " + timeDiff + "ms");
    }
}
