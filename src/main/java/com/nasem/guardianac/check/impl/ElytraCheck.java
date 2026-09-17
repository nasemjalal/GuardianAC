package com.nasem.guardianac.check.impl;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class ElytraCheck extends Check {

    public ElytraCheck(GuardianAC plugin) {
        super(plugin, CheckType.ELYTRA);
    }

    public void handle(Player player, PlayerData data, Location from, Location to) {
        if (!enabled) return;

        // Player claims to be gliding but not wearing elytra
        if (player.isGliding()) {
            ItemStack chest = player.getInventory().getChestplate();
            if (chest == null || chest.getType() != Material.ELYTRA) {
                flag(player, data, "gliding without elytra");
            }
        }
    }
}
