package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class BlockReachCheck extends Check {

    private final double maxReach;

    public BlockReachCheck(GuardianAC plugin) {
        super(plugin, CheckType.BLOCKREACH);
        this.maxReach = plugin.getConfig().getDouble("checks.blockreach.max-reach", 4.5);
    }

    private boolean shouldSkip(PlayerData data, Player player) {
        if (data.isNukerActive()) return true;
        if (data.isFastBreakActive()) return true;
        if (hasEnchantedTool(player)) return true;
        return false;
    }

    public void handleBreak(Player player, BlockBreakEvent event, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (shouldSkip(data, player)) return;

        double distance = player.getEyeLocation()
                .distance(event.getBlock().getLocation().add(0.5, 0.5, 0.5));

        if (distance > maxReach) {
            flag(player, data, "break reach=" + MathUtil.round(distance, 2));
        }
    }

    public void handlePlace(Player player, BlockPlaceEvent event, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (shouldSkip(data, player)) return;

        double distance = player.getEyeLocation()
                .distance(event.getBlock().getLocation().add(0.5, 0.5, 0.5));

        if (distance > maxReach) {
            flag(player, data, "place reach=" + MathUtil.round(distance, 2));
        }
    }

    public void handlePacketBreak(Player player, PlayerData data, double reach) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (shouldSkip(data, player)) return;

        flag(player, data, "packet break reach=" + MathUtil.round(reach, 2));
    }

    public void handlePacketPlace(Player player, PlayerData data, double reach) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (shouldSkip(data, player)) return;

        flag(player, data, "packet place reach=" + MathUtil.round(reach, 2));
    }

    private boolean hasEnchantedTool(Player player) {
        try {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) return false;
            if (!item.getEnchantments().isEmpty()) return true;
            if (player.hasPotionEffect(PotionEffectType.HASTE)) return true;
            if (player.hasPotionEffect(PotionEffectType.CONDUIT_POWER)) return true;
        } catch (Exception ignored) {}
        return false;
    }
}
