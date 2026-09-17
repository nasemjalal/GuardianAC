package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class ReachCheck extends Check {

    private final double maxReach;

    public ReachCheck(GuardianAC plugin) {
        super(plugin, CheckType.REACH);
        this.maxReach = plugin.getConfig().getDouble("checks.reach.max-reach", 3.2);
    }

    public void handle(Player attacker, Entity victim, PlayerData data) {
        if (!enabled) return;

        if (attacker.getAllowFlight()) return;
        if (attacker.isInsideVehicle()) return;

        // Distance from attacker's eyes to victim's bounding box
        Vector attackerEye = attacker.getEyeLocation().toVector();
        Vector victimCenter = victim.getLocation().add(0, victim.getHeight() / 2.0, 0).toVector();

        double distance = attackerEye.distance(victimCenter);

        // Account for victim's hitbox size
        double hitboxRadius = Math.max(victim.getWidth(), victim.getHeight() / 2.0);
        double effective = distance - hitboxRadius;

        if (effective > maxReach) {
            flag(attacker, data, "reach=" + MathUtil.round(effective, 2) + " max=" + maxReach);
        }
    }
}
