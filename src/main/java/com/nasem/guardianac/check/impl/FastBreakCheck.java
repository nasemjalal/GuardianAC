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
    private final int requiredStreak;

    public FastBreakCheck(GuardianAC plugin) {
        super(plugin, CheckType.FASTBREAK);
        // ⭐ 80ms — سرعة مستحيلة
        this.minDelay = plugin.getConfig().getLong("checks.fastbreak.min-delay", 80);
        // ⭐ نطلب 8 ضربات سريعة متتالية
        this.requiredStreak = plugin.getConfig().getInt("checks.fastbreak.required-streak", 8);
    }

    public void handle(Player player, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (data.isNukerActive()) return;

        // ⭐⭐⭐ نتجاهل إذا الأداة فيها أي enchantment
        if (hasEnchantedTool(player)) return;

        // ⭐⭐⭐ نتجاهل إذا اليد (ما فيها أداة)
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) return;

        long now = System.currentTimeMillis();
        long last = data.getLastBlockBreakTime();

        data.setLastBlockBreakTime(now);

        if (last == 0) return;

        long diff = now - last;

        // ⭐⭐⭐ إذا سريع → نزيد العداد
        if (diff < minDelay && diff > 0) {
            int streak = data.getFastPlaceStreak() + 1;
            data.setFastPlaceStreak(streak);

            // ⭐⭐⭐ فقط إذا 8+ متتالية → flag
            if (streak >= requiredStreak) {
                flag(player, data, "streak=" + streak + " delay=" + diff + "ms");
                data.setLastFastBreakFlagTime(now);
                data.setFastPlaceStreak(0);
            }
        } else {
            // ⭐ إذا كان عادي → نصفّر
            if (diff > 200) {
                data.setFastPlaceStreak(0);
            }
        }
    }

    public void handlePacket(Player player, PlayerData data, long diff) {
        // ما نستخدمها — نستخدم BlockBreakEvent
    }

    /**
     * ⭐ فحص: هل الأداة فيها أي enchantment؟
     */
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
