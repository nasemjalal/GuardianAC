package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NoSlowCheck extends Check {

    // ⭐ رفعنا الحد — لأن الأكل العادي يعطي حركة
    private final double maxSlowedSpeed = 0.25;

    public NoSlowCheck(GuardianAC plugin) {
        super(plugin, CheckType.NOSLOW);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // ========== تجاهل حالات كثيرة ==========

        // 1. GameMode
        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        // 2. طيران
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;

        // 3. ⭐⭐ مهم: نايم في السرير — طبيعي
        if (player.isSleeping()) return;

        // 4. ⭐ في الماء
        if (player.isInWater() || player.isInLava()) return;
        if (player.isSwimming()) return;

        // 5. ⭐ Net ضعيف
        if (player.getPing() > 200) return;

        // 6. ⭐ الهواء (قفز)
        if (!player.isOnGround()) return;
        if (data.getAirTicks() > 0) return;

        // 7. Knockback
        if (System.currentTimeMillis() - data.getLastVelocityTime() < 2000) return;

        // ========== الكشف الفعلي ==========

        boolean blocking = player.isBlocking();
        boolean eating = player.isHandRaised();

        if (!blocking && !eating) return;

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        // ⭐⭐ نتسامح أكثر: لازم السرعة > maxSlowedSpeed + 0.1
        if (speed > maxSlowedSpeed + 0.1) {
            flag(player, data, "speed=" + MathUtil.round(speed, 3)
                    + " type=" + (blocking ? "block" : "eat"));
        }
    }
}
