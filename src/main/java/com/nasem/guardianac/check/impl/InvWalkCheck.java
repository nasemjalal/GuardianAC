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

    // ⭐⭐ الوقت اللي بعد فتح الإنفنتوري — نعتبره مفتوح
    private static final long INV_OPEN_WINDOW_MS = 3000;

    public InvWalkCheck(GuardianAC plugin) {
        super(plugin, CheckType.INVENTORYMOVE);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;
        if (player.isFlying() || player.getAllowFlight()) return;
        if (player.isGliding()) return;

        // ⭐⭐⭐ الطريقة الجديدة: نتحقق إذا الإنفنتوري مفتوح
        // إما من InventoryOpenEvent، أو من ClientCommand
        boolean invOpen = data.isInventoryOpen();

        if (!invOpen) {
            // ⭐ ما مفتوح — نتجاهل
            return;
        }

        // ⭐ نحسب الحركة
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        // ⭐ إذا تحرك بشكل ملحوظ
        if (speed > 0.05) {
            flag(player, data, "move=" + MathUtil.round(speed, 3));
        }
    }

    /**
     * ⭐ swing check — إذا اللاعب يضرب و الإنفنتوري مفتوح
     */
    public void handleSwing(Player player, PlayerData data) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        // ⭐ إذا الإنفنتوري مفتوح + swing = Wurst InvWalk
        if (!data.isInventoryOpen()) return;

        long lastInvOpen = data.getLastInvOpenTime();
        if (System.currentTimeMillis() - lastInvOpen < INV_OPEN_WINDOW_MS) {
            flag(player, data, "swing while inventory open");
        }
    }
}
