package com.nasem.guardianac.alert;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AlertManager {

    private final GuardianAC plugin;
    private final String prefix;

    public AlertManager(GuardianAC plugin) {
        this.plugin = plugin;
        this.prefix = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("settings.prefix", "&8[&bGuardianAC&8] &r"));
    }

    /**
     * Send an alert to staff with permission guardianac.alerts.
     *
     * @param player  the player being flagged
     * @param check   the check that flagged them
     * @param vl      the player's current violation level for that check
     * @param debug   extra debug info
     */
    public void sendAlert(Player player, Check check, int vl, String debug) {
        int threshold = plugin.getConfig().getInt("settings.alert-threshold", 1);
        if (vl < threshold) return;

        String message = prefix
                + ChatColor.GRAY + player.getName()
                + ChatColor.WHITE + " failed "
                + ChatColor.RED + check.getName()
                + ChatColor.WHITE + " (VL: "
                + ChatColor.YELLOW + vl
                + ChatColor.WHITE + ")"
                + (debug != null && !debug.isEmpty()
                    ? ChatColor.GRAY + " [" + debug + "]" : "");

        // Send to all online staff
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("guardianac.alerts")) {
                online.sendMessage(message);
            }
        }

        // Log to console if enabled
        if (plugin.getConfig().getBoolean("settings.console-alerts", true)) {
            Bukkit.getConsoleSender().sendMessage(message);
        }

        // Handle punishment if max violations reached
        if (vl >= check.getMaxViolations()) {
            handlePunishment(player, check, vl);
        }
    }

    /**
     * Handle punishment when max VL reached.
     * Currently just logs and resets. Extend as needed.
     */
    private void handlePunishment(Player player, Check check, int vl) {
        String punishMsg = prefix + ChatColor.DARK_RED + "[PUNISH] "
                + ChatColor.WHITE + player.getName()
                + " reached VL " + vl + " on " + check.getName();

        Bukkit.getConsoleSender().sendMessage(punishMsg);
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("guardianac.alerts")) {
                online.sendMessage(punishMsg);
            }
        }

        // Reset violations after punishment
        plugin.getPlayerDataManager().get(player).resetViolation(check.getType());
    }

    public String getPrefix() {
        return prefix;
    }
}
