package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.UUID;

public class KillAuraCheck extends Check {

    public KillAuraCheck(GuardianAC plugin) {
        super(plugin, CheckType.KILLAURA);
    }

    private boolean debug() {
        return plugin.getConfig().getBoolean("checks.killaura.debug", false);
    }

    private boolean shouldSkip(Player player) {
        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return true;
        return player.isInsideVehicle();
    }

    public void handlePacket(Player attacker, PlayerData data, Entity target,
                             float yaw, float pitch, long time) {
        if (!enabled) return;

        if (shouldSkip(attacker)) {
            if (debug()) {
                plugin.getLogger().info("[KillAura] skip " + attacker.getName()
                        + " (creative/spectator/vehicle)");
            }
            return;
        }

        // 1) Target switch
        long minSwitch = plugin.getConfig().getLong("checks.killaura.min-target-switch-ms", 100);
        UUID lastId = data.getLastTargetId();
        long sinceLast = time - data.getLastTargetTime();
        boolean switched = lastId != null && !lastId.equals(target.getUniqueId());
        data.setLastTargetId(target.getUniqueId());
        data.setLastTargetTime(time);
        if (switched && sinceLast >= 0 && sinceLast < minSwitch) {
            flag(attacker, data, "targetSwitch=" + sinceLast + "ms");
        }

        // 2) Angle
        if (yaw == 0f && pitch == 0f) return;

        Vector toTarget = target.getLocation().add(0, target.getHeight() / 2.0, 0)
                .toVector().subtract(attacker.getEyeLocation().toVector());

        double requiredYaw = Math.toDegrees(Math.atan2(-toTarget.getX(), toTarget.getZ()));
        double requiredPitch = Math.toDegrees(Math.atan2(-toTarget.getY(),
                Math.sqrt(toTarget.getX() * toTarget.getX() + toTarget.getZ() * toTarget.getZ())));

        double yawDiff = angleDifference(yaw, (float) requiredYaw);
        double pitchDiff = Math.abs(pitch - requiredPitch);

        double maxDiff = plugin.getConfig().getDouble("checks.killaura.max-angle-difference", 45.0);

        if (debug()) {
            plugin.getLogger().info("[KillAura] " + attacker.getName()
                    + " -> " + target.getType()
                    + " yawDiff=" + Math.round(yawDiff)
                    + " pitchDiff=" + Math.round(pitchDiff)
                    + " max=" + maxDiff
                    + " switch=" + sinceLast + "ms");
        }

        if (yawDiff > maxDiff || pitchDiff > maxDiff) {
            flag(attacker, data, String.format("yawDiff=%.1f pitchDiff=%.1f", yawDiff, pitchDiff));
        }
    }

    public void handleRotation(Player player, PlayerData data, float newYaw) {
        if (!enabled) return;
        if (shouldSkip(player)) return;

        float lastYaw = data.getLastYawGCD();
        if (lastYaw == 0) {
            data.setLastYawGCD(newYaw);
            return;
        }

        double delta = angleDelta(lastYaw, newYaw);
        data.setLastYawGCD(newYaw);

        if (Math.abs(delta) < 0.001) return;

        data.getRotationDeltas().addLast(delta);
        int sample = plugin.getConfig().getInt("checks.killaura.rotation-sample-size", 20);
        while (data.getRotationDeltas().size() > sample) {
            data.getRotationDeltas().removeFirst();
        }
        if (data.getRotationDeltas().size() < sample) return;

        long gcdScaled = 0;
        for (double d : data.getRotationDeltas()) {
            long scaled = Math.round(Math.abs(d) * 1000);
            if (scaled == 0) continue;
            gcdScaled = (gcdScaled == 0) ? scaled : gcd(gcdScaled, scaled);
        }

        double gcdThreshold = plugin.getConfig()
                .getDouble("checks.killaura.min-suspicious-gcd", 45.0);

        if (gcdScaled > gcdThreshold) {
            final String info = "rotationGCD=" + gcdScaled;
            data.getRotationDeltas().clear();
            Bukkit.getScheduler().runTask(plugin, () -> {
                if (player.isOnline()) flag(player, data, info);
            });
        }
    }

    public void handleSwing(Player attacker, PlayerData data) {
        data.setLastSwingTime(System.currentTimeMillis());
    }

    private long gcd(long a, long b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    private double angleDelta(float a, float b) {
        double diff = (b - a) % 360;
        if (diff < -180) diff += 360;
        if (diff > 180) diff -= 360;
        return diff;
    }

    private double angleDifference(float a, float b) {
        double diff = Math.abs(a - b) % 360;
        return diff > 180 ? 360 - diff : diff;
    }
}
