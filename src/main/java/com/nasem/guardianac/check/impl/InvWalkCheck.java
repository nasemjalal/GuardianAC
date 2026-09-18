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

    private final long minSwingDelay;
    private final double maxMoveSpeed;

    public InvWalkCheck(GuardianAC plugin) {
        super(plugin, CheckType.INVENTORYMOVE);
        this.minSwingDelay = plugin.getConfig().getLong("checks.inventorymove.min-swing-delay", 50);
        this.maxMoveSpeed = plugin.getConfig().getDouble("checks.inventorymove.max-move-speed", 0.3);
    }

    /**
     * ⭐ يستدعى من ARM_ANIMATION packet
     * إذا اللاعب يلوّح (swing) + يتحرك بسرعة + ما فيه ضحية قريبة = InvWalk
     */
    public void handleSwing(Player player, PlayerData data) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.isInsideVehicle()) return;

        // ⭐ نتأكد اللاعب يتحرك حالياً
        long lastMove = data.getLastMoveTime();
        long now = System.currentTimeMillis();

        // إذا ما تحرك في آخر 200ms — نتجاهل
        if (now - lastMove > 200) return;

        // ⭐ نحسب سرعة الحركة الحالية
        Location lastLoc = data.getLastLocation();
        if (lastLoc == null) return;

        Location current = player.getLocation();
        double dx = current.getX() - lastLoc.getX();
        double dz = current.getZ() - lastLoc.getZ();
        double speed = Math.sqrt(dx * dx + dz * dz);

        // ⭐ إذا يتحرك بسرعة عالية + يلوّح = InvWalk suspicious
        if (speed > maxMoveSpeed) {
            long swingDelay = now - data.getLastAttackTime();

            if (swingDelay > minSwingDelay) {
                data.setLastAttackTime(now);
                flag(player, data, "swing while moving speed=" + MathUtil.round(speed, 3));
            }
        }
    }
}
