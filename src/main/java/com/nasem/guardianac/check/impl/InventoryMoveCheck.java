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

        // ⭐ نحسب الحركة أول
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        // ⭐ إذا الحركة ضعيفة — نتجاهل
        if (speed < 0.05) return;

        // ⭐ إذا الإنفنتوري ما مفتوح — نتجاهل
        if (!data.isInventoryOpen()) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isGliding()) return;

        // ⭐ flag
        flag(player, data, "move=" + MathUtil.round(speed, 3));
    }
}
