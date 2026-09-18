package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class InvWalkCheck extends Check {

    // ⭐⭐ نافذة زمنية — إذا تحرك في آخر 500ms
    private static final long MOVE_WINDOW_MS = 500;
    // ⭐⭐ الحد الأدنى للسرعة — 0.1
    private static final double MIN_MOVE_SPEED = 0.1;

    public InvWalkCheck(GuardianAC plugin) {
        super(plugin, CheckType.INVENTORYMOVE);
    }

    /**
     * ⭐ من PlayerMoveEvent — يتحرك والإنفنتوري مفتوح
     */
    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isGliding()) return;

        if (!data.isInventoryOpen()) return;

        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        if (speed > 0.05) {
            flag(player, data, "move while open speed=" + MathUtil.round(speed, 3));
        }
    }

    /**
     * ⭐ swing — يضرب والإنفنتوري مفتوح
     */
    public void handleSwing(Player player, PlayerData data) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (!data.isInventoryOpen()) return;

        flag(player, data, "swing while inventory open");
    }

    /**
     * ⭐⭐⭐ الأهم — InventoryClick
     * اللاعب حرّك شي في الإنفنتوري + يتحرك = InvWalk
     */
    public void handleClick(Player player, PlayerData data) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;

        long now = System.currentTimeMillis();
        long lastActualMove = data.getLastActualMoveTime();
        double moveSpeed = data.getLastMovementSpeed();

        // ⭐⭐ المنطق:
        // 1. اللاعب حرّك شي في الإنفنتوري (handleClick)
        // 2. كان يتحرك في آخر 500ms (lastActualMove)
        // 3. السرعة > 0.1 (moveSpeed)

        long timeSinceMove = now - lastActualMove;

        if (timeSinceMove < MOVE_WINDOW_MS && moveSpeed > MIN_MOVE_SPEED) {
            flag(player, data, "click while moving speed=" + MathUtil.round(moveSpeed, 3)
                    + " time=" + timeSinceMove + "ms");
            return;
        }

        // ⭐⭐ الطريقة الثانية: نفحص الحركة الحالية مباشرة
        Location lastLoc = data.getLastLocation();
        if (lastLoc == null) return;

        Location current = player.getLocation();
        double dx = current.getX() - lastLoc.getX();
        double dz = current.getZ() - lastLoc.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        if (speed > MIN_MOVE_SPEED) {
            flag(player, data, "click while current moving speed=" + MathUtil.round(speed, 3));
        }
    }
}
