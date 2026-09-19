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

    // ⭐ 5+ بلوكات في ثانية = Nuker
    private static final long WINDOW_MS = 1000;
    private static final int MAX_BLOCKS = 5;

    public NukerCheck(GuardianAC plugin) {
        super(plugin, CheckType.NUKER);
    }

    /**
     * ⭐⭐⭐ packet-level — الأهم
     * يحسب عدد START_DIGGING packets في الثانية
     */
    public void handlePacketDig(Player player, PlayerData data, Location blockLoc) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long windowStart = data.getBreakWindowStart();

        // ⭐ إذا مر أكثر من ثانية → نصفّر
        if (now - windowStart > WINDOW_MS) {
            data.setBreakWindowStart(now);
            data.setBreakCountInSecond(1);
            return;
        }

        // ⭐ نزيد العداد
        int count = data.getBreakCountInSecond() + 1;
        data.setBreakCountInSecond(count);

        // ⭐⭐⭐ إذا 5+ بلوكات في ثانية → Nuker
        if (count == MAX_BLOCKS) {
            flag(player, data, "nuker " + count + " blocks/sec");
            data.setLastNukerFlagTime(now);
            data.setBreakCountInSecond(0);
            data.setBreakWindowStart(now);
        }
    }

    /**
     * ⭐ Bukkit fallback — للإصدارات الأقدم
     */
    public void handle(Player player, PlayerData data, BlockBreakEvent event) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long windowStart = data.getBreakWindowStart();

        if (now - windowStart > WINDOW_MS) {
            data.setBreakWindowStart(now);
            data.setBreakCountInSecond(1);
            return;
        }

        int count = data.getBreakCountInSecond() + 1;
        data.setBreakCountInSecond(count);

        if (count >= MAX_BLOCKS) {
            flag(player, data, "bukkit nuker " + count);
            data.setLastNukerFlagTime(now);
            data.setBreakCountInSecond(0);
            data.setBreakWindowStart(now);
        }
    }

    public void handlePacket(Player player, PlayerData data, double dist, long timeDiff) {
        // ما نستخدمها الآن
    }
}
