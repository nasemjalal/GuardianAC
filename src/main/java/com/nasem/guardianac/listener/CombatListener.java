package com.nasem.guardianac.listener;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;

public class CombatListener implements Listener {

    private final GuardianAC plugin;

    public CombatListener(GuardianAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player)) return;
        Player attacker = (Player) event.getDamager();
        Entity victim = event.getEntity();

        PlayerData data = plugin.getPlayerDataManager().get(attacker);

        // Track attack time & clicks
        long now = System.currentTimeMillis();
        data.setLastAttackTime(now);
        data.incrementClicks();
        data.setLastAttackLocation(attacker.getLocation().clone());

        // Run combat checks
        runReach(attacker, victim, data);
        runKillAura(attacker, victim, data);
        runAutoClicker(attacker, data);
        runCriticals(attacker, data);
        runWallhack(attacker, victim, data);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().get(player);

        long now = System.currentTimeMillis();
        data.setLastBlockBreakTime(now);
        data.setLastBlockBreakLocation(event.getBlock().getLocation().clone());

        runFastBreak(player, data);
        runNuker(player, data, event);
        runBlockReachBreak(player, event, data);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        PlayerData data = plugin.getPlayerDataManager().get(player);

        long now = System.currentTimeMillis();
        data.setLastBlockPlaceTime(now);

        runFastPlace(player, data);
        runBlockReachPlace(player, event, data);
        runScaffold(player, event, data);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        // Currently unused, but kept for future checks (e.g., ghost blocks)
    }

    @EventHandler
    public void onItemHeld(PlayerItemHeldEvent event) {
        // Could be used by NoSlow / inventory checks
    }

    // ============ Check runners ============

    private void runReach(Player attacker, Entity victim, PlayerData data) {
        Check check = plugin.getCheckManager().getCheck(CheckType.REACH);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.ReachCheck) check).handle(attacker, victim, data);
        } catch (Exception ignored) {}
    }

    private void runKillAura(Player attacker, Entity victim, PlayerData data) {
        Check check = plugin.getCheckManager().getCheck(CheckType.KILLAURA);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.KillAuraCheck) check).handle(attacker, victim, data);
        } catch (Exception ignored) {}
    }

    private void runAutoClicker(Player attacker, PlayerData data) {
        Check check = plugin.getCheckManager().getCheck(CheckType.AUTOCLICKER);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.AutoClickerCheck) check).handle(attacker, data);
        } catch (Exception ignored) {}
    }

    private void runCriticals(Player attacker, PlayerData data) {
        Check check = plugin.getCheckManager().getCheck(CheckType.CRITICALS);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.CriticalsCheck) check).handle(attacker, data);
        } catch (Exception ignored) {}
    }

    private void runWallhack(Player attacker, Entity victim, PlayerData data) {
        Check check = plugin.getCheckManager().getCheck(CheckType.WALLHACK);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.WallhackCheck) check).handle(attacker, victim, data);
        } catch (Exception ignored) {}
    }

    private void runFastBreak(Player player, PlayerData data) {
        Check check = plugin.getCheckManager().getCheck(CheckType.FASTBREAK);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.FastBreakCheck) check).handle(player, data);
        } catch (Exception ignored) {}
    }

    private void runNuker(Player player, PlayerData data, BlockBreakEvent event) {
        Check check = plugin.getCheckManager().getCheck(CheckType.NUKER);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.NukerCheck) check).handle(player, data, event);
        } catch (Exception ignored) {}
    }

    private void runBlockReachBreak(Player player, BlockBreakEvent event, PlayerData data) {
        Check check = plugin.getCheckManager().getCheck(CheckType.BLOCKREACH);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.BlockReachCheck) check).handleBreak(player, event, data);
        } catch (Exception ignored) {}
    }

    private void runFastPlace(Player player, PlayerData data) {
        Check check = plugin.getCheckManager().getCheck(CheckType.FASTPLACE);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.FastPlaceCheck) check).handle(player, data);
        } catch (Exception ignored) {}
    }

    private void runBlockReachPlace(Player player, BlockPlaceEvent event, PlayerData data) {
        Check check = plugin.getCheckManager().getCheck(CheckType.BLOCKREACH);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.BlockReachCheck) check).handlePlace(player, event, data);
        } catch (Exception ignored) {}
    }

    private void runScaffold(Player player, BlockPlaceEvent event, PlayerData data) {
        Check check = plugin.getCheckManager().getCheck(CheckType.SCAFFOLD);
        if (check == null || !check.isEnabled()) return;
        try {
            ((com.nasem.guardianac.check.impl.ScaffoldCheck) check).handle(player, event, data);
        } catch (Exception ignored) {}
    }
}
