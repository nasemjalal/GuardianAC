package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;

public class FastPlaceCheck extends Check {

    private final long minDelay;

    public FastPlaceCheck(GuardianAC plugin) {
        super(plugin, CheckType.FASTPLACE);
        this.minDelay = plugin.getConfig().getLong("checks.fastplace.min-delay", 100);
    }

    /**
     * ⭐ فقط من BlockPlaceEvent — لما يوضع بلوك حقيقي
     */
    public void handle(Player player, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.CREATIVE) return;

        long now = System.currentTimeMillis();
        long last = data.getLastBlockPlaceTime();

        data.setLastBlockPlaceTime(now);

        if (last == 0) return;

        long diff = now - last;

        if (diff < minDelay && diff > 0) {
            flag(player, data, "delay=" + diff + "ms < " + minDelay + "ms");
        }
    }

    /**
     * ⭐⭐ packet-level — نستخدمها فقط كـ backup
     * بس نتحقق أول إن البلوك انوضع فعلاً
     */
    public void handlePacket(Player player, PlayerData data, long diff) {
        // ⭐⭐⭐ ما نستخدمها — نعتمد على BlockPlaceEvent
        // السبب: PLAYER_BLOCK_PLACEMENT packet يرسل حتى لو البلوك ما انوضع
    }
}
