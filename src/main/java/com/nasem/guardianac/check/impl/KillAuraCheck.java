package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KillAuraCheck extends Check {

    private final double maxAngle;
    private final long minAttackDelay;

    public KillAuraCheck(GuardianAC plugin) {
        super(plugin, CheckType.KILLAURA);
        this.maxAngle = plugin.getConfig().getDouble("checks.killaura.max-angle", 90.0);
        this.minAttackDelay = plugin.getConfig().getLong("checks.killaura.min-attack-delay", 80);
    }

    /**
     * ⭐ الكشف الأساسي — packet-level
     *
     * KillAura يضرب أهداف بدون ما يبص لها، فيكون:
     * 1. زاوية النظر للضحية كبيرة جداً (> 90°)
     * 2. الضربات سريعة جداً (delay صغير)
     * 3. اتجاه النظر ثابت (ما يتحرك) — بينما اللاعب العادي يحرك الفأرة
     */
    public void handlePacket(Player attacker, PlayerData data, Entity target) {
        if (!enabled) return;

        // تجاهل الحالات الطبيعية
        if (attacker.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        if (attacker.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;
        if (attacker.isInsideVehicle()) return;

        // ========== 1. فحص الزاوية ==========
        Vector look = attacker.getEyeLocation().getDirection().normalize();
        Vector toTarget = target.getLocation().add(0, target.getHeight() / 2.0, 0)
                .toVector().subtract(attacker.getEyeLocation().toVector()).normalize();

        double angle = MathUtil.angle(look, toTarget);

        if (angle > maxAngle) {
            flag(attacker, data, "angle=" + MathUtil.round(angle, 1) + "° > " + maxAngle);
            return;
        }

        // ========== 2. فحص سرعة الضربات ==========
        long now = System.currentTimeMillis();
        long last = data.getLastAttackTime();

        if (last > 0) {
            long diff = now - last;
            if (diff < minAttackDelay && diff > 0) {
                flag(attacker, data, "delay=" + diff + "ms < " + minAttackDelay);
                return;
            }
        }

        data.setLastAttackTime(now);
        data.setLastAttackLocation(attacker.getLocation().clone());
    }

    /**
     * نسخة Bukkit — تبقى للتوافق
     */
    public void handle(Player attacker, Entity victim, PlayerData data) {
        // ما نستخدمها — تعتمد على packet-level
    }
}
