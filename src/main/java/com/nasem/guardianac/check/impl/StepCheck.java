package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class StepCheck extends Check {

    private final double maxStep;

    public StepCheck(GuardianAC plugin) {
        super(plugin, CheckType.STEP);
        this.maxStep = plugin.getConfig().getDouble("checks.step.max-step", 0.7);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;
        if (player.hasPotionEffect(org.bukkit.potion.PotionEffectType.JUMP_BOOST)) return;

        double dy = to.getY() - from.getY();

        // Only check upward movement when on ground (jumping/stepping)
        if (dy > maxStep && dy < 1.5 && player.isOnGround()) {
            // Ignore if standing on slab/stairs (block below is half block)
            Location below = from.clone().subtract(0, 1, 0);
            String belowName = below.getBlock().getType().name();
            if (belowName.contains("SLAB") || belowName.contains("STAIRS")
                    || belowName.contains("STEP") || belowName.contains("CARPET")) {
                return;
            }

            flag(player, data, "dy=" + MathUtil.round(dy, 3));
        }
    }
}
