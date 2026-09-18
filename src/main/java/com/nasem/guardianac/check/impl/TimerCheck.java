package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TimerCheck extends Check {

    private final int maxBalance;

    // ⭐ تسامح كبير — لأن lag يسبب false positives
    private static final long MIN_TICK_MS = 35;  // قبل: 45
    private static final long MAX_TICK_MS = 65;  // قبل: 55

    public TimerCheck(GuardianAC plugin) {
        super(plugin, CheckType.TIMER);
        this.maxBalance = plugin.getConfig().getInt("checks.timer.max-balance", 5);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // تجاهل الحالات الطبيعية
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;

        // ⭐ تجاهل إذا اللاعب lag (ping > 300ms)
        int ping = player.getPing();
        if (ping > 300) {
            data.setTimerBalance(0);
            return;
        }

        long now = System.currentTimeMillis();
        long last = data.getLastTimerCheck();

        if (last == 0) {
            data.setLastTimerCheck(now);
            return;
        }

        long elapsed = now - last;

        // ⭐ نتجاهل القيم الشاذة (lag spikes)
        if (elapsed > 500 || elapsed < 10) {
            data.setLastTimerCheck(now);
            return;
        }

        // Vanilla: ~50ms per tick
        if (elapsed < MIN_TICK_MS) {
            data.addTimerBalance(1);
        } else if (elapsed > MAX_TICK_MS) {
            data.addTimerBalance(-1);
        }

        data.setLastTimerCheck(now);

        // ⭐ عتبة أعلى (10 بدل 5)
        int threshold = maxBalance * 2;
        if (data.getTimerBalance() > threshold) {
            flag(player, data, "balance=" + data.getTimerBalance() + " ping=" + ping);
            data.setTimerBalance(0);
        } else if (data.getTimerBalance() < -threshold) {
            data.setTimerBalance(0);
        }
    }
}
