package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.entity.Player;

public class AutoClickerCheck extends Check {

    private final double maxCps;

    public AutoClickerCheck(GuardianAC plugin) {
        super(plugin, CheckType.AUTOCLICKER);
        this.maxCps = plugin.getConfig().getDouble("checks.autoclicker.max-cps", 16.0);
    }

    public void handle(Player attacker, PlayerData data) {
        if (!enabled) return;

        int cps = data.getClicksThisSecond();

        if (cps > maxCps) {
            flag(attacker, data, "cps=" + MathUtil.round(cps, 1) + " max=" + maxCps);
        }
    }
}
