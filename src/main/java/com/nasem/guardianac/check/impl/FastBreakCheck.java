package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffectType;

public class FastBreakCheck extends Check {

    private final long minDelay;

    public FastBreakCheck(GuardianAC plugin) {
        super(plugin, CheckType.FASTBREAK);
        this.minDelay = plugin.getConfig().getLong("checks.fastbreak.min-delay", 100);
    }

    public void handle(Player player, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (data.isNukerActive()) return;

        // ⭐⭐⭐ نتجاهل أي أداة مطوّرة (أي enchantment)
        if (hasEnchantedTool(player)) return;

        long now = System.currentTimeMillis();
        long last = data.getLastBlockBreakTime();

        data.setLastBlockBreakTime(now);

        if (last == 0) return;

        long diff = now - last;

        if (diff < minDelay && diff > 0) {
            flag(player, data, "delay=" + diff + "ms");
            data.setLastFastBreakFlagTime(now);
        }
    }

    public void handlePacket(Player player, PlayerData data, long diff) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (data.isNukerActive()) return;
        if (hasEnchantedTool(player)) return;

        flag(player, data, "packet delay=" + diff + "ms");
        data.setLastFastBreakFlagTime(System.currentTimeMillis());
    }

    /**
     * ⭐⭐⭐ فحص: هل الأداة فيها أي enchantment؟
     */
    private boolean hasEnchantedTool(Player player) {
        try {
            ItemStack item = player.getInventory().getItemInMainHand();
            if (item == null || item.getType() == Material.AIR) return false;

            // ⭐ إذا الأداة فيها أي enchantment → تجاهل
            if (!item.getEnchantments().isEmpty()) return true;

            // ⭐ Haste potion
            if (player.hasPotionEffect(PotionEffectType.HASTE)) return true;
            if (player.hasPotionEffect(PotionEffectType.CONDUIT_POWER)) return true;
        } catch (Exception ignored) {}
        return false;
    }
}
