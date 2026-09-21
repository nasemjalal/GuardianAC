package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class TriggerBotCheck extends Check {

    public TriggerBotCheck(GuardianAC plugin) {
        super(plugin, CheckType.TRIGGERBOT);
    }

    private boolean debug() {
        return plugin.getConfig().getBoolean("checks.triggerbot.debug", false)
                || plugin.getConfig().getBoolean("checks.killaura.debug", false);
    }

    public void handleAttack(Player player, PlayerData data, long time) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        // 1) سرعة الضربات (من باكيتات الهجوم)
        if (time - data.getAttackWindowStart() >= 1000) {
            data.setAttackWindowStart(time);
            data.setAttackCount(0);
        }
        int count = data.getAttackCount() + 1;
        data.setAttackCount(count);

        int max = plugin.getConfig().getInt("checks.triggerbot.max-attacks-per-second", 16);
        if (count == max + 1) {
            flag(player, data, "attacks/s=" + count);
        }

        // 2) ضربة بدون swing
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            long lastSwing = data.getLastSwingTime();
            boolean swung = lastSwing >= time - 100;

            if (debug()) {
                plugin.getLogger().info("[TriggerBot] " + player.getName()
                        + " swingDiff=" + (lastSwing - time) + "ms"
                        + " attacks/s=" + data.getAttackCount()
                        + " => " + (swung ? "ok" : "FLAG"));
            }

            if (!swung) {
                flag(player, data, "attack-without-swing");
            }
        }, 4L);
    }
}
