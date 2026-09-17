package com.nasem.guardianac.check;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.impl.*;

import java.util.ArrayList;
import java.util.List;

public class CheckManager {

    private final GuardianAC plugin;
    private final List<Check> checks = new ArrayList<>();

    public CheckManager(GuardianAC plugin) {
        this.plugin = plugin;
        registerChecks();
    }

    private void registerChecks() {
        // Movement checks
        checks.add(new SpeedCheck(plugin));
        checks.add(new FlightCheck(plugin));
        checks.add(new NoFallCheck(plugin));
        checks.add(new JesusCheck(plugin));
        checks.add(new StepCheck(plugin));
        checks.add(new NoSlowCheck(plugin));
        checks.add(new PhaseCheck(plugin));
        checks.add(new ElytraCheck(plugin));

        // Combat checks
        checks.add(new ReachCheck(plugin));
        checks.add(new KillAuraCheck(plugin));
        checks.add(new AutoClickerCheck(plugin));
        checks.add(new CriticalsCheck(plugin));
        checks.add(new VelocityCheck(plugin));
        checks.add(new WallhackCheck(plugin));

        // World / Block checks
        checks.add(new FastPlaceCheck(plugin));
        checks.add(new FastBreakCheck(plugin));
        checks.add(new BlockReachCheck(plugin));
        checks.add(new NukerCheck(plugin));
        checks.add(new ScaffoldCheck(plugin));

        // Misc checks
        checks.add(new TimerCheck(plugin));
        checks.add(new InventoryMoveCheck(plugin));

        plugin.getLogger().info("Registered " + checks.size() + " checks.");
    }

    public List<Check> getChecks() {
        return checks;
    }

    public Check getCheck(CheckType type) {
        for (Check check : checks) {
            if (check.getType() == type) return check;
        }
        return null;
    }

    public void reload() {
        for (Check check : checks) {
            check.reload();
        }
    }

    public void shutdown() {
        checks.clear();
    }
}
