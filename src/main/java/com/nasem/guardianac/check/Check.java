package com.nasem.guardianac.check;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.entity.Player;

public abstract class Check {

    protected final GuardianAC plugin;
    protected final CheckType type;
    protected final String name;

    protected boolean enabled;
    protected int maxViolations;
    protected int decaySeconds;

    public Check(GuardianAC plugin, CheckType type) {
        this.plugin = plugin;
        this.type = type;
        this.name = type.getName();
        loadConfig();
    }

    protected void loadConfig() {
        String path = "checks." + type.getConfigKey();
        this.enabled = plugin.getConfig().getBoolean(path + ".enabled", true);
        this.maxViolations = plugin.getConfig().getInt(path + ".max-violations", 20);
        this.decaySeconds = plugin.getConfig().getInt(path + ".decay-seconds", 10);
    }

    /**
     * Called when the check should be re-evaluated (e.g., after config reload).
     */
    public void reload() {
        loadConfig();
    }

    /**
     * Flag a player for this check.
     *
     * @param player  the player
     * @param data    the player's data
     * @param debug   extra info to include in the alert
     */
    protected void flag(Player player, PlayerData data, String debug) {
        if (!enabled) return;

        data.addViolation(type);
        int vl = data.getViolation(type);

        plugin.getAlertManager().sendAlert(player, this, vl, debug);
    }

    public CheckType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public int getMaxViolations() {
        return maxViolations;
    }

    public int getDecaySeconds() {
        return decaySeconds;
    }
}
