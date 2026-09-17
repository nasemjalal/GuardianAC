package com.nasem.guardianac.data;

import com.nasem.guardianac.check.CheckType;
import org.bukkit.Location;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

public class PlayerData {

    private final UUID uuid;
    private final String name;

    // Violations per check
    private final Map<CheckType, Integer> violations = new EnumMap<>(CheckType.class);

    // Last tick timestamp for each check (for decay)
    private final Map<CheckType, Long> lastFlagTime = new EnumMap<>(CheckType.class);

    // Movement tracking
    private Location lastLocation;
    private Location lastGroundLocation;
    private long lastMoveTime;

    // Air ticks counter (for Flight check)
    private int airTicks;

    // Ground ticks counter
    private int groundTicks;

    // Combat tracking
    private long lastAttackTime;
    private int clicksThisSecond;
    private long clickWindowStart;
    private Location lastAttackLocation;

    // Block interaction tracking
    private long lastBlockBreakTime;
    private long lastBlockPlaceTime;
    private Location lastBlockBreakLocation;

    // Velocity tracking
    private long lastVelocityTime;
    private boolean pendingVelocity;

    // Timer balance
    private long lastTimerCheck;
    private int timerBalance;

    // Inventory state
    private boolean inventoryOpen;

    public PlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        for (CheckType type : CheckType.values()) {
            violations.put(type, 0);
            lastFlagTime.put(type, 0L);
        }
        this.clickWindowStart = System.currentTimeMillis();
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    // ============ Violations ============

    public int getViolation(CheckType type) {
        return violations.getOrDefault(type, 0);
    }

    public void addViolation(CheckType type) {
        violations.put(type, getViolation(type) + 1);
        lastFlagTime.put(type, System.currentTimeMillis());
    }

    public void setViolation(CheckType type, int amount) {
        violations.put(type, Math.max(0, amount));
    }

    public void resetViolation(CheckType type) {
        violations.put(type, 0);
    }

    public void resetAllViolations() {
        for (CheckType type : CheckType.values()) {
            violations.put(type, 0);
        }
    }

    /**
     * Decay violations based on time passed since last flag.
     * Called periodically.
     */
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

    // ============ Movement ============

    public Location getLastLocation() {
        return lastLocation;
    }

    public void setLastLocation(Location loc) {
        this.lastLocation = loc;
    }

    public Location getLastGroundLocation() {
        return lastGroundLocation;
    }

    public void setLastGroundLocation(Location loc) {
        this.lastGroundLocation = loc;
    }

    public long getLastMoveTime() {
        return lastMoveTime;
    }

    public void setLastMoveTime(long time) {
        this.lastMoveTime = time;
    }

    public int getAirTicks() {
        return airTicks;
    }

    public void incrementAirTicks() {
        this.airTicks++;
    }

    public void resetAirTicks() {
        this.airTicks = 0;
    }

    public int getGroundTicks() {
        return groundTicks;
    }

    public void incrementGroundTicks() {
        this.groundTicks++;
    }

    public void resetGroundTicks() {
        this.groundTicks = 0;
    }

    // ============ Combat ============

    public long getLastAttackTime() {
        return lastAttackTime;
    }

    public void setLastAttackTime(long time) {
        this.lastAttackTime = time;
    }

    public int getClicksThisSecond() {
        return clicksThisSecond;
    }

    public void incrementClicks() {
        long now = System.currentTimeMillis();
        if (now - clickWindowStart >= 1000L) {
            clicksThisSecond = 0;
            clickWindowStart = now;
        }
        clicksThisSecond++;
    }

    public long getClickWindowStart() {
        return clickWindowStart;
    }

    public Location getLastAttackLocation() {
        return lastAttackLocation;
    }

    public void setLastAttackLocation(Location loc) {
        this.lastAttackLocation = loc;
    }

    // ============ Block Interaction ============

    public long getLastBlockBreakTime() {
        return lastBlockBreakTime;
    }

    public void setLastBlockBreakTime(long time) {
        this.lastBlockBreakTime = time;
    }

    public long getLastBlockPlaceTime() {
        return lastBlockPlaceTime;
    }

    public void setLastBlockPlaceTime(long time) {
        this.lastBlockPlaceTime = time;
    }

    public Location getLastBlockBreakLocation() {
        return lastBlockBreakLocation;
    }

    public void setLastBlockBreakLocation(Location loc) {
        this.lastBlockBreakLocation = loc;
    }

    // ============ Velocity ============

    public long getLastVelocityTime() {
        return lastVelocityTime;
    }

    public void setLastVelocityTime(long time) {
        this.lastVelocityTime = time;
    }

    public boolean isPendingVelocity() {
        return pendingVelocity;
    }

    public void setPendingVelocity(boolean pending) {
        this.pendingVelocity = pending;
    }

    // ============ Timer ============

    public long getLastTimerCheck() {
        return lastTimerCheck;
    }

    public void setLastTimerCheck(long time) {
        this.lastTimerCheck = time;
    }

    public int getTimerBalance() {
        return timerBalance;
    }

    public void addTimerBalance(int amount) {
        this.timerBalance += amount;
    }

    public void setTimerBalance(int amount) {
        this.timerBalance = amount;
    }

    // ============ Inventory ============

    public boolean isInventoryOpen() {
        return inventoryOpen;
    }

    public void setInventoryOpen(boolean open) {
        this.inventoryOpen = open;
    }
}
