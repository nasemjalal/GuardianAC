package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NoFallCheck extends Check {

    private final double fallDistanceThreshold = 3.0;

    public NoFallCheck(GuardianAC plugin) {
        super(plugin, CheckType.NOFALL);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // Skip legit cases
        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;
        if (player.isInWater() || player.isInLava()) return;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SLOW_FALLING)) return;

        // If player fell more than threshold but didn't take fall damage
        // and suddenly is on ground with no damage — suspicious
        double fallDist = player.getFallDistance();

        // Player is falling fast (previous tick had fallDistance > threshold)
        // but this tick they're on ground with fallDistance = 0 — could be NoFall
        if (data.getLastLocation() != null && player.isOnGround()) {
            double prevDy = data.getLastLocation().getY() - to.getY();
            // If they suddenly stopped falling with high speed — suspicious
            if (prevDy < -0.5 && fallDist == 0 && data.getAirTicks() > 3) {
                flag(player, data, "prevDy=" + Math.round(prevDy * 100) / 100.0);
            }
        }
    }
}
