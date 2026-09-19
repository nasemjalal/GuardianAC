package com.nasem.guardianac;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
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
    private ProtocolManager protocolManager;
    private PacketListener packetListener;

    @Override
    public void onEnable() {
        instance = this;

        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") == null) {
            getLogger().severe("ProtocolLib is not installed! GuardianAC requires ProtocolLib.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        this.protocolManager = ProtocolLibrary.getProtocolManager();
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

        getLogger().info("GuardianAC v" + getDescription().getVersion() + " enabled with ProtocolLib!");
    }

    @Override
    public void onDisable() {
        if (packetListener != null) {
            packetListener.unregister();
        }
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

    public PunishmentManager getPunishmentManager() {
        return punishmentManager;
    }

    public ProtocolManager getProtocolManager() {
        return protocolManager;
    }
}
