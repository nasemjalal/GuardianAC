package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class FlightCheck extends Check {

    private final int maxAirTicks;

    public FlightCheck(GuardianAC plugin) {
        super(plugin, CheckType.FLIGHT);
        this.maxAirTicks = plugin.getConfig().getInt("checks.flight.max-air-ticks", 15);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // ========== تجاهل حالات كثيرة ==========

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;

        // ⭐⭐ مهم: نفحص الماء/الحمم بدقة (مو بس isInWater/isInLava)
        if (player.isSwimming()) return;
        if (isInLiquidAround(to)) return;

        // بوشن
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION)) return;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING)) return;

        // Knockback
        if (System.currentTimeMillis() - data.getLastVelocityTime() < 2000) return;

        // ⭐ Net ضعيف
        if (player.getPing() > 200) return;

        // ========== الكشف الفعلي ==========

        int airTicks = data.getAirTicks();
        double dy = to.getY() - from.getY();

        // ⭐⭐ في النيثر: السقف 128 — نفحص إذا يرتفع بسرعة
        World world = player.getWorld();
        boolean isNether = world.getEnvironment() == World.Environment.NETHER;

        if (airTicks > maxAirTicks) {
            // 1. يرتفع لفوق (dy > 0.05) = طيران
            if (dy > 0.05) {
                flag(player, data, "airTicks=" + airTicks
                        + " dy=" + MathUtil.round(dy, 3)
                        + " world=" + world.getEnvironment().name());
                return;
            }

            // 2. ثابت في الهواء (hover)
            if (Math.abs(dy) < 0.01 && airTicks > maxAirTicks * 2) {
                flag(player, data, "hover airTicks=" + airTicks);
                return;
            }

            // ⭐ 3. في النيثر: airTicks أكثر من 30 = طيران
            if (isNether && airTicks > 30) {
                flag(player, data, "nether airTicks=" + airTicks
                        + " dy=" + MathUtil.round(dy, 3));
            }
        }
    }

    /**
     * ⭐ فحص السوائل حول اللاعب بدقة (5 مواقع)
     */
    private boolean isInLiquidAround(Location loc) {
        if (loc.getWorld() == null) return false;

        Location[] checks = {
                loc.clone(),
                loc.clone().add(0, 1, 0),
                loc.clone().add(0, 0.5, 0),
                loc.clone().subtract(0, 1, 0),
                loc.clone().add(0, -0.5, 0)
        };

        for (Location check : checks) {
            String n = check.getBlock().getType().name();
            if (n.equals("WATER") || n.equals("LAVA")
                    || n.equals("KELP") || n.equals("KELP_PLANT")
                    || n.equals("SEAGRASS") || n.equals("BUBBLE_COLUMN")) {
                return true;
            }
        }
        return false;
    }
}
