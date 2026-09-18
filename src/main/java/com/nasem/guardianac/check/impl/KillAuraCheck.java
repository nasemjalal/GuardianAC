package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.List;

public class KillAuraCheck extends Check {

    private final double maxAngle;
    private final long minAttackDelay;

    public KillAuraCheck(GuardianAC plugin) {
        super(plugin, CheckType.KILLAURA);
        this.maxAngle = plugin.getConfig().getDouble("checks.killaura.max-angle", 65.0);
        this.minAttackDelay = plugin.getConfig().getLong("checks.killaura.min-attack-delay", 55);
    }

    /**
     * ⭐ packet-level من USE_ENTITY
     */
    public void handlePacket(Player attacker, PlayerData data, Entity target) {
        if (!enabled) return;

        try {
            GameMode gm = attacker.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
            if (attacker.isInsideVehicle()) return;

            Vector look = attacker.getEyeLocation().getDirection().normalize();
            Vector toTarget = target.getLocation().add(0, target.getHeight() / 2.0, 0)
                    .toVector().subtract(attacker.getEyeLocation().toVector()).normalize();

            double angle = MathUtil.angle(look, toTarget);

            if (angle > maxAngle) {
                flag(attacker, data, "packet angle=" + MathUtil.round(angle, 1) + "°");
                return;
            }

            long now = System.currentTimeMillis();
            long last = data.getLastAttackTime();

            if (last > 0) {
                long diff = now - last;
                if (diff < minAttackDelay && diff > 0) {
                    flag(attacker, data, "packet delay=" + diff + "ms");
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
     * ⭐ swing check — محسّن مع try/catch كامل
     */
    public void handleSwing(Player attacker, PlayerData data) {
        if (!enabled) return;

        try {
            GameMode gm = attacker.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
            if (attacker.isInsideVehicle()) return;

            // ⭐ نتأكد إن العالم محمّل
            if (attacker.getWorld() == null) return;
            if (!attacker.isOnline()) return;

            Vector look = attacker.getEyeLocation().getDirection().normalize();

            LivingEntity nearest = null;
            double nearestDist = 6.0;

            // ⭐ نستخدم try/catch حول getNearbyEntities
            List<Entity> nearby;
            try {
                nearby = attacker.getNearbyEntities(6, 6, 6);
            } catch (Exception e) {
                return;
            }

            for (Entity entity : nearby) {
                if (entity == null) continue;
                if (entity == attacker) continue;
                if (!(entity instanceof LivingEntity)) continue;
                if (!entity.isValid()) continue;

                try {
                    double dist = entity.getLocation().distance(attacker.getLocation());
                    if (dist < nearestDist) {
                        nearestDist = dist;
                        nearest = (LivingEntity) entity;
                    }
                } catch (Exception ignored) {}
            }

            if (nearest == null) return;

            Vector toTarget = nearest.getLocation().add(0, nearest.getHeight() / 2.0, 0)
                    .toVector().subtract(attacker.getEyeLocation().toVector()).normalize();

            double angle = MathUtil.angle(look, toTarget);

            if (angle > maxAngle + 20) {
                flag(attacker, data, "swing angle=" + MathUtil.round(angle, 1) + "°");
            }
        } catch (Exception ignored) {}
    }
}
