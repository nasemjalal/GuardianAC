package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FlightCheck extends Check {

    private final int maxAirTicks;

    public FlightCheck(GuardianAC plugin) {
        super(plugin, CheckType.FLIGHT);
        this.maxAirTicks = plugin.getConfig().getInt("checks.flight.max-air-ticks", 20);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // ========== تجاهل حالات كثيرة ==========

        // 1. ⭐ كريتف / سبكتيتور — يقدر يطير بشكل شرعي
        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        // 2. طيران مسموح به (allow flight)
        if (player.getAllowFlight() || player.isFlying()) return;

        // 3. ⭐ طيران بالإليترا
        if (player.isGliding()) return;

        // 4. داخل مركبة
        if (player.isInsideVehicle()) return;

        // 5. ⭐ في الماء / الحمم (يشمل السباحة)
        if (player.isInWater() || player.isInLava()) return;

        // 6. ⭐ كان في الماء قبل لحظات (الخروج من الماء)
        Location belowFeet = to.clone().subtract(0, 1, 0);
        Location belowFeet2 = to.clone().subtract(0, 2, 0);
        if (isWaterOrLava(belowFeet) || isWaterOrLava(belowFeet2)) return;

        // 7. ⭐ وضع السباحة
        if (player.isSwimming()) return;

        // 8. ⭐ بوشن Levitation / Slow Falling
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION)) return;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING)) return;

        // 9. ⭐ أخذ knockback قريب (يصير في الهواء)
        if (System.currentTimeMillis() - data.getLastVelocityTime() < 2000) return;

        // 10. ⭐ القفز الطبيعي — إذا اللاعب يقفز من الأرض
        // نسمح بعدد air ticks أكبر بكثير قبل ما نعتبره طيران
        // القفز الطبيعي: ~12 tick في الهواء
        // مع lag: ~20 tick
        // مع قفز من مكان عالي: أكثر
        int airTicks = data.getAirTicks();
        int allowedAirTicks = maxAirTicks;

        // إذا اللاعب كان ينزل من مكان عالي (y أقل من قبل)، نسمح أكثر
        if (data.getLastGroundLocation() != null) {
            double dropFrom = data.getLastGroundLocation().getY() - to.getY();
            if (dropFrom > 2) {
                // يسقط من مكان عالي — طبيعي
                return;
            }
        }

        // ========== الكشف الفعلي ==========

        // الشرط: air ticks كثيرة + يرتفع لفوق (dy > 0)
        double dy = to.getY() - from.getY();

        // ⭐ يرتفع لفوق بدون سبب = طيران
        // لكن: نسمح بالقفز الطبيعي (dy صغير + airTicks معتدل)
        if (airTicks > allowedAirTicks && dy > 0.1) {
            flag(player, data, "airTicks=" + airTicks + " dy=" + Math.round(dy * 100) / 100.0);
        }
    }

    private boolean isWaterOrLava(Location loc) {
        if (loc.getWorld() == null) return false;
        org.bukkit.Material mat = loc.getBlock().getType();
        String n = mat.name();
        return n.equals("WATER") || n.equals("LAVA")
                || n.equals("KELP") || n.equals("KELP_PLANT")
                || n.equals("SEAGRASS") || n.equals("BUBBLE_COLUMN");
    }
}
