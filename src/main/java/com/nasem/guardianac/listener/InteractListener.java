package com.nasem.guardianac.listener;

import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.check.impl.ScaffoldCheck;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class InteractListener implements Listener {

    private final GuardianAC plugin;

    public InteractListener(GuardianAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getHand() != EquipmentSlot.HAND) return;

        Check check = plugin.getCheckManager().getCheck(CheckType.SCAFFOLD);
        if (!(check instanceof ScaffoldCheck) || !check.isEnabled()) return;

        try {
            Player player = event.getPlayer();
            PlayerData data = plugin.getPlayerDataManager().get(player);
            ((ScaffoldCheck) check).handleInteract(player, event, data);
        } catch (Exception ex) {
            plugin.getLogger().warning("[Scaffold] interact error: " + ex);
        }
    }
}
