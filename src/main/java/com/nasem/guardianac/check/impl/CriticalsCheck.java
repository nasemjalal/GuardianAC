package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class CriticalsCheck extends Check {

    public CriticalsCheck(GuardianAC plugin) {
        super(plugin, CheckType.CRITICALS);
    }

    /**
     * Critical hit حقيقي يحتاج:
     * - اللاعب ينزل (fallDistance > 0)
     * - ما على الأرض
     * - ما في الماء
     * - ما يسبح
     * - ما يستخدم درع (blocking)
     *
     * Criticals hack: يعطي critical بدون سقوط
     */
    public void handle(Player attacker, PlayerData data) {
        if (!enabled) return;

        GameMode gm = attacker.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        if (attacker.getAllowFlight() || attacker.isFlying()) return;
        if (attacker.isGliding()) return;
        if (attacker.isInsideVehicle()) return;
        if (attacker.isInWater() || attacker.isInLava()) return;
        if (attacker.isSwimming()) return;
        if (attacker.isClimbing()) return;
        if (attacker.isBlocking()) return;

        if (attacker.hasPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS)) return;

        // ⭐ اللاعب ضرب — هل Critical صحيح؟
        boolean onGround = attacker.isOnGround();
        float fallDistance = attacker.getFallDistance();
        boolean sprinting = attacker.isSprinting();

        // Critical حقيقي: fallDistance > 0 AND NOT onGround AND NOT sprinting
        // Criticals hack: onGround AND fallDistance == 0 (لكن يعطي critical)

        // ⭐ نتحقق إذا اللاعب على الأرض + fallDistance 0 = ما يقدر يسوي critical
        if (onGround && fallDistance == 0 && !sprinting) {
            // ما نقدر نتحقق من الـ damage الفعلي من Bukkit API
            // لكن نستخدم pattern: تكرار الضربات بدون قفز

            long now = System.currentTimeMillis();
            long last = data.getLastAttackTime();

            if (last > 0 && (now - last) < 400) {
                // ⭐ ضربات سريعة + ما يقفز = Criticals hack
                int count = data.getViolation(CheckType.CRITICALS);

                // نسمح بعدد معين
                if (count >= 3) {
                    flag(attacker, data, "onGround fallDist=0 spam");
                } else {
                    // نزيد العداد بدون flag
                    data.addViolation(CheckType.CRITICALS);
                }
            }
        }
    }
}
