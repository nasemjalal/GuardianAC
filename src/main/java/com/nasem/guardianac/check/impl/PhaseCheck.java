package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class PhaseCheck extends Check {

    public PhaseCheck(GuardianAC plugin) {
        super(plugin, CheckType.PHASE);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isInsideVehicle()) return;
        if (player.getGameMode() == org.bukkit.GameMode.SPECTATOR) return;

        // If player teleported / respawned
        if (from.getWorld() != to.getWorld()) return;

        // Check if the destination block is solid (player inside a wall)
        Location toCheck = to.clone();
        // Check head and feet
        boolean feetSolid = toCheck.getBlock().getType().isSolid();
        boolean headSolid = toCheck.clone().add(0, 1, 0).getBlock().getType().isSolid();

        if (feetSolid && headSolid) {
            // Check if player was NOT in solid before
            boolean wasFeetSolid = from.getBlock().getType().isSolid();

            if (!wasFeetSolid) {
                flag(player, data, "inside solid block");
            }
        }
    }
}
