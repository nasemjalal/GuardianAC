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
    // ⭐ زاوية تغيير النظر المفاجئ
    private final float maxYawChange = 90.0f;

    public KillAuraCheck(GuardianAC plugin) {
        super(plugin, CheckType.KILLAURA);
        this.maxAngle = plugin.getConfig().getDouble("checks.killaura.max-angle", 75.0);
        this.minAttackDelay = plugin.getConfig().getLong("checks.killaura.min-attack-delay", 55);
    }

    /**
     * ⭐ packet-level — USE_ENTITY
     */
    public void handlePacket(Player attacker, PlayerData data, Entity target) {
        if (!enabled) return;

        try {
            GameMode gm = attacker.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
            if (attacker.isInsideVehicle()) return;

            // ⭐⭐⭐ فحص Silent Aim — النظر قفز فجأة
            float yawDiff = Math.abs(data.getCurrentYaw() - data.getPreviousYaw());
            // نتعامل مع wrap-around (0° و 360°)
            if (yawDiff > 180) yawDiff = 360 - yawDiff;

            if (yawDiff > maxYawChange) {
                flag(attacker, data, "silent-aim yaw jump=" + MathUtil.round(yawDiff, 1) + "°");
                return;
            }

            // ⭐ فحص الزاوية العادي
            Vector look = attacker.getEyeLocation().getDirection().normalize();
            Vector toTarget = target.getLocation().add(0, target.getHeight() / 2.0, 0)
                    .toVector().subtract(attacker.getEyeLocation().toVector()).normalize();

            double angle = MathUtil.angle(look, toTarget);

            if (angle > maxAngle) {
                flag(attacker, data, "angle=" + MathUtil.round(angle, 1) + "°");
                return;
            }

            // ⭐ فحص سرعة الضرب
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

            // ⭐ Silent aim
            float yawDiff = Math.abs(data.getCurrentYaw() - data.getPreviousYaw());
            if (yawDiff > 180) yawDiff = 360 - yawDiff;

            if (yawDiff > maxYawChange) {
                flag(attacker, data, "bukkit silent-aim yaw=" + MathUtil.round(yawDiff, 1) + "°");
                return;
            }

            // ⭐ زاوية
            Vector look = attacker.getEyeLocation().getDirection().normalize();
            Vector toTarget = victim.getLocation().add(0, victim.getHeight() / 2.0, 0)
                    .toVector().subtract(attacker.getEyeLocation().toVector()).normalize();

            double angle = MathUtil.angle(look, toTarget);

            if (angle > maxAngle) {
                flag(attacker, data, "bukkit angle=" + MathUtil.round(angle, 1) + "°");
            }
        } catch (Exception ignored) {}
    }

    /**
     * ⭐ swing check
     */
    public void handleSwing(Player attacker, PlayerData data) {
        if (!enabled) return;

        try {
            GameMode gm = attacker.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
            if (attacker.isInsideVehicle()) return;

            // ⭐ Silent aim — حتى في swing
            float yawDiff = Math.abs(data.getCurrentYaw() - data.getPreviousYaw());
            if (yawDiff > 180) yawDiff = 360 - yawDiff;

            if (yawDiff > maxYawChange) {
                flag(attacker, data, "swing silent-aim yaw=" + MathUtil.round(yawDiff, 1) + "°");
            }
        } catch (Exception ignored) {}
    }
}
