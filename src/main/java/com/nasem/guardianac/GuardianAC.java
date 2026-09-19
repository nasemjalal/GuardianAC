package com.nasem.guardianac;

import com.github.retrooper.packetevents.PacketEvents;
import com.nasem.guardianac.alert.AlertManager;
import com.nasem.guardianac.alert.PunishmentManager;
import com.nasem.guardianac.check.CheckManager;
import com.nasem.guardianac.command.GuardianCommand;
import com.nasem.guardianac.data.PlayerDataManager;
import com.nasem.guardianac.listener.CombatListener;
import com.nasem.guardianac.listener.ConnectionListener;
import com.nasem.guardianac.listener.MovementListener;
import com.nasem.guardianac.packet.PacketListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class GuardianAC extends JavaPlugin {

    private static GuardianAC instance;

    private PlayerDataManager playerDataManager;
    private CheckManager checkManager;
    private AlertManager alertManager;
    private PunishmentManager punishmentManager;
    private PacketListener packetListener;

    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("packetevents") == null) {
            getLogger().severe("PacketEvents is not installed! GuardianAC requires PacketEvents.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        // Initialize PacketEvents API
        PacketEvents.getAPI().load();
        PacketEvents.getAPI().init();

        this.packetListener = new PacketListener(this);

        this.playerDataManager = new PlayerDataManager(this);
        this.checkManager = new CheckManager(this);
        this.alertManager = new AlertManager(this);
        this.punishmentManager = new PunishmentManager(this);

        Bukkit.getPluginManager().registerEvents(new MovementListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);

        packetListener.register();

        GuardianCommand commandExecutor = new GuardianCommand(this);
        getCommand("guardian").setExecutor(commandExecutor);
        getCommand("guardian").setTabCompleter(commandExecutor);

        getLogger().info("GuardianAC v" + getDescription().getVersion() + " enabled with PacketEvents!");
    }

    @Override
    public void onDisable() {
        if (packetListener != null) {
            packetListener.unregister();
        }
        if (checkManager != null) {
            checkManager.shutdown();
        }
        PacketEvents.getAPI().terminate();
        getLogger().info("GuardianAC disabled.");
    }

    public static GuardianAC getInstance() {
        return instance;
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    public CheckManager getCheckManager() {
        return checkManager;
    }

    public AlertManager getAlertManager() {
        return alertManager;
    }

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }
}
