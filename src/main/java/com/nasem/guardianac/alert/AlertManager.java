package com.nasem.guardianac.alert;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class AlertManager {

    private final GuardianAC plugin;
    private final String pluginName;

    public AlertManager(GuardianAC plugin) {
        this.plugin = plugin;
        this.pluginName = ChatColor.translateAlternateColorCodes('&',
                plugin.getConfig().getString("settings.plugin-name", "&cGuardianAC"));
    }

    public void sendAlert(Player player, Check check, int vl, String debug) {
        int threshold = plugin.getConfig().getInt("settings.alert-threshold", 1);
        if (vl < threshold) return;

        int maxVL = check.getMaxViolations();

        // ⭐ شكل Vulcan: GuardianAC » nasem failed Speed (Type A) [1/25]
        String message = pluginName
                + ChatColor.GRAY + " » "
                + ChatColor.WHITE + player.getName()
                + ChatColor.GRAY + " failed "
                + ChatColor.WHITE + check.getName()
                + ChatColor.GRAY + " (Type A) "
                + ChatColor.RED + "[" + vl
                + ChatColor.GRAY + "/" + maxVL
                + ChatColor.RED + "]";

        // إضافة debug إذا موجود
        if (debug != null && !debug.isEmpty()) {
            message += ChatColor.DARK_GRAY + " [" + debug + "]";
        }

        // إرسال للستاف
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("guardianac.alerts")) {
                online.sendMessage(message);
            }
        }

        // الكونسول
        if (plugin.getConfig().getBoolean("settings.console-alerts", true)) {
            Bukkit.getConsoleSender().sendMessage(message);
        }
    }

    public String getPrefix() {
        return pluginName;
    }

    public String getPluginName() {
        return pluginName;
    }
}
