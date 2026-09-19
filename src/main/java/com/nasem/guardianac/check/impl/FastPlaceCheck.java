package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class FastPlaceCheck extends Check {

    private final long minDelay;
    private final int requiredStreak;

    public FastPlaceCheck(GuardianAC plugin) {
        super(plugin, CheckType.FASTPLACE);
        // ⭐ 200ms بين البلوكات = طبيعي
        // أقل من 60ms = FastPlace hack
        this.minDelay = plugin.getConfig().getLong("checks.fastplace.min-delay", 60);
        // ⭐ نطلب 5 ضربات سريعة متتالية قبل flag
        this.requiredStreak = plugin.getConfig().getInt("checks.fastplace.required-streak", 5);
    }

    /**
     * ⭐ من BlockPlaceEvent فقط — لما يوضع بلوك حقيقي
     */
    public void handle(Player player, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (player.isInsideVehicle()) return;

        long now = System.currentTimeMillis();
        long last = data.getLastBlockPlaceTime();

        data.setLastBlockPlaceTime(now);

        if (last == 0) return;

        long diff = now - last;

        // ⭐⭐⭐ نتحقق إذا كان سريع بشكل مستحيل
        if (diff < minDelay && diff > 0) {
            // ⭐ نزيد العداد
            int streak = data.getFastPlaceStreak() + 1;
            data.setFastPlaceStreak(streak);

            // ⭐⭐⭐ فقط إذا تكرر 5 مرات → flag
            if (streak >= requiredStreak) {
                flag(player, data, "streak=" + streak + " delay=" + diff + "ms");
                data.setFastPlaceStreak(0);
            }
        } else {
            // ⭐ إذا كان عادي — نصفّر العداد
            if (diff > 200) {
                data.setFastPlaceStreak(0);
            }
        }
    }

    public void handlePacket(Player player, PlayerData data, long diff) {
        // ما نستخدمها
    }
}
