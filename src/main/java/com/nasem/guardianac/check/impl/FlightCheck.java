package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class FlightCheck extends Check {

    private final int maxAirTicks;

    public FlightCheck(GuardianAC plugin) {
        super(plugin, CheckType.FLIGHT);
        this.maxAirTicks = plugin.getConfig().getInt("checks.flight.max-air-ticks", 20);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // Skip legit cases
        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;
        if (player.isSwimming()) return;
        if (player.isInWater() || player.isInLava()) return;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.LEVITATION)) return;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING)) return;

        // Recently took velocity
        if (System.currentTimeMillis() - data.getLastVelocityTime() < 1500) return;

        int airTicks = data.getAirTicks();

        // Check if player is going UP while in air for too long
        double dy = to.getY() - from.getY();

        if (airTicks > maxAirTicks && dy >= 0) {
            flag(player, data, "airTicks=" + airTicks + " dy=" + Math.round(dy * 100) / 100.0);
        }
    }
}
