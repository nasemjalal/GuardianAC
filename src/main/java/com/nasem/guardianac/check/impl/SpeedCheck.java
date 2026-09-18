package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class SpeedCheck extends Check {

    private final double maxSpeed;

    public SpeedCheck(GuardianAC plugin) {
        super(plugin, CheckType.SPEED);
        this.maxSpeed = plugin.getConfig().getDouble("checks.speed.max-speed", 0.35);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // ========== تجاهل حالات كثيرة ==========

        // 1. GameMode
        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        // 2. طيران / إليترا / مركبة
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;

        // 3. السباحة والماء
        if (player.isSwimming()) return;
        if (player.isInWater() || player.isInLava()) return;
        if (isInWaterAround(to)) return;

        // 4. ⭐ Net ضعيف — نتجاهل إذا ping > 200
        int ping = player.getPing();
        if (ping > 200) return;

        // 5. ⭐ Sprint طبيعي — نسمح بسرعة أعلى
        boolean sprinting = player.isSprinting();

        // 6. ⭐ الهواء (قفز)
        if (!player.isOnGround()) return;
        if (data.getAirTicks() > 0) return;

        // 7. Knockback حديث
        if (System.currentTimeMillis() - data.getLastVelocityTime() < 2000) return;

        // 8. بوشن
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) return;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST)) return;

        // 9. بلوكات خاصة (جليد، سلايم)
        if (isOnSpecialBlock(to)) return;

        // 10. ⭐⭐ مهم: إذا تفاعل مع بلوك حديث (فتح باب، زر، صندوق)
        // ننتظر 500ms قبل ما نفحصه
        long lastInteract = data.getLastBlockPlaceTime();
        if (System.currentTimeMillis() - lastInteract < 500) return;

        // ========== الحساب ==========

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        if (speed < 0.01) return;

        // ⭐ Sprint يسمح بسرعة أعلى (40% زيادة)
        double allowed = maxSpeed;
        if (sprinting) {
            allowed *= 1.4;
        }

        // ⭐ نتجاهل الفروق الصغيرة
        if (speed > allowed + 0.05) {
            flag(player, data, "speed=" + MathUtil.round(speed, 3)
                    + " max=" + MathUtil.round(allowed, 3)
                    + " ping=" + ping);
        }
    }

    private boolean isInWaterAround(Location loc) {
        if (loc.getWorld() == null) return false;
        Location[] checks = {
                loc.clone(),
                loc.clone().add(0, 1, 0),
                loc.clone().add(0, 0.5, 0),
                loc.clone().subtract(0, 1, 0)
        };
        for (Location check : checks) {
            if (isLiquid(check.getBlock().getType())) return true;
        }
        return false;
    }

    private boolean isLiquid(Material mat) {
        String n = mat.name();
        return n.equals("WATER") || n.equals("LAVA")
                || n.equals("KELP") || n.equals("KELP_PLANT")
                || n.equals("SEAGRASS") || n.equals("BUBBLE_COLUMN");
    }

    private boolean isOnSpecialBlock(Location loc) {
        if (loc.getWorld() == null) return false;
        Material type = loc.clone().subtract(0, 1, 0).getBlock().getType();
        String n = type.name();
        return n.contains("ICE") || n.contains("SLIME")
                || n.contains("PACKED_ICE") || n.contains("BLUE_ICE")
                || n.contains("FROSTED_ICE");
    }
}
