package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.entity.Player;

public class AutoClickerCheck extends Check {

    private final double maxCps;
    // ⭐ حد أدنى مرتفع للانحراف (اللاعب العادي > 150)
    private final double minVariance;
    // ⭐ عدد الضربات المطلوبة
    private final int requiredClicks;

    public AutoClickerCheck(GuardianAC plugin) {
        super(plugin, CheckType.AUTOCLICKER);
        this.maxCps = plugin.getConfig().getDouble("checks.autoclicker.max-cps", 18.0);
        this.minVariance = plugin.getConfig().getDouble("checks.autoclicker.min-variance", 100.0);
        this.requiredClicks = plugin.getConfig().getInt("checks.autoclicker.required-clicks", 10);
    }

    public void handleAttack(Player player, PlayerData data) {
        if (!enabled) return;

        long now = System.currentTimeMillis();
        long lastClick = data.getLastEntityClickTime();

        // ⭐⭐⭐ 1. فحص CPS
        int cps = data.getClicksThisSecond();
        if (cps > maxCps) {
            flag(player, data, "cps=" + cps + " > " + maxCps);
            return;
        }

        // ⭐⭐⭐ 2. فحص Pattern (TriggerBot)
        if (lastClick > 0) {
            long interval = now - lastClick;

            if (interval > 20 && interval < 2000) {
                data.addClickInterval(interval);

                if (data.getClickIntervals().size() >= requiredClicks) {
                    double variance = data.getClickIntervalVariance();

                    // ⭐⭐⭐ الانحراف منخفض = TriggerBot
                    if (variance > 0 && variance < minVariance) {
                        flag(player, data, "pattern variance=" + Math.round(variance)
                                + " < " + minVariance);
                        data.getClickIntervals().clear();
                    }
                }
            }
        }

        data.setLastEntityClickTime(now);
    }

    public void handle(Player attacker, PlayerData data) {
        handleAttack(attacker, data);
    }
}
