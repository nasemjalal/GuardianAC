package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class TimerCheck extends Check {

    private final int maxBalance;

    public TimerCheck(GuardianAC plugin) {
        super(plugin, CheckType.TIMER);
        this.maxBalance = plugin.getConfig().getInt("checks.timer.max-balance", 5);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        long now = System.currentTimeMillis();
        long last = data.getLastTimerCheck();

        if (last == 0) {
            data.setLastTimerCheck(now);
            return;
        }

        long elapsed = now - last;

        // Vanilla client sends ~50ms per tick
        // If it's consistently lower → Timer hack
        if (elapsed < 45) {
            data.addTimerBalance(1);
        } else if (elapsed > 55) {
            data.addTimerBalance(-1);
        }

        data.setLastTimerCheck(now);

        if (data.getTimerBalance() > maxBalance) {
            flag(player, data, "balance=" + data.getTimerBalance());
            data.setTimerBalance(0);
        } else if (data.getTimerBalance() < -maxBalance) {
            data.setTimerBalance(0);
        }
    }
}
