package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class JesusCheck extends Check {

    public JesusCheck(GuardianAC plugin) {
        super(plugin, CheckType.JESUS);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // Skip legit cases
        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;
        if (player.isSwimming()) return;

        // Is player on top of water? (standing on water surface)
        Location below = to.clone().subtract(0, 1, 0);
        Material belowType = below.getBlock().getType();

        // Check if player is standing just above water without being in it
        boolean aboveWater = isWater(belowType) || isWater(below.clone().subtract(0, 1, 0).getBlock().getType());

        if (aboveWater && !player.isInWater() && player.isOnGround()) {
            // Player is "standing" on water — Jesus hack
            flag(player, data, "onWater");
        }
    }

    private boolean isWater(Material mat) {
        return mat == Material.WATER || mat == Material.KELP
                || mat == Material.KELP_PLANT || mat == Material.SEAGRASS;
    }
}
