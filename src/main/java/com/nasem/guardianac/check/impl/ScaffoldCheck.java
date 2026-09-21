package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ScaffoldCheck extends Check {

    public ScaffoldCheck(GuardianAC plugin) {
        super(plugin, CheckType.SCAFFOLD);
    }

    public void handle(Player player, BlockPlaceEvent event, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        boolean debug = plugin.getConfig().getBoolean("checks.scaffold.debug", false);

        Block placed = event.getBlock();
        Block against = event.getBlockAgainst();
        Location feet = player.getLocation();

        // بس البلوكات اللي تنحط تحت مستوى الرجلين (سكافولد)
        if (placed.getY() >= feet.getBlockY()) {
            if (debug) plugin.getLogger().info("[Scaffold] skip " + player.getName() + " (block not below feet)");
            return;
        }

        // هل النظرة فعلاً على البلوك اللي انحط عليه؟
        double expand = plugin.getConfig().getDouble("checks.scaffold.look-expand", 0.3);
        BoundingBox box = BoundingBox.of(against).expand(expand);
        Vector origin = player.getEyeLocation().toVector();
        Vector dir = player.getEyeLocation().getDirection();

        RayTraceResult result = box.rayTrace(origin, dir, 7.0);
        boolean looking = result != null;

        if (debug) {
            plugin.getLogger().info("[Scaffold] " + player.getName()
                    + " placed " + placed.getType()
                    + " looking=" + looking
                    + " => " + (looking ? "ok" : "FLAG"));
        }

        if (!looking) {
            flag(player, data, "not-looking-at-block");
        }
    }
}
