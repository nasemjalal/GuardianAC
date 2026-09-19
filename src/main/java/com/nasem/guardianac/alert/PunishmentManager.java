package com.nasem.guardianac.alert;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Date;

public class PunishmentManager {

    private final GuardianAC plugin;

    public PunishmentManager(GuardianAC plugin) {
        this.plugin = plugin;
    }

    public void applyPunishment(Player player, Check check, int vl) {
        int maxVL = check.getMaxViolations();
        if (vl < maxVL) return;

        String punishment = plugin.getConfig()
                .getString("checks." + check.getType().getConfigKey() + ".punishment", "kick");

        switch (punishment.toLowerCase()) {
            case "ban":
                banPlayer(player, check, vl);
                break;
            case "kick":
            default:
                kickPlayer(player, check, vl);
                break;
        }
    }

    private void kickPlayer(Player player, Check check, int vl) {
        String pluginName = plugin.getAlertManager().getPluginName();

        String kickMsg = pluginName + ChatColor.GRAY + " » "
                + ChatColor.WHITE + "\n\n"
                + ChatColor.RED + "تم طردك من السيرفر\n"
                + ChatColor.GRAY + "السبب: "
                + ChatColor.WHITE + check.getName()
                + ChatColor.GRAY + " [" + vl + "/" + check.getMaxViolations() + "]";

        Bukkit.getScheduler().runTask(plugin, () -> {
            player.kickPlayer(kickMsg);

            String alert = pluginName + ChatColor.GRAY + " » "
                    + ChatColor.WHITE + player.getName()
                    + ChatColor.RED + " was kicked for "
                    + ChatColor.WHITE + check.getName()
                    + ChatColor.RED + " [" + vl + "/" + check.getMaxViolations() + "]";
            broadcastToStaff(alert);
            Bukkit.getConsoleSender().sendMessage(alert);
        });
    }

    private void banPlayer(Player player, Check check, int vl) {
        String pluginName = plugin.getAlertManager().getPluginName();

        String kickMsg = pluginName + ChatColor.GRAY + " » "
                + ChatColor.WHITE + "\n\n"
                + ChatColor.DARK_RED + "تم حظرك من السيرفر\n"
                + ChatColor.GRAY + "السبب: "
                + ChatColor.WHITE + check.getName()
                + ChatColor.GRAY + " [" + vl + "/" + check.getMaxViolations() + "]";

        Bukkit.getScheduler().runTask(plugin, () -> {
            Bukkit.getBanList(BanList.Type.NAME).addBan(
                    player.getName(),
                    "Anti-Cheat: " + check.getName(),
                    (Date) null,
                    "GuardianAC"
            );

            player.kickPlayer(kickMsg);

            String alert = pluginName + ChatColor.GRAY + " » "
                    + ChatColor.DARK_RED + player.getName()
                    + ChatColor.RED + " was banned for "
                    + ChatColor.WHITE + check.getName()
                    + ChatColor.RED + " [" + vl + "/" + check.getMaxViolations() + "]";
            broadcastToStaff(alert);
            Bukkit.getConsoleSender().sendMessage(alert);
        });
    }

    private void broadcastToStaff(String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("guardianac.alerts")) {
                online.sendMessage(message);
            }
        }
    }
}
