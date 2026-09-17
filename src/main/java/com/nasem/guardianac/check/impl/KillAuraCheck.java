package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class KillAuraCheck extends Check {

    private final double maxAngle;

    public KillAuraCheck(GuardianAC plugin) {
        super(plugin, CheckType.KILLAURA);
        this.maxAngle = plugin.getConfig().getDouble("checks.killaura.max-angle", 90.0);
    }

    public void handle(Player attacker, Entity victim, PlayerData data) {
        if (!enabled) return;

        if (attacker.isInsideVehicle()) return;

        // Angle between where the player is looking and the victim
        Vector look = attacker.getEyeLocation().getDirection().normalize();
        Vector toVictim = victim.getLocation().add(0, victim.getHeight() / 2.0, 0)
                .toVector().subtract(attacker.getEyeLocation().toVector()).normalize();

        double angle = MathUtil.angle(look, toVictim);

        // If angle is too big → hitting something they're not looking at
        if (angle > maxAngle) {
            flag(attacker, data, "angle=" + MathUtil.round(angle, 1) + "°");
        }
    }
}
