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
    protected String punishment;

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
        this.decaySeconds = plugin.getConfig().getInt(path + ".decay-seconds", 60);
        // ⭐ العقوبة: "none" / "kick" / "ban"
        this.punishment = plugin.getConfig().getString(path + ".punishment", "none");
    }

    public void reload() {
        loadConfig();
    }

    protected void flag(Player player, PlayerData data, String debug) {
        if (!enabled) return;

        data.addViolation(type);
        int vl = data.getViolation(type);

        // ⭐⭐⭐ يرسل التحذير فقط
        plugin.getAlertManager().sendAlert(player, this, vl, debug);

        // ⭐⭐⭐ العقوبة فقط إذا مو "none"
        if (!"none".equalsIgnoreCase(punishment) && vl >= maxViolations) {
            plugin.getPunishmentManager().applyPunishment(player, this, vl);
            data.resetViolation(type);
        }
    }

    public CheckType getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getNameText() {
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

    public String getPunishment() {
        return punishment;
    }
}
