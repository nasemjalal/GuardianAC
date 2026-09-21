package com.nasem.guardianac.data;

import com.nasem.guardianac.check.CheckType;
import org.bukkit.Location;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private final String name;

    private final Map<CheckType, Integer> violations = new EnumMap<>(CheckType.class);
    private final Map<CheckType, Long> lastFlagTime = new EnumMap<>(CheckType.class);

    private Location lastLocation;
    private Location lastGroundLocation;
    private long lastMoveTime;

    private int airTicks;
    private int groundTicks;

    private long lastAttackTime;
    private int clicksThisSecond;
    private long clickWindowStart;
    private Location lastAttackLocation;

    // ⭐ تتبع الكليكات
    private final Queue<Long> clickIntervals = new LinkedList<>();
    private long lastEntityClickTime;

    // ⭐⭐⭐ GCD Rotation Check
    private final Deque<Double> rotationDeltas = new ArrayDeque<>();
    private float lastYawGCD = 0;
    private UUID lastTargetId;
    private long lastTargetTime;
    private long attackWindowStart;
    private int attackCount;

    private long lastSwingTime;
    private long lastEntityAttackTime;

    private float currentYaw;
    private float previousYaw;
    private float currentPitch;
    private float previousPitch;
    private long lastLookTime;

    private long lastBlockBreakTime;
    private long lastBlockPlaceTime;
    private Location lastBlockBreakLocation;
    private Location lastBlockPlaceLocation;
    private int fastPlaceStreak;
    private int breakCountInSecond;
    private long breakWindowStart;

    private long lastNukerFlagTime;
    private long lastFastBreakFlagTime;

    private long lastVelocityTime;
    private boolean pendingVelocity;

    private long lastTimerCheck;
    private int timerBalance;
    private int packetCount;
    private long packetWindowStart;

    private boolean inventoryOpen;
    private long lastInvOpenTime;

    private double lastMovementSpeed;
    private long lastActualMoveTime;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        for (CheckType type : CheckType.values()) {
            violations.put(type, 0);
            lastFlagTime.put(type, 0L);
        }
        this.clickWindowStart = System.currentTimeMillis();
        this.packetWindowStart = System.currentTimeMillis();
        this.breakWindowStart = System.currentTimeMillis();
    }

    public UUID getUuid() { return uuid; }
    public String getName() { return name; }

    public int getViolation(CheckType type) { return violations.getOrDefault(type, 0); }
    public void addViolation(CheckType type) {
        violations.put(type, getViolation(type) + 1);
        lastFlagTime.put(type, System.currentTimeMillis());
    }
    public void setViolation(CheckType type, int amount) { violations.put(type, Math.max(0, amount)); }
    public void resetViolation(CheckType type) { violations.put(type, 0); }
    public void resetAllViolations() {
        for (CheckType type : CheckType.values()) violations.put(type, 0);
    }
    public void decayViolations(int decaySeconds) {
        long now = System.currentTimeMillis();
        for (CheckType type : CheckType.values()) {
            int vl = violations.getOrDefault(type, 0);
            if (vl <= 0) continue;
            long lastFlag = lastFlagTime.getOrDefault(type, 0L);
            long elapsed = (now - lastFlag) / 1000L;
            if (elapsed >= decaySeconds) {
                violations.put(type, Math.max(0, vl - 1));
                lastFlagTime.put(type, now);
            }
        }
    }

    // ⭐ تتبع الكليكات
    public void addClickInterval(long interval) {
        clickIntervals.add(interval);
        if (clickIntervals.size() > 20) {
            clickIntervals.poll();
        }
    }

    public Queue<Long> getClickIntervals() { return clickIntervals; }

    public double getClickIntervalVariance() {
        if (clickIntervals.size() < 5) return -1;
        double sum = 0;
        for (long v : clickIntervals) sum += v;
        double mean = sum / clickIntervals.size();
        double variance = 0;
        for (long v : clickIntervals) variance += Math.pow(v - mean, 2);
        variance /= clickIntervals.size();
        return Math.sqrt(variance);
    }

    public long getLastEntityClickTime() { return lastEntityClickTime; }
    public void setLastEntityClickTime(long time) { this.lastEntityClickTime = time; }

    // ⭐⭐⭐ GCD Rotation
    public Deque<Double> getRotationDeltas() { return rotationDeltas; }
    public float getLastYawGCD() { return lastYawGCD; }
    public void setLastYawGCD(float yaw) { this.lastYawGCD = yaw; }
    public UUID getLastTargetId() { return lastTargetId; }
    public long getLastTargetTime() { return lastTargetTime; }
    public void setLastTargetTime(long t) { this.lastTargetTime = t; }
    public long getAttackWindowStart() { return attackWindowStart; }
    public void setAttackWindowStart(long t) { this.attackWindowStart = t; }
    public int getAttackCount() { return attackCount; }
    public void setAttackCount(int c) { this.attackCount = c; }
    public void setLastTargetId(UUID id) { this.lastTargetId = id; }

    public Location getLastLocation() { return lastLocation; }
    public void setLastLocation(Location loc) { this.lastLocation = loc; }
    public Location getLastGroundLocation() { return lastGroundLocation; }
    public void setLastGroundLocation(Location loc) { this.lastGroundLocation = loc; }
    public long getLastMoveTime() { return lastMoveTime; }
    public void setLastMoveTime(long time) { this.lastMoveTime = time; }
    public int getAirTicks() { return airTicks; }
    public void incrementAirTicks() { this.airTicks++; }
    public void resetAirTicks() { this.airTicks = 0; }
    public int getGroundTicks() { return groundTicks; }
    public void incrementGroundTicks() { this.groundTicks++; }
    public void resetGroundTicks() { this.groundTicks = 0; }

    public long getLastAttackTime() { return lastAttackTime; }
    public void setLastAttackTime(long time) { this.lastAttackTime = time; }
    public int getClicksThisSecond() { return clicksThisSecond; }
    public void incrementClicks() {
        long now = System.currentTimeMillis();
        if (now - clickWindowStart >= 1000L) {
            clicksThisSecond = 0;
            clickWindowStart = now;
        }
        clicksThisSecond++;
    }
    public long getClickWindowStart() { return clickWindowStart; }
    public Location getLastAttackLocation() { return lastAttackLocation; }
    public void setLastAttackLocation(Location loc) { this.lastAttackLocation = loc; }

    public long getLastSwingTime() { return lastSwingTime; }
    public void setLastSwingTime(long time) { this.lastSwingTime = time; }
    public long getLastEntityAttackTime() { return lastEntityAttackTime; }
    public void setLastEntityAttackTime(long time) { this.lastEntityAttackTime = time; }

    public float getCurrentYaw() { return currentYaw; }
    public void setCurrentYaw(float yaw) { this.currentYaw = yaw; }
    public float getPreviousYaw() { return previousYaw; }
    public void setPreviousYaw(float yaw) { this.previousYaw = yaw; }
    public float getCurrentPitch() { return currentPitch; }
    public void setCurrentPitch(float pitch) { this.currentPitch = pitch; }
    public float getPreviousPitch() { return previousPitch; }
    public void setPreviousPitch(float pitch) { this.previousPitch = pitch; }
    public long getLastLookTime() { return lastLookTime; }
    public void setLastLookTime(long time) { this.lastLookTime = time; }

    public long getLastBlockBreakTime() { return lastBlockBreakTime; }
    public void setLastBlockBreakTime(long time) { this.lastBlockBreakTime = time; }
    public long getLastBlockPlaceTime() { return lastBlockPlaceTime; }
    public void setLastBlockPlaceTime(long time) { this.lastBlockPlaceTime = time; }
    public Location getLastBlockBreakLocation() { return lastBlockBreakLocation; }
    public void setLastBlockBreakLocation(Location loc) { this.lastBlockBreakLocation = loc; }
    public Location getLastBlockPlaceLocation() { return lastBlockPlaceLocation; }
    public void setLastBlockPlaceLocation(Location loc) { this.lastBlockPlaceLocation = loc; }
    public int getFastPlaceStreak() { return fastPlaceStreak; }
    public void setFastPlaceStreak(int streak) { this.fastPlaceStreak = streak; }

    public int getBreakCountInSecond() { return breakCountInSecond; }
    public void setBreakCountInSecond(int count) { this.breakCountInSecond = count; }
    public long getBreakWindowStart() { return breakWindowStart; }
    public void setBreakWindowStart(long time) { this.breakWindowStart = time; }

    public long getLastNukerFlagTime() { return lastNukerFlagTime; }
    public void setLastNukerFlagTime(long time) { this.lastNukerFlagTime = time; }
    public boolean isNukerActive() {
        return System.currentTimeMillis() - lastNukerFlagTime < 3000;
    }

    public long getLastFastBreakFlagTime() { return lastFastBreakFlagTime; }
    public void setLastFastBreakFlagTime(long time) { this.lastFastBreakFlagTime = time; }
    public boolean isFastBreakActive() {
        return System.currentTimeMillis() - lastFastBreakFlagTime < 2000;
    }

    public long getLastVelocityTime() { return lastVelocityTime; }
    public void setLastVelocityTime(long time) { this.lastVelocityTime = time; }
    public boolean isPendingVelocity() { return pendingVelocity; }
    public void setPendingVelocity(boolean pending) { this.pendingVelocity = pending; }

    public long getLastTimerCheck() { return lastTimerCheck; }
    public void setLastTimerCheck(long time) { this.lastTimerCheck = time; }
    public int getTimerBalance() { return timerBalance; }
    public void addTimerBalance(int amount) { this.timerBalance += amount; }
    public void setTimerBalance(int amount) { this.timerBalance = amount; }
    public int getPacketCount() { return packetCount; }
    public void incrementPacketCount() { this.packetCount++; }
    public long getPacketWindowStart() { return packetWindowStart; }
    public void setPacketWindowStart(long time) {
        this.packetWindowStart = time;
        this.packetCount = 0;
    }

    public boolean isInventoryOpen() { return inventoryOpen; }
    public void setInventoryOpen(boolean open) { this.inventoryOpen = open; }
    public long getLastInvOpenTime() { return lastInvOpenTime; }
    public void setLastInvOpenTime(long time) { this.lastInvOpenTime = time; }

    public double getLastMovementSpeed() { return lastMovementSpeed; }
    public void setLastMovementSpeed(double speed) { this.lastMovementSpeed = speed; }
    public long getLastActualMoveTime() { return lastActualMoveTime; }
    public void setLastActualMoveTime(long time) { this.lastActualMoveTime = time; }
}
