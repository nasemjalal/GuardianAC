package com.nasem.guardianac.listener;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

public class MovementListener implements Listener {

    private final GuardianAC plugin;

    public MovementListener(GuardianAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (from.getX() == to.getX() && from.getY() == to.getY() && from.getZ() == to.getZ()) {
            return;
        }

        PlayerData data = plugin.getPlayerDataManager().get(player);

        if (player.isOnGround()) {
            data.incrementGroundTicks();
            data.resetAirTicks();
            data.setLastGroundLocation(to.clone());
        } else {
            data.incrementAirTicks();
            data.resetGroundTicks();
        }

        runCheck(CheckType.SPEED, player, data, from, to);
        runCheck(CheckType.FLIGHT, player, data, from, to);
        runCheck(CheckType.NOFALL, player, data, from, to);
        runCheck(CheckType.JESUS, player, data, from, to);
        runCheck(CheckType.STEP, player, data, from, to);
        runCheck(CheckType.NOSLOW, player, data, from, to);
        runCheck(CheckType.PHASE, player, data, from, to);
        runCheck(CheckType.ELYTRA, player, data, from, to);
        runCheck(CheckType.TIMER, player, data, from, to);

        // ⭐ InvWalk (يستخدم InvWalkCheck)
        runInvWalk(player, data, from, to);

        // ⭐ Velocity
        runVelocity(player, data, from, to);

        data.setLastLocation(to.clone());
        data.setLastMoveTime(System.currentTimeMillis());
    }

    private void runInvWalk(Player player, PlayerData data, Location from, Location to) {
        Check check = plugin.getCheckManager().getCheck(CheckType.INVENTORYMOVE);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.InvWalkCheck) check)
                    .handle(player, data, from, to);
        } catch (Exception ignored) {}
    }

    private void runVelocity(Player player, PlayerData data, Location from, Location to) {
        Check check = plugin.getCheckManager().getCheck(CheckType.VELOCITY);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.VelocityCheck) check)
                    .handleMovement(player, data, from, to);
        } catch (Exception ignored) {}
    }

    private void runCheck(CheckType type, Player player, PlayerData data, Location from, Location to) {
        Check check = plugin.getCheckManager().getCheck(type);
        if (check == null || !check.isEnabled()) return;

        try {
            switch (type) {
                case SPEED:
                    ((com.nasem.guardianac.check.impl.SpeedCheck) check).handle(player, data, from, to);
                    break;
                case FLIGHT:
                    ((com.nasem.guardianac.check.impl.FlightCheck) check).handle(player, data, from, to);
                    break;
                case NOFALL:
                    ((com.nasem.guardianac.check.impl.NoFallCheck) check).handle(player, data, from, to);
                    break;
                case JESUS:
                    ((com.nasem.guardianac.check.impl.JesusCheck) check).handle(player, data, from, to);
                    break;
                case STEP:
                    ((com.nasem.guardianac.check.impl.StepCheck) check).handle(player, data, from, to);
                    break;
                case NOSLOW:
                    ((com.nasem.guardianac.check.impl.NoSlowCheck) check).handle(player, data, from, to);
                    break;
                case PHASE:
                    ((com.nasem.guardianac.check.impl.PhaseCheck) check).handle(player, data, from, to);
                    break;
                case ELYTRA:
                    ((com.nasem.guardianac.check.impl.ElytraCheck) check).handle(player, data, from, to);
                    break;
                case TIMER:
                    ((com.nasem.guardianac.check.impl.TimerCheck) check).handle(player, data, from, to);
                    break;
                default:
                    break;
            }
        } catch (Exception ignored) {}
    }
}
