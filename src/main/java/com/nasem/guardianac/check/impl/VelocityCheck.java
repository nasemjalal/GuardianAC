package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import com.nasem.guardianac.util.MathUtil;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class VelocityCheck extends Check {

    public VelocityCheck(GuardianAC plugin) {
        super(plugin, CheckType.VELOCITY);
    }

    /**
     * ⭐ يُستدعى من ConnectionListener عند أخذ ضرر
     */
    public void handle(Player player, PlayerData data) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;

        // ⭐ نحفظ وقت الضرر
        data.setLastVelocityTime(System.currentTimeMillis());
        data.setPendingVelocity(true);
    }

    /**
     * ⭐ يُستدعى من MovementListener بعد الضرر
     * نتأكد إن اللاعب تحرك (knockback)
     */
    public void handleMovement(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;
        if (!data.isPendingVelocity()) return;

        long timeSinceDamage = System.currentTimeMillis() - data.getLastVelocityTime();

        // ⭐ نتوقع knockback في أول 500ms
        if (timeSinceDamage < 50 || timeSinceDamage > 500) {
            data.setPendingVelocity(false);
            return;
        }

        // ⭐ نقيس الحركة الأفقية
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double horizontalMovement = Math.sqrt(dx * dx + dz * dz);

        // Knockback عادي: حركة > 0.1
        // AntiKnockback: حركة < 0.01 (ثابت)

        if (horizontalMovement < 0.01 && timeSinceDamage > 200) {
            flag(player, data, "no knockback mov=" + MathUtil.round(horizontalMovement, 3)
                    + " time=" + timeSinceDamage + "ms");
            data.setPendingVelocity(false);
        }
    }
}
