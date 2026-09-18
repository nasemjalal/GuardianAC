package com.nasem.guardianac.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.check.impl.KillAuraCheck;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PacketListener {

    private final GuardianAC pluginInstance;
    private final ProtocolManager protocolManager;
    private PacketAdapter adapter;

    public PacketListener(GuardianAC plugin) {
        this.pluginInstance = plugin;
        this.protocolManager = plugin.getProtocolManager();
    }

    public void register() {
        adapter = new PacketAdapter(pluginInstance, ListenerPriority.HIGHEST,
                PacketType.Play.Client.POSITION,
                PacketType.Play.Client.POSITION_LOOK,
                PacketType.Play.Client.LOOK,
                PacketType.Play.Client.FLYING,
                PacketType.Play.Client.ARM_ANIMATION,
                PacketType.Play.Client.USE_ENTITY) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null) return;

                PlayerData data = pluginInstance.getPlayerDataManager().get(player);
                if (data == null) return;

                PacketType type = event.getPacketType();

                if (type == PacketType.Play.Client.ARM_ANIMATION) {
                    data.incrementClicks();
                    // ⭐ KillAura من Swing
                    handleSwing(player, data);
                } else if (type == PacketType.Play.Client.USE_ENTITY) {
                    handleUseEntity(event, player, data);
                }

                if (type == PacketType.Play.Client.POSITION
                        || type == PacketType.Play.Client.POSITION_LOOK
                        || type == PacketType.Play.Client.LOOK
                        || type == PacketType.Play.Client.FLYING) {
                    data.incrementPacketCount();
                }
            }
        };

        protocolManager.addPacketListener(adapter);
    }

    /**
     * ⭐ KillAura من USE_ENTITY
     */
    private void handleUseEntity(PacketEvent event, Player player, PlayerData data) {
        try {
            PacketContainer packet = event.getPacket();
            EnumWrappers.EntityUseAction action = packet.getEntityUseActions().read(0);
            if (action != EnumWrappers.EntityUseAction.ATTACK) return;

            int entityId = packet.getIntegers().read(0);
            Entity target = null;
            for (Entity e : player.getWorld().getEntities()) {
                if (e.getEntityId() == entityId) {
                    target = e;
                    break;
                }
            }
            if (target == null) return;

            Check check = pluginInstance.getCheckManager().getCheck(CheckType.KILLAURA);
            if (check instanceof KillAuraCheck && check.isEnabled()) {
                ((KillAuraCheck) check).handlePacket(player, data, target);
            }
        } catch (Exception ignored) {}
    }

    /**
     * ⭐ KillAura من ARM_ANIMATION
     */
    private void handleSwing(Player player, PlayerData data) {
        Check check = pluginInstance.getCheckManager().getCheck(CheckType.KILLAURA);
        if (check instanceof KillAuraCheck && check.isEnabled()) {
            ((KillAuraCheck) check).handleSwing(player, data);
        }
    }

    public void unregister() {
        if (adapter != null) {
            protocolManager.removePacketListener(adapter);
            adapter = null;
        }
    }
}
