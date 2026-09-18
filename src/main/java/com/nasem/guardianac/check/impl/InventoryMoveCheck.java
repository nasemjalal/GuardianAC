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

    // ⭐ قللناها — أي حركة > 0.05 = flag
    private static final double MIN_MOVE = 0.05;

    public InventoryMoveCheck(GuardianAC plugin) {
        super(plugin, CheckType.INVENTORYMOVE);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // ⭐ ما نفحص إلى إذا الإنفنتوري مفتوح
        if (!data.isInventoryOpen()) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;

        // ⭐ نقيس الحركة الأفقية
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        // ⭐⭐⭐ أي حركة > 0.05 = flag
        if (speed > MIN_MOVE) {
            flag(player, data, "move=" + MathUtil.round(speed, 3));
        }
    }
}
