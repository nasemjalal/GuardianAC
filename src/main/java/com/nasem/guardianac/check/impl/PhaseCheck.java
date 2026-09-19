package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

public class PhaseCheck extends Check {

    public PhaseCheck(GuardianAC plugin) {
        super(plugin, CheckType.PHASE);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;
        if (player.getAllowFlight() || player.isFlying()) return;
        if (player.isGliding()) return;
        if (player.isInsideVehicle()) return;

        // ⭐ نتجاهل ping عالي
        if (player.getPing() > 200) return;

        if (from.getWorld() != to.getWorld()) return;

        // ⭐ نفحص البلوكات اللي اللاعب داخل فيها
        Location feet = to.clone();
        Location head = to.clone().add(0, 1, 0);

        boolean feetSolid = isSolid(feet);
        boolean headSolid = isSolid(head);

        // ⭐⭐ لازم الاثنين صلبة
        if (!feetSolid || !headSolid) return;

        // ⭐⭐⭐ نتجاهل الباب (door) — إذا كان اللاعب يفتح الباب ويدخل
        if (isDoorOrGate(feet.getBlock().getType())) return;
        if (isDoorOrGate(head.getBlock().getType())) return;

        // ⭐ نتجاهل البلوكات اللي ما هي صلبة فعلياً
        if (isNonSolid(feet.getBlock().getType())) return;
        if (isNonSolid(head.getBlock().getType())) return;

        // ⭐⭐⭐ نتجاهل إذا كان اللاعب داخل بلوك لأول مرة
        Location lastLoc = data.getLastLocation();
        if (lastLoc == null) return;

        // ⭐ إذا كان آخر موقع **مو داخل بلوك** = دخل بلوك = Phase
        boolean wasFeetSolid = isSolid(lastLoc);
        boolean wasHeadSolid = isSolid(lastLoc.clone().add(0, 1, 0));

        // ⭐⭐⭐ فقط إذا كان **خارج** البلوك الآن **داخله**
        if (!wasFeetSolid && !wasHeadSolid) {
            // ⭐ ننتظر tick قبل flag
            flag(player, data, "inside solid block");
        }
    }

    /**
     * ⭐ فحص هل البلوك صلب
     */
    private boolean isSolid(Location loc) {
        if (loc.getWorld() == null) return false;
        Material type = loc.getBlock().getType();
        return type.isSolid();
    }

    /**
     * ⭐ نتجاهل الأبواب والبوابات
     */
    private boolean isDoorOrGate(Material mat) {
        String name = mat.name();
        return name.contains("DOOR") || name.contains("GATE")
                || name.contains("TRAPDOOR") || name.contains("FENCE_GATE");
    }

    /**
     * ⭐ نتجاهل البلوكات اللي مو صلبة
     */
    private boolean isNonSolid(Material mat) {
        String name = mat.name();
        return name.contains("SIGN") || name.contains("BANNER")
                || name.contains("TORCH") || name.contains("LANTERN")
                || name.contains("BUTTON") || name.contains("LEVER")
                || name.contains("PRESSURE_PLATE") || name.contains("RAIL")
                || name.contains("CARPET") || name.contains("SNOW");
    }
}
