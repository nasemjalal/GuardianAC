package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NoFallCheck extends Check {

    // ⭐ أقل ارتفاع للسقوط يعتبر مشبوه
    private static final double MIN_FALL_DISTANCE = 3.0;

    public NoFallCheck(GuardianAC plugin) {
        super(plugin, CheckType.NOFALL);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // ========== تجاهل الحالات الطبيعية ==========

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;
        if (player.isInWater() || player.isInLava()) return;
        if (player.isSwimming()) return;

        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING)) return;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION)) return;

        // ========== المنطق الأساسي ==========

        // 1. اللاعب لازم يكون نازل (dy < 0)
        double dy = to.getY() - from.getY();
        if (dy >= 0) return;

        // 2. اللاعب كان في الهواء (airTicks > 3)
        int airTicks = data.getAirTicks();
        if (airTicks < 3) return;

        // 3. ⭐ نحصل على fallDistance الحالية
        float fallDistance = player.getFallDistance();

        // 4. ⭐ إذا اللاعب نزل بلوكات كثيرة لكن fallDistance == 0
        //    (معناها NoFall hack)

        // نستخدم lastGroundLocation لتتبع الارتفاع
        Location lastGround = data.getLastGroundLocation();

        if (lastGround != null) {
            double dropFromGround = lastGround.getY() - to.getY();

            // ⭐ إذا نزل من مكان عالي (3+ بلوكات) + fallDistance == 0
            if (dropFromGround > MIN_FALL_DISTANCE && fallDistance == 0) {
                flag(player, data, "drop=" + MathUtil.round(dropFromGround, 1)
                        + " fallDist=0 airTicks=" + airTicks);
                return;
            }

            // ⭐ أو fallDistance صغيرة جداً (أقل من المتوقع)
            //    مقارنة مع مسافة السقوط الحقيقية
            if (dropFromGround > MIN_FALL_DISTANCE
                    && fallDistance < dropFromGround * 0.3) {
                flag(player, data, "drop=" + MathUtil.round(dropFromGround, 1)
                        + " fallDist=" + MathUtil.round(fallDistance, 1));
            }
        }
    }
}
