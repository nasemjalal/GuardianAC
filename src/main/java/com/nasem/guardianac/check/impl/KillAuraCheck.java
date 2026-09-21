package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
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
                             float yaw, float pitch, float prevYaw, long lookTime, long time) {
        if (!enabled) return;

        if (shouldSkip(attacker)) {
            if (debug()) {
                plugin.getLogger().info("[KillAura] skip " + attacker.getName()
                        + " (creative/spectator/vehicle)");
            }
            return;
        }

        StringBuilder reasons = new StringBuilder();

        // 1) Target switch
        long minSwitch = plugin.getConfig().getLong("checks.killaura.min-target-switch-ms", 100);
        UUID lastId = data.getLastTargetId();
        long sinceLast = time - data.getLastTargetTime();
        boolean switched = lastId != null && !lastId.equals(target.getUniqueId());
        data.setLastTargetId(target.getUniqueId());
        data.setLastTargetTime(time);
        if (switched && sinceLast >= 0 && sinceLast < minSwitch) {
            reasons.append("switch=").append(sinceLast).append("ms ");
        }

        // 2) Snap
        double snapAngle = plugin.getConfig().getDouble("checks.killaura.snap-angle", 50.0);
        double snap = Math.abs(angleDelta(prevYaw, yaw));
        if (lookTime > 0 && (time - lookTime) < 150 && snap > snapAngle) {
            reasons.append(String.format("snap=%.0f ", snap));
        }

        // 3) Hitbox
        boolean hitboxKnown = !(yaw == 0f && pitch == 0f);
        boolean hit = true;
        if (hitboxKnown) {
            double expand = plugin.getConfig().getDouble("checks.killaura.hitbox-expand", 0.3);
            BoundingBox box = target.getBoundingBox().clone().expand(expand);

            double yawRad = Math.toRadians(yaw);
            double pitchRad = Math.toRadians(pitch);
            double xz = Math.cos(pitchRad);
            Vector dir = new Vector(-Math.sin(yawRad) * xz, -Math.sin(pitchRad), Math.cos(yawRad) * xz);
            Vector origin = attacker.getEyeLocation().toVector();

            RayTraceResult result = box.rayTrace(origin, dir, 8.0);
            hit = result != null;
            if (!hit) {
                reasons.append("hitbox-miss ");
            }
        }

        if (debug()) {
            plugin.getLogger().info("[KillAura] " + attacker.getName()
                    + " -> " + target.getType()
                    + " hit=" + hit
                    + " snap=" + Math.round(snap)
                    + " switch=" + sinceLast + "ms"
                    + " => " + (reasons.length() > 0 ? "FLAG" : "ok"));
        }

        if (reasons.length() > 0) {
            flag(attacker, data, reasons.toString().trim());
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
}
