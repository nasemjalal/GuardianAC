package com.nasem.guardianac.command;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GuardianCommand implements CommandExecutor, TabCompleter {

    private final GuardianAC plugin;

    public GuardianCommand(GuardianAC plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String prefix = plugin.getAlertManager().getPluginName() + ChatColor.GRAY + " » " + ChatColor.RESET;

        if (args.length == 0) {
            sender.sendMessage(prefix + ChatColor.YELLOW + "GuardianAC v" + plugin.getDescription().getVersion());
            sender.sendMessage(ChatColor.GRAY + "/guardian vl <player>" + ChatColor.WHITE + " - Check violations");
            sender.sendMessage(ChatColor.GRAY + "/guardian reset <player>" + ChatColor.WHITE + " - Reset violations");
            sender.sendMessage(ChatColor.GRAY + "/guardian reload" + ChatColor.WHITE + " - Reload config");
            sender.sendMessage(ChatColor.GRAY + "/guardian info" + ChatColor.WHITE + " - Plugin info");
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "vl": {
                if (!sender.hasPermission("guardianac.admin")) {
                    sender.sendMessage(prefix + ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(prefix + ChatColor.RED + "Usage: /guardian vl <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(prefix + ChatColor.RED + "Player not found.");
                    return true;
                }
                PlayerData data = plugin.getPlayerDataManager().get(target);
                sender.sendMessage(prefix + ChatColor.YELLOW + "Violations for " + target.getName() + ":");
                boolean any = false;
                for (CheckType type : CheckType.values()) {
                    int vl = data.getViolation(type);
                    if (vl > 0) {
                        sender.sendMessage(ChatColor.GRAY + " - " + type.getName() + ": "
                                + ChatColor.RED + vl);
                        any = true;
                    }
                }
                if (!any) {
                    sender.sendMessage(ChatColor.GREEN + " Clean! No violations.");
                }
                return true;
            }

            case "reset": {
                if (!sender.hasPermission("guardianac.admin")) {
                    sender.sendMessage(prefix + ChatColor.RED + "No permission.");
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(prefix + ChatColor.RED + "Usage: /guardian reset <player>");
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(prefix + ChatColor.RED + "Player not found.");
                    return true;
                }
                plugin.getPlayerDataManager().get(target).resetAllViolations();
                sender.sendMessage(prefix + ChatColor.GREEN + "Reset violations for " + target.getName());
                return true;
            }

            case "reload": {
                if (!sender.hasPermission("guardianac.admin")) {
                    sender.sendMessage(prefix + ChatColor.RED + "No permission.");
                    return true;
                }
                plugin.reloadConfig();
                plugin.getCheckManager().reload();
                sender.sendMessage(prefix + ChatColor.GREEN + "Config reloaded.");
                return true;
            }

            case "info": {
                sender.sendMessage(prefix + ChatColor.YELLOW + "GuardianAC v" + plugin.getDescription().getVersion());
                sender.sendMessage(ChatColor.GRAY + "Checks loaded: " + ChatColor.WHITE + plugin.getCheckManager().getChecks().size());
                sender.sendMessage(ChatColor.GRAY + "Players tracked: " + ChatColor.WHITE + plugin.getPlayerDataManager().getAllData().size());
                return true;
            }

            default: {
                sender.sendMessage(prefix + ChatColor.RED + "Unknown subcommand.");
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = Arrays.asList("vl", "reset", "reload", "info");
            for (String s : subs) {
                if (s.startsWith(args[0].toLowerCase())) completions.add(s);
            }
        } else if (args.length == 2
                && (args[0].equalsIgnoreCase("vl") || args[0].equalsIgnoreCase("reset"))) {
            for (Player p : Bukkit.getOnlinePlayers()) {
                if (p.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(p.getName());
                }
            }
        }
        return completions;
    }
}
