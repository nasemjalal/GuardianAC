package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class JesusCheck extends Check {

    public JesusCheck(GuardianAC plugin) {
        super(plugin, CheckType.JESUS);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        // ⭐ تجاهل الطيران
        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;

        // ⭐⭐⭐ الأهم — تجاهل إذا اللاعب في الماء أو يسبح
        if (player.isInWater()) return;
        if (player.isSwimming()) return;

        // ⭐⭐⭐ تجاهل إذا الماء حول اللاعب (3 بلوكات)
        if (isInWaterAround(to)) return;

        // ⭐ Knockback
        if (System.currentTimeMillis() - data.getLastVelocityTime() < 2000) return;

        // ⭐ ping
        if (player.getPing() > 200) return;

        // ⭐⭐⭐ المنطق الفعلي لـ Jesus:
        // اللاعب يقف على سطح الماء (بلوك الماء تحته = سطح الماء)
        // + ما في ماء حول جسمه
        // + على الأرض (isOnGround)

        if (!player.isOnGround()) return;

        // ⭐ نفحص إذا الماء تحته مباشرة
        Location below = to.clone().subtract(0, 1, 0);
        Material belowType = below.getBlock().getType();

        if (!isWater(belowType)) return;

        // ⭐⭐⭐ نتأكد: ما في ماء حول جسم اللاعب
        // (يعني واقف على سطح الماء، مو داخل الماء)
        Location checkBody = to.clone().add(0, 0.5, 0);
        Material bodyType = checkBody.getBlock().getType();

        if (isWater(bodyType)) return; // اللاعب داخل الماء — نتجاهل

        // ⭐⭐ ما نفحص إلى إذا اللاعب يقف على سطح الماء
        // + بطيء (ما يتحرك بسرعة)

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        // ⭐ Jesus hack: يقف على الماء + يتحرك بسرعة كبيرة
        // اللاعب العادي: يقف على الماء لكن يتحرك ببطء شديد

        // ⭐ نفحص إذا السرعة عالية (Jesus يعطي سرعة عادية على الماء)
        if (speed > 0.15) {
            flag(player, data, "on water speed=" + Math.round(speed * 1000) / 1000.0);
        }
    }

    /**
     * ⭐ فحص الماء حول اللاعب (5 مواقع)
     */
    private boolean isInWaterAround(Location loc) {
        if (loc.getWorld() == null) return false;

        Location[] checks = {
                loc.clone().add(0, 1, 0),      // رأس
                loc.clone().add(0, 0.5, 0),    // صدر
                loc.clone(),                    // قدم
                loc.clone().subtract(0, 1, 0), // تحت القدم
                loc.clone().subtract(0, 2, 0)  // أعمق
        };

        for (Location check : checks) {
            if (isWater(check.getBlock().getType())) {
                return true;
            }
        }
        return false;
    }

    private boolean isWater(Material mat) {
        String n = mat.name();
        return n.equals("WATER") || n.equals("KELP")
                || n.equals("KELP_PLANT") || n.equals("SEAGRASS")
                || n.equals("BUBBLE_COLUMN");
    }
}
