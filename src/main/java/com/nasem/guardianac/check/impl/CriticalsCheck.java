package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.entity.Player;

public class CriticalsCheck extends Check {

    public CriticalsCheck(GuardianAC plugin) {
        super(plugin, CheckType.CRITICALS);
    }

    public void handle(Player attacker, PlayerData data) {
        if (!enabled) return;

        if (attacker.getAllowFlight() || attacker.isFlying()) return;
        if (attacker.isInsideVehicle()) return;
        if (attacker.isInWater() || attacker.isInLava()) return;
        if (attacker.isClimbing()) return;
        if (attacker.hasPotionEffect(org.bukkit.potion.PotionEffectType.BLINDNESS)) return;

        // Critical hit conditions in vanilla:
        // - falling (fallDistance > 0)
        // - not on ground
        // - not in water/lava
        // - not riding
        // - not on ladder/vine
        // - not with blindness
        // - not sprinting

        double fallDist = attacker.getFallDistance();
        boolean onGround = attacker.isOnGround();

        // A critical hit should only happen when falling AND not on ground
        // If player is on ground AND has 0 fall distance, they can't crit
        // But we can't directly check the crit — instead check that the crit hit happens
        // This check works by: if the player is on ground and had no fall → flag

        // Detect suspicious: on ground + 0 fall distance but they attacked
        // (this simple check assumes we're in the attack event already)
        if (onGround && fallDist == 0 && !attacker.isSprinting()) {
            // Might be legit — crits don't always happen
            // Only flag if repeated pattern (we rely on VL system)
            // Actually: only flag if fallDistance was suspicious
            // Comment: to reduce false positives, only flag when some indicator present
            // For now, skip — requires more packet info
        }
    }
}
