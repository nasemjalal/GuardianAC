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

    public KillAuraCheck(GuardianAC plugin) {
        super(plugin, CheckType.KILLAURA);
        this.maxAngle = plugin.getConfig().getDouble("checks.killaura.max-angle", 60.0);
        this.minAttackDelay = plugin.getConfig().getLong("checks.killaura.min-attack-delay", 55);
    }

    /**
     * ⭐ USE_ENTITY — نطبع debug في الكونسول
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

            // ⭐ DEBUG — نطبع في الكونسول عشان نشوف
            plugin.getLogger().info("[DEBUG KillAura] " + attacker.getName()
                    + " angle=" + MathUtil.round(angle, 1) + "° target=" + target.getType());

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
        } catch (Exception e) {
            plugin.getLogger().warning("[DEBUG KillAura Error] " + e.getMessage());
        }
    }

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

            plugin.getLogger().info("[DEBUG KillAura Bukkit] " + attacker.getName()
                    + " angle=" + MathUtil.round(angle, 1) + "°");

            if (angle > maxAngle) {
                flag(attacker, data, "bukkit angle=" + MathUtil.round(angle, 1) + "°");
            }
        } catch (Exception ignored) {}
    }

    public void handleSwing(Player attacker, PlayerData data) {
        // ما نفحص
    }
}
