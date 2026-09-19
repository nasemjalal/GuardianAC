package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KillAuraCheck extends Check {

    private final double maxAngle;
    private final long minAttackDelay;
    private final float maxYawChange = 90.0f;

    public KillAuraCheck(GuardianAC plugin) {
        super(plugin, CheckType.KILLAURA);
        this.maxAngle = plugin.getConfig().getDouble("checks.killaura.max-angle", 75.0);
        this.minAttackDelay = plugin.getConfig().getLong("checks.killaura.min-attack-delay", 55);
    }

    /**
     * ⭐ USE_ENTITY — ضربة كيان
     */
    public void handlePacket(Player attacker, PlayerData data, Entity target) {
        if (!enabled) return;

        try {
            GameMode gm = attacker.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
            if (attacker.isInsideVehicle()) return;

            // ⭐ Silent Aim
            float yawDiff = Math.abs(data.getCurrentYaw() - data.getPreviousYaw());
            if (yawDiff > 180) yawDiff = 360 - yawDiff;

            if (yawDiff > maxYawChange) {
                flag(attacker, data, "silent-aim yaw=" + MathUtil.round(yawDiff, 1) + "°");
                return;
            }

            Vector look = attacker.getEyeLocation().getDirection().normalize();
            Vector toTarget = target.getLocation().add(0, target.getHeight() / 2.0, 0)
                    .toVector().subtract(attacker.getEyeLocation().toVector()).normalize();

            double angle = MathUtil.angle(look, toTarget);

            if (angle > maxAngle) {
                flag(attacker, data, "angle=" + MathUtil.round(angle, 1) + "°");
                return;
            }

            long now = System.currentTimeMillis();
            long last = data.getLastAttackTime();

            if (last > 0) {
                long diff = now - last;
                if (diff < minAttackDelay && diff > 0) {
                    flag(attacker, data, "fast attack=" + diff + "ms");
                    return;
                }
            }

            data.setLastAttackTime(now);
        } catch (Exception ignored) {}
    }

    /**
     * ⭐ Bukkit fallback
     */
    public void handle(Player attacker, Entity victim, PlayerData data) {
        if (!enabled) return;

        try {
            GameMode gm = attacker.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
            if (attacker.isInsideVehicle()) return;

            float yawDiff = Math.abs(data.getCurrentYaw() - data.getPreviousYaw());
            if (yawDiff > 180) yawDiff = 360 - yawDiff;

            if (yawDiff > maxYawChange) {
                flag(attacker, data, "silent-aim yaw=" + MathUtil.round(yawDiff, 1) + "°");
                return;
            }

            Vector look = attacker.getEyeLocation().getDirection().normalize();
            Vector toTarget = victim.getLocation().add(0, victim.getHeight() / 2.0, 0)
                    .toVector().subtract(attacker.getEyeLocation().toVector()).normalize();

            double angle = MathUtil.angle(look, toTarget);

            if (angle > maxAngle) {
                flag(attacker, data, "angle=" + MathUtil.round(angle, 1) + "°");
            }
        } catch (Exception ignored) {}
    }

    /**
     * ⭐⭐⭐ swing — الأهم (مع تجاهل كسر البلوكات)
     */
    public void handleSwing(Player attacker, PlayerData data) {
        if (!enabled) return;

        try {
            GameMode gm = attacker.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
            if (attacker.isInsideVehicle()) return;

            // ⭐⭐⭐ مهم: نتجاهل إذا اللاعب يكسر بلوك حالياً
            long now = System.currentTimeMillis();
            long lastBreak = data.getLastBlockBreakTime();

            // إذا كسر بلوك في آخر 500ms → هذا swing لكسر بلوك، مو ضرب
            if (lastBreak > 0 && (now - lastBreak) < 500) {
                return;
            }

            // ⭐ Silent Aim
            float yawDiff = Math.abs(data.getCurrentYaw() - data.getPreviousYaw());
            if (yawDiff > 180) yawDiff = 360 - yawDiff;

            if (yawDiff > maxYawChange) {
                flag(attacker, data, "swing silent-aim yaw=" + MathUtil.round(yawDiff, 1) + "°");
            }
        } catch (Exception ignored) {}
    }
}
