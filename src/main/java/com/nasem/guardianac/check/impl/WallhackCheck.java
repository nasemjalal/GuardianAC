package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.RayTraceResult;

public class WallhackCheck extends Check {

    public WallhackCheck(GuardianAC plugin) {
        super(plugin, CheckType.WALLHACK);
    }

    public void handle(Player attacker, Entity victim, PlayerData data) {
        if (!enabled) return;

        if (attacker.getAllowFlight()) return;
        if (attacker.isInsideVehicle()) return;

        // Cast a ray from attacker's eyes to the victim — if a solid block is in the way,
        // they shouldn't be able to attack
        var eye = attacker.getEyeLocation();
        var victimLoc = victim.getLocation().add(0, victim.getHeight() / 2.0, 0);
        var direction = victimLoc.toVector().subtract(eye.toVector()).normalize();
        double distance = eye.toVector().distance(victimLoc.toVector());

        RayTraceResult result = attacker.getWorld().rayTraceBlocks(
                eye, direction, distance, org.bukkit.FluidCollisionMode.NEVER, true);

        if (result != null && result.getHitBlock() != null) {
            // There's a solid block between attacker and victim
            flag(attacker, data, "hit through " + result.getHitBlock().getType());
        }
    }
}
