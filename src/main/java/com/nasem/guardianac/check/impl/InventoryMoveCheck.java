package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InventoryMoveCheck extends Check {

    public InventoryMoveCheck(GuardianAC plugin) {
        super(plugin, CheckType.INVENTORYMOVE);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        if (!data.isInventoryOpen()) return;

        // Player shouldn't be able to move while inventory is open (vanilla)
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        // Allow small movement (server lag, minor adjustments)
        if (speed > 0.2) {
            flag(player, data, "speed=" + MathUtil.round(speed, 3));
        }
    }
}
