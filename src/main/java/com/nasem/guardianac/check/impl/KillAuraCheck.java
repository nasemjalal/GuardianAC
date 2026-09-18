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
        this.minAttackDelay = plugin.getConfig().getLong("checks.killaura.min-attack-delay", 50);
    }

    public void handlePacket(Player attacker, PlayerData data, Entity target) {
        if (!enabled) return;

        GameMode gm = attacker.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (attacker.isInsideVehicle()) return;

        // ⭐ 1. فحص الزاوية — أي زاوية > 60° = KillAura
        Vector look = attacker.getEyeLocation().getDirection().normalize();
        Vector toTarget = target.getLocation().add(0, target.getHeight() / 2.0, 0)
                .toVector().subtract(attacker.getEyeLocation().toVector()).normalize();

        double angle = MathUtil.angle(look, toTarget);

        if (angle > maxAngle) {
            flag(attacker, data, "angle=" + MathUtil.round(angle, 1) + "°");
            return;
        }

        // ⭐ 2. فحص سرعة الضرب
        long now = System.currentTimeMillis();
        long last = data.getLastAttackTime();

        if (last > 0) {
            long diff = now - last;
            if (diff < minAttackDelay && diff > 0) {
                flag(attacker, data, "delay=" + diff + "ms");
                return;
            }
        }

        data.setLastAttackTime(now);
    }

    public void handle(Player attacker, Entity victim, PlayerData data) {
        if (!enabled) return;

        GameMode gm = attacker.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (attacker.isInsideVehicle()) return;

        // ⭐ فحص الزاوية (Bukkit fallback)
        Vector look = attacker.getEyeLocation().getDirection().normalize();
        Vector toTarget = victim.getLocation().add(0, victim.getHeight() / 2.0, 0)
                .toVector().subtract(attacker.getEyeLocation().toVector()).normalize();

        double angle = MathUtil.angle(look, toTarget);

        if (angle > maxAngle) {
            flag(attacker, data, "bukkit angle=" + MathUtil.round(angle, 1) + "°");
        }
    }
}
