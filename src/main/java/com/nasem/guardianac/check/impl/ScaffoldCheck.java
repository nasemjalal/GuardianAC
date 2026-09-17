package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.Vector;

public class ScaffoldCheck extends Check {

    public ScaffoldCheck(GuardianAC plugin) {
        super(plugin, CheckType.SCAFFOLD);
    }

    public void handle(Player player, BlockPlaceEvent event, PlayerData data) {
        if (!enabled) return;

        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        // Check if placing block BELOW player (typical scaffold)
        Location blockLoc = event.getBlock().getLocation();
        Location playerLoc = player.getLocation();

        // Block must be below player (y less than player y - 1)
        if (blockLoc.getY() >= playerLoc.getY() - 1) return;

        // Where is the player looking?
        Vector look = player.getEyeLocation().getDirection().normalize();

        // Direction to the placed block
        Vector toBlock = blockLoc.clone().add(0.5, 0.5, 0.5).toVector()
                .subtract(player.getEyeLocation().toVector()).normalize();

        double angle = MathUtil.angle(look, toBlock);

        // If player placed a block below but is NOT looking at it → scaffold
        if (angle > 75.0) {
            flag(player, data, "angle=" + MathUtil.round(angle, 1) + "°");
        }
    }
}
