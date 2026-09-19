package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class NukerCheck extends Check {

    private static final long WINDOW_MS = 1000;
    private static final int MAX_BLOCKS = 5;

    public NukerCheck(GuardianAC plugin) {
        super(plugin, CheckType.NUKER);
    }

    public void handle(Player player, PlayerData data, BlockBreakEvent event) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        // ⭐⭐⭐ نتجاهل أي أداة مطوّرة
        if (hasEnchantedTool(player)) return;

        long now = System.currentTimeMillis();
        long windowStart = data.getBreakWindowStart();

        if (now - windowStart > WINDOW_MS) {
            data.setBreakWindowStart(now);
            data.setBreakCountInSecond(1);
            return;
        }

        int count = data.getBreakCountInSecond() + 1;
        data.setBreakCountInSecond(count);

        if (count >= MAX_BLOCKS) {
            flag(player, data, "nuker " + count + " blocks/sec");
            data.setLastNukerFlagTime(now);
            data.setBreakCountInSecond(0);
            data.setBreakWindowStart(now);
        }
    }

    public void handlePacket(Player player, PlayerData data, double dist, long timeDiff) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (hasEnchantedTool(player)) return;

        if (dist > 3.0) {
            flag(player, data, "nuker dist=" + Math.round(dist * 10) / 10.0);
            data.setLastNukerFlagTime(System.currentTimeMillis());
        }
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
