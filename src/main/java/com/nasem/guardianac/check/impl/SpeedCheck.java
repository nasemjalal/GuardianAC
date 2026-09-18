package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class SpeedCheck extends Check {

    private final double maxSpeed;

    public SpeedCheck(GuardianAC plugin) {
        super(plugin, CheckType.SPEED);
        this.maxSpeed = plugin.getConfig().getDouble("checks.speed.max-speed", 0.35);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // ========== تجاهل الحالات الطبيعية ==========
        
        // 1. طيران / كريتف / سبكتيتور
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;

        // 2. طيران بالإليترا
        if (player.isGliding()) return;

        // 3. داخل مركبة
        if (player.isInsideVehicle()) return;

        // 4. يسبح
        if (player.isSwimming()) return;

        // 5. في الماء / الحمم
        if (player.isInWater() || player.isInLava()) return;

        // ⭐ 6. مهم جداً: يتجاهل الحركة أثناء القفز
        // اللاعب لما يقفز أثناء الجري يصير أسرع بشكل طبيعي
        if (!player.isOnGround()) return;
        
        // ⭐ 7. إذا كان في الهواء خلال آخر ticks (قفز)
        if (data.getAirTicks() > 0) return;

        // 8. إذا أخذ ضرر / knockback قريب
        if (System.currentTimeMillis() - data.getLastVelocityTime() < 2000) return;

        // 9. تأثيرات البوشن اللي تغيّر السرعة
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
            return; // نتجاهل كامل — لأن البوشن يغير السرعة
        }
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST)) {
            return; // Jump boost يزيد السرعة
        }

        // 10. إذا كان على جليد / سلايم / بلوكات خاصة
        if (isOnSpecialBlock(to)) return;

        // ========== الحساب الفعلي ==========

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        // السماح بسرعة أعلى إذا كان يجري (sprint)
        double allowed = maxSpeed;
        if (player.isSprinting()) {
            allowed *= 1.3; // Sprint يعطي 30% زيادة
        }

        // إذا كان اللاعب على الأرض وثابت الحركة → تجاهل
        if (speed < 0.01) return;

        if (speed > allowed) {
            flag(player, data, "speed=" + MathUtil.round(speed, 3) + " max=" + MathUtil.round(allowed, 3));
        }
    }

    private boolean isOnSpecialBlock(Location loc) {
        if (loc.getWorld() == null) return false;
        org.bukkit.Material type = loc.clone().subtract(0, 1, 0).getBlock().getType();
        String n = type.name();
        return n.contains("ICE") || n.contains("SLIME") || n.contains("PACKED_ICE")
                || n.contains("BLUE_ICE") || n.contains("FROSTED_ICE");
    }
}
