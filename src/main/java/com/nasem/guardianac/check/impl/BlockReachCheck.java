package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockReachCheck extends Check {

    private final double maxReach;

    public BlockReachCheck(GuardianAC plugin) {
        super(plugin, CheckType.BLOCKREACH);
        this.maxReach = plugin.getConfig().getDouble("checks.blockreach.max-reach", 4.7);
    }

    public void handleBreak(Player player, BlockBreakEvent event, PlayerData data) {
        if (!enabled) return;

        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Location eye = player.getEyeLocation();
        Location blockCenter = event.getBlock().getLocation().add(0.5, 0.5, 0.5);

        double distance = eye.distance(blockCenter);

        if (distance > maxReach) {
            flag(player, data, "break reach=" + MathUtil.round(distance, 2));
        }
    }

    public void handlePlace(Player player, BlockPlaceEvent event, PlayerData data) {
        if (!enabled) return;

        if (player.getGameMode() == org.bukkit.GameMode.CREATIVE) return;

        Location eye = player.getEyeLocation();
        Location blockCenter = event.getBlock().getLocation().add(0.5, 0.5, 0.5);

        double distance = eye.distance(blockCenter);

        if (distance > maxReach) {
            flag(player, data, "place reach=" + MathUtil.round(distance, 2));
        }
    }
}
