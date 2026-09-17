package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class NoSlowCheck extends Check {

    // Max speed while using item/blocking/eating (should be much slower)
    private final double maxSlowedSpeed = 0.15;

    public NoSlowCheck(GuardianAC plugin) {
        super(plugin, CheckType.NOSLOW);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;

        // Check if player is "slowed" (blocking sword, eating, drinking, drawing bow)
        boolean blocking = player.isBlocking();
        boolean eating = player.isHandRaised(); // In 1.21 this works for eating/drinking/bow

        if (!blocking && !eating) return;

        // If slowed, speed should be very low. If it's normal → NoSlow hack
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        if (speed > maxSlowedSpeed) {
            flag(player, data, "speed=" + MathUtil.round(speed, 3) + " type=" + (blocking ? "block" : "eat"));
        }
    }
}
