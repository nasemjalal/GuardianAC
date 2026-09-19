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

    // ⭐ الزاوية القصوى — إذا نظر بعيد عن الهدف = KillAura
    private final double maxAngle;
    private final long minAttackDelay;

    public KillAuraCheck(GuardianAC plugin) {
        super(plugin, CheckType.KILLAURA);
        // ⭐ 60° — إذا الزاوية أكبر = ما يبص للهدف
        this.maxAngle = plugin.getConfig().getDouble("checks.killaura.max-angle", 60.0);
        this.minAttackDelay = plugin.getConfig().getLong("checks.killaura.min-attack-delay", 55);
    }

    /**
     * ⭐ USE_ENTITY — ضرب كيان
     * ⭐ نقيس الزاوية بين النظر والهدف
     */
    public void handlePacket(Player attacker, PlayerData data, Entity target) {
        if (!enabled) return;

        try {
            GameMode gm = attacker.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
            if (attacker.isInsideVehicle()) return;

            // ⭐ اتجاه النظر
            Vector look = attacker.getEyeLocation().getDirection().normalize();

            // ⭐ اتجاه الهدف (من العين)
            Vector toTarget = target.getLocation().add(0, target.getHeight() / 2.0, 0)
                    .toVector().subtract(attacker.getEyeLocation().toVector()).normalize();

            // ⭐ الزاوية
            double angle = MathUtil.angle(look, toTarget);

            // ⭐⭐⭐ إذا الزاوية كبيرة = ما يبص للهدف
            if (angle > maxAngle) {
                flag(attacker, data, "angle=" + MathUtil.round(angle, 1) + "° > " + maxAngle);
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
     * ⭐ Swing — ما نفحص شي (Nuker/FastBreak ما يطلعون KillAura)
     */
    public void handleSwing(Player attacker, PlayerData data) {
        // ⭐ نتجاهل Swing — KillAura يُفحص من USE_ENTITY فقط
    }
}
