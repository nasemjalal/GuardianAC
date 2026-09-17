package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.entity.Player;

public class VelocityCheck extends Check {

    public VelocityCheck(GuardianAC plugin) {
        super(plugin, CheckType.VELOCITY);
    }

    public void handle(Player player, PlayerData data) {
        if (!enabled) return;

        // This is called when player takes damage
        // We expect the player to be knocked back
        data.setLastVelocityTime(System.currentTimeMillis());
        data.setPendingVelocity(true);
    }
}
