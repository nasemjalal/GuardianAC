package com.nasem.guardianac;

import com.nasem.guardianac.alert.AlertManager;
import com.nasem.guardianac.check.CheckManager;
import com.nasem.guardianac.command.GuardianCommand;
import com.nasem.guardianac.data.PlayerDataManager;
import com.nasem.guardianac.listener.CombatListener;
import com.nasem.guardianac.listener.ConnectionListener;
import com.nasem.guardianac.listener.MovementListener;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class GuardianAC extends JavaPlugin {

    private static GuardianAC instance;

    private PlayerDataManager playerDataManager;
    private CheckManager checkManager;
    private AlertManager alertManager;

    @Override
    public void onEnable() {
        instance = this;

        // Save default config
        saveDefaultConfig();

        // Initialize managers
        this.playerDataManager = new PlayerDataManager(this);
        this.checkManager = new CheckManager(this);
        this.alertManager = new AlertManager(this);

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new MovementListener(this), this);
        Bukkit.getPluginManager().registerEvents(new CombatListener(this), this);
        Bukkit.getPluginManager().registerEvents(new ConnectionListener(this), this);

        // Register command
        GuardianCommand commandExecutor = new GuardianCommand(this);
        getCommand("guardian").setExecutor(commandExecutor);
        getCommand("guardian").setTabCompleter(commandExecutor);

        getLogger().info("GuardianAC v" + getDescription().getVersion() + " enabled successfully!");
    }

    @Override
    public void onDisable() {
        if (checkManager != null) {
            checkManager.shutdown();
        }
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
}
