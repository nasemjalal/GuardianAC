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
    private final double sprintMultiplier;

    public SpeedCheck(GuardianAC plugin) {
        super(plugin, CheckType.SPEED);
        this.maxSpeed = plugin.getConfig().getDouble("checks.speed.max-speed", 0.5);
        this.sprintMultiplier = plugin.getConfig().getDouble("checks.speed.sprint-multiplier", 1.3);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // ========== تجاهل الحالات الطبيعية ==========

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;

        if (player.isSwimming()) return;
        if (player.isInWater() || player.isInLava()) return;
        if (isInWaterAround(to)) return;

        // ⭐ ping — نتجاهل فقط ping عالي جداً
        int ping = player.getPing();
        if (ping > 400) return;

        // ⭐ الهواء — نتجاهل
        if (!player.isOnGround()) return;
        if (data.getAirTicks() > 0) return;

        // ⭐ Knockback
        if (System.currentTimeMillis() - data.getLastVelocityTime() < 1500) return;

        // ⭐ بوشن
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) return;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST)) return;

        // ⭐ بلوكات خاصة
        if (isOnSpecialBlock(to)) return;

        // ⭐ التفاعل مع بلوك (فتح باب، زر)
        long lastInteract = data.getLastBlockPlaceTime();
        if (System.currentTimeMillis() - lastInteract < 300) return;

        // ========== الحساب الفعلي ==========

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        if (speed < 0.01) return;

        // ⭐ Sprint يعطي 30% زيادة
        double allowed = maxSpeed;
        if (player.isSprinting()) {
            allowed *= sprintMultiplier;
        }

        // ⭐⭐⭐ الهاك: السرعة تتجاوز بكثير
        // نتجاهل الفروق الصغيرة (0.03)
        if (speed > allowed + 0.03) {
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
