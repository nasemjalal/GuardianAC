package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockReachCheck extends Check {

    private final double maxReach;

    public BlockReachCheck(GuardianAC plugin) {
        super(plugin, CheckType.BLOCKREACH);
        this.maxReach = plugin.getConfig().getDouble("checks.blockreach.max-reach", 4.5);
    }

    public void handleBreak(Player player, BlockBreakEvent event, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        double distance = player.getEyeLocation()
                .distance(event.getBlock().getLocation().add(0.5, 0.5, 0.5));

        if (distance > maxReach) {
            flag(player, data, "bukkit break=" + MathUtil.round(distance, 2));
        }
    }

    public void handlePlace(Player player, BlockPlaceEvent event, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        double distance = player.getEyeLocation()
                .distance(event.getBlock().getLocation().add(0.5, 0.5, 0.5));

        if (distance > maxReach) {
            flag(player, data, "bukkit place=" + MathUtil.round(distance, 2));
        }
    }

    public void handlePacketBreak(Player player, PlayerData data, double reach) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        flag(player, data, "packet break reach=" + MathUtil.round(reach, 2)
                + " > " + maxReach);
    }

    public void handlePacketPlace(Player player, PlayerData data, double reach) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        flag(player, data, "packet place reach=" + MathUtil.round(reach, 2)
                + " > " + maxReach);
    }
}
