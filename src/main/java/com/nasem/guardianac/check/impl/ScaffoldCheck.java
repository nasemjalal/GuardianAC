package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

public class ScaffoldCheck extends Check {

    public ScaffoldCheck(GuardianAC plugin) {
        super(plugin, CheckType.SCAFFOLD);
    }

    private boolean debug() {
        return plugin.getConfig().getBoolean("checks.scaffold.debug", false)
                || plugin.getConfig().getBoolean("checks.killaura.debug", false);
    }

    public void handleInteract(Player player, PlayerInteractEvent event, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        Block clicked = event.getClickedBlock();
        Location point = event.getInteractionPoint();
        ItemStack item = event.getItem();
        if (clicked == null || point == null || item == null) return;
        if (!item.getType().isBlock()) return;

        // بس لما البلوك اللي انكبس عليه تحت مستوى الرجلين
        if (clicked.getY() >= player.getLocation().getBlockY()) return;

        double rx = point.getX() - clicked.getX();
        double ry = point.getY() - clicked.getY();
        double rz = point.getZ() - clicked.getZ();

        double a;
        double b;
        switch (event.getBlockFace()) {
            case UP:
            case DOWN:
                a = rx;
                b = rz;
                break;
            case NORTH:
            case SOUTH:
                a = rx;
                b = ry;
                break;
            case EAST:
            case WEST:
                a = ry;
                b = rz;
                break;
            default:
                return;
        }

        double tol = plugin.getConfig().getDouble("checks.scaffold.center-tolerance", 0.01);
        boolean centered = Math.abs(a - 0.5) < tol && Math.abs(b - 0.5) < tol;

        if (debug()) {
            plugin.getLogger().info("[Scaffold] click " + player.getName()
                    + " face=" + event.getBlockFace()
                    + String.format(" a=%.3f b=%.3f", a, b)
                    + " => " + (centered ? "FLAG" : "ok"));
        }

        if (centered) {
            flag(player, data, "centered-click");
        }
    }

    public void handle(Player player, BlockPlaceEvent event, PlayerData data) {
        if (!enabled) return;
        if (player.getGameMode() == GameMode.SPECTATOR) return;

        Block placed = event.getBlock();
        Block against = event.getBlockAgainst();
        Location feet = player.getLocation();

        if (placed.getY() >= feet.getBlockY()) return;

        double expand = plugin.getConfig().getDouble("checks.scaffold.look-expand", 0.3);
        BoundingBox box = BoundingBox.of(against).expand(expand);
        Vector origin = player.getEyeLocation().toVector();
        Vector dir = player.getEyeLocation().getDirection();

        RayTraceResult result = box.rayTrace(origin, dir, 7.0);
        boolean looking = result != null;

        if (debug()) {
            plugin.getLogger().info("[Scaffold] place " + player.getName()
                    + " " + placed.getType()
                    + " looking=" + looking
                    + " => " + (looking ? "ok" : "FLAG"));
        }

        if (!looking) {
            flag(player, data, "not-looking-at-block");
        }
    }
}
