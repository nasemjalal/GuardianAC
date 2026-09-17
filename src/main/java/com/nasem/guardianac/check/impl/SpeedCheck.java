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

        // Skip if in creative / flying / gliding / riding
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;
        if (player.isSwimming()) return;

        // Skip if recently damaged (knockback)
        if (System.currentTimeMillis() - data.getLastVelocityTime() < 1500) return;

        // Skip if player teleported recently
        if (data.getLastLocation() == null) return;

        // Horizontal speed
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        // Skip if player is on ice/slime/etc (would increase speed legitimately)
        if (isOnSpecialBlock(to)) return;

        // Boost allowed speed if player has speed potion
        double allowed = maxSpeed;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.SPEED)) {
            int amp = player.getPotionEffect(org.bukkit.potion.PotionEffectType.SPEED).getAmplifier();
            allowed += 0.06 * (amp + 1);
        }

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
