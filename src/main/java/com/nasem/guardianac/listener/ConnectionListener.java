package com.nasem.guardianac.listener;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {

    private final GuardianAC plugin;

    public ConnectionListener(GuardianAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        plugin.getPlayerDataManager().get(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.getPlayerDataManager().remove(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        plugin.getPlayerDataManager().get(player).setInventoryOpen(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) return;
        Player player = (Player) event.getPlayer();
        plugin.getPlayerDataManager().get(player).setInventoryOpen(false);
    }

    /**
     * ⭐⭐⭐ الأهم — InventoryClick
     * يشتغل لما اللاعب ينقر داخل الإنفنتوري (نقل شي من slot لـ slot)
     * لا يشتغل لما يأخذ شي من الأرض
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        PlayerData data = plugin.getPlayerDataManager().get(player);

        // ⭐ نتجاهل النقرات على الأرض
        if (event.getClickedInventory() == null) return;
        if (!event.getClickedInventory().equals(player.getInventory())
                && !event.getView().getTopInventory().equals(event.getClickedInventory())) {
            // هذا inventory آخر — نتجاهله
            return;
        }

        // ⭐ نتحقق إذا اللاعب يستخدم Shift-Click أو سحب
        // هذا هو اللي يصير لما ينقل شي

        Check check = plugin.getCheckManager().getCheck(CheckType.INVENTORYMOVE);
        if (check != null && check.isEnabled()) {
            try {
                ((com.nasem.guardianac.check.impl.InvWalkCheck) check)
                        .handleClick(player, data);
            } catch (Exception ignored) {}
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) return;
        Player player = (Player) event.getEntity();
        PlayerData data = plugin.getPlayerDataManager().get(player);

        Check velocity = plugin.getCheckManager().getCheck(CheckType.VELOCITY);
        if (velocity != null && velocity.isEnabled()) {
            try {
                ((com.nasem.guardianac.check.impl.VelocityCheck) velocity).handle(player, data);
            } catch (Exception ignored) {}
        }
    }
}
