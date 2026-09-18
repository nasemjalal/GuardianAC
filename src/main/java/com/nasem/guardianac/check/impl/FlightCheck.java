package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FlightCheck extends Check {

    private final int maxAirTicks;

    public FlightCheck(GuardianAC plugin) {
        super(plugin, CheckType.FLIGHT);
        this.maxAirTicks = plugin.getConfig().getInt("checks.flight.max-air-ticks", 8);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;
        if (player.isSwimming()) return;
        if (player.isInWater() || player.isInLava()) return;
        if (isInLiquidAround(to)) return;

        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION)) return;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING)) return;

        if (System.currentTimeMillis() - data.getLastVelocityTime() < 2000) return;
        if (player.getPing() > 250) return;

        int airTicks = data.getAirTicks();
        double dy = to.getY() - from.getY();

        // ⭐⭐⭐ أي airTicks > 8 + يرتفع = flag مباشر
        if (airTicks > maxAirTicks) {
            // يرتفع
            if (dy > 0) {
                flag(player, data, "rise airTicks=" + airTicks
                        + " dy=" + MathUtil.round(dy, 3));
                return;
            }
            // ثابت (hover) — أي dy صغير
            if (Math.abs(dy) < 0.05 && airTicks > maxAirTicks + 3) {
                flag(player, data, "hover airTicks=" + airTicks);
                return;
            }
        }
    }

    private boolean isInLiquidAround(Location loc) {
        if (loc.getWorld() == null) return false;
        Location[] checks = {
                loc.clone(),
                loc.clone().add(0, 1, 0),
                loc.clone().subtract(0, 1, 0)
        };
        for (Location check : checks) {
            String n = check.getBlock().getType().name();
            if (n.equals("WATER") || n.equals("LAVA")) return true;
        }
        return false;
    }
}
