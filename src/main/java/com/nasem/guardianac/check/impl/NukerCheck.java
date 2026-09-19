package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class NukerCheck extends Check {

    private static final long WINDOW_MS = 1000;

    // ⭐ حد للأداة العادية
    private static final int MAX_BLOCKS_NORMAL = 6;

    // ⭐⭐⭐ حد للأداة المطوّرة (Efficiency V) — أعلى بكثير
    private static final int MAX_BLOCKS_ENCHANTED = 12;

    public NukerCheck(GuardianAC plugin) {
        super(plugin, CheckType.NUKER);
    }

    /**
     * ⭐ packet-level — الأهم
     */
    public void handlePacketDig(Player player, PlayerData data, Location blockLoc) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long windowStart = data.getBreakWindowStart();

        if (now - windowStart > WINDOW_MS) {
            data.setBreakWindowStart(now);
            data.setBreakCountInSecond(1);
            return;
        }

        int count = data.getBreakCountInSecond() + 1;
        data.setBreakCountInSecond(count);

        // ⭐⭐⭐ الحد يعتمد على الأداة
        int threshold;
        if (hasEnchantedTool(player)) {
            threshold = MAX_BLOCKS_ENCHANTED; // 12
        } else {
            threshold = MAX_BLOCKS_NORMAL; // 6
        }

        if (count >= threshold) {
            flag(player, data, "nuker " + count + " blocks/sec (max=" + threshold + ")");
            data.setLastNukerFlagTime(now);
            data.setBreakCountInSecond(0);
            data.setBreakWindowStart(now);
        }
    }

    public void handle(Player player, PlayerData data, BlockBreakEvent event) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long windowStart = data.getBreakWindowStart();

        if (now - windowStart > WINDOW_MS) {
            data.setBreakWindowStart(now);
            data.setBreakCountInSecond(1);
            return;
        }

        int count = data.getBreakCountInSecond() + 1;
        data.setBreakCountInSecond(count);

        int threshold = hasEnchantedTool(player) ? MAX_BLOCKS_ENCHANTED : MAX_BLOCKS_NORMAL;

        if (count >= threshold) {
            flag(player, data, "nuker " + count + " (max=" + threshold + ")");
            data.setLastNukerFlagTime(now);
            data.setBreakCountInSecond(0);
            data.setBreakWindowStart(now);
        }
    }

    public void handlePacket(Player player, PlayerData data, double dist, long timeDiff) {
        // ما نستخدمها
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
