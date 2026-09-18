package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TimerCheck extends Check {

    // Vanilla: 20 packet/ثانية للحركة
    // Timer hack: 25-30+ packet/ثانية
    private static final int VANILLA_PACKETS_PER_SEC = 20;
    private static final int MAX_PACKETS_PER_SEC = 24;

    public TimerCheck(GuardianAC plugin) {
        super(plugin, CheckType.TIMER);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;

        if (player.getPing() > 250) {
            data.setPacketWindowStart(System.currentTimeMillis());
            return;
        }

        long now = System.currentTimeMillis();
        long windowStart = data.getPacketWindowStart();

        if (now - windowStart >= 1000) {
            int packets = data.getPacketCount();

            // ⭐ عدد الباكتات في الثانية
            if (packets > MAX_PACKETS_PER_SEC) {
                flag(player, data, "packets/sec=" + packets);
            }

            data.setPacketWindowStart(now);
        }
    }
}
