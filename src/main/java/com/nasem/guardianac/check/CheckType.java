package com.nasem.guardianac.check;

public enum CheckType {

    SPEED("Speed", "speed"),
    FLIGHT("Flight", "flight"),
    REACH("Reach", "reach"),
    KILLAURA("KillAura", "killaura"),
    AUTOCLICKER("AutoClicker", "autoclicker"),
    NOFALL("NoFall", "nofall"),
    JESUS("Jesus", "jesus"),
    STEP("Step", "step"),
    NOSLOW("NoSlow", "noslow"),
    TIMER("Timer", "timer"),
    FASTPLACE("FastPlace", "fastplace"),
    SCAFFOLD("Scaffold", "scaffold"),
    CRITICALS("Criticals", "criticals"),
    VELOCITY("Velocity", "velocity"),
    PHASE("Phase", "phase"),
    INVENTORYMOVE("InventoryMove", "inventorymove"),
    FASTBREAK("FastBreak", "fastbreak"),
    BLOCKREACH("BlockReach", "blockreach"),
    NUKER("Nuker", "nuker"),
    WALLHACK("Wallhack", "wallhack"),
    ELYTRA("Elytra", "elytra");

    private final String name;
    private final String configKey;

    CheckType(String name, String configKey) {
        this.name = name;
        this.configKey = configKey;
    }

    public String getName() {
        return name;
    }

    public String getConfigKey() {
        return configKey;
    }
}
