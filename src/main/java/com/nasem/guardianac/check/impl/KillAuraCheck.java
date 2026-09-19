package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.function.Predicate;

public class KillAuraCheck extends Check {

    private final long minAttackDelay;

    public KillAuraCheck(GuardianAC plugin) {
        super(plugin, CheckType.KILLAURA);
        this.minAttackDelay = plugin.getConfig().getLong("checks.killaura.min-attack-delay", 55);
    }

    /**
     * ⭐⭐⭐ USE_ENTITY — Ray Trace Detection
     * يتحقق إذا خط النظر يمر من هيد بوكس الكيان
     */
    public void handlePacket(Player attacker, PlayerData data, Entity target) {
        if (!enabled) return;

        try {
            GameMode gm = attacker.getGameMode();
            if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
            if (attacker.isInsideVehicle()) return;

            // ⭐⭐⭐ Ray Trace — خط النظر
            Vector eye = attacker.getEyeLocation().toVector();
            Vector direction = attacker.getEyeLocation().getDirection().normalize();

            // ⭐ مسافة الضرب
            double maxRange = 5.0;

            // ⭐ نبحث عن أول كيان يقطعه خط النظر
            Predicate<Entity> filter = e -> e.getEntityId() == target.getEntityId();

            RayTraceResult result = attacker.getWorld().rayTraceEntities(
                    attacker.getEyeLocation(),
                    direction,
                    maxRange,
                    0.1,
                    filter
            );

            // ⭐⭐⭐ إذا خط النظر **ما مر** من الكيان = KillAura
            if (result == null || result.getHitEntity() == null) {
                flag(attacker, data, "no-look-at-hitbox");
                return;
            }

            // ⭐ تأكد إن اللي ضربه = اللي باصص له
            if (result.getHitEntity().getEntityId() != target.getEntityId()) {
                flag(attacker, data, "wrong-target");
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

            Vector eye = attacker.getEyeLocation().toVector();
            Vector direction = attacker.getEyeLocation().getDirection().normalize();

            Predicate<Entity> filter = e -> e.getEntityId() == victim.getEntityId();

            RayTraceResult result = attacker.getWorld().rayTraceEntities(
                    attacker.getEyeLocation(),
                    direction,
                    5.0,
                    0.1,
                    filter
            );

            if (result == null || result.getHitEntity() == null) {
                flag(attacker, data, "bukkit no-look-at-hitbox");
            }
        } catch (Exception ignored) {}
    }

    /**
     * ⭐ Swing — نتجاهله تماماً (لأن ما نعرف الكيان)
     */
    public void handleSwing(Player attacker, PlayerData data) {
        // ⭐ Swing بدون USE_ENTITY = مو KillAura (Nuker/FastBreak)
        // ما نفحص شي هنا
    }
}
