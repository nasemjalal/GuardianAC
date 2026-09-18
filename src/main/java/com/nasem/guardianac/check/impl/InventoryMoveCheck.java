package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InventoryMoveCheck extends Check {

    public InventoryMoveCheck(GuardianAC plugin) {
        super(plugin, CheckType.INVENTORYMOVE);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // ⭐⭐⭐ إذا الإنفنتوري ما مفتوح — نتجاهل
        if (!data.isInventoryOpen()) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;

        // ⭐ ما نفحص الطيران
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isGliding()) return;

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        // ⭐ أي حركة > 0.03 = flag
        if (speed > 0.03) {
            flag(player, data, "move=" + MathUtil.round(speed, 3));
        }
    }
}
