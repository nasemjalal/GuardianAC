package com.nasem.guardianac.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketEvent;
import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.data.PlayerData;
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
        adapter = new PacketAdapter(pluginInstance, ListenerPriority.HIGH,
                PacketType.Play.Client.POSITION,
                PacketType.Play.Client.POSITION_LOOK,
                PacketType.Play.Client.LOOK,
                PacketType.Play.Client.FLYING,
                PacketType.Play.Client.ARM_ANIMATION) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null) return;

                PlayerData data = pluginInstance.getPlayerDataManager().get(player);
                if (data == null) return;

                PacketType type = event.getPacketType();

                if (type == PacketType.Play.Client.POSITION
                        || type == PacketType.Play.Client.POSITION_LOOK) {
                    runPacketCheck(CheckType.SPEED, player, data);
                    runPacketCheck(CheckType.FLIGHT, player, data);
                } else if (type == PacketType.Play.Client.ARM_ANIMATION) {
                    data.incrementClicks();
                    runPacketCheck(CheckType.AUTOCLICKER, player, data);
                }
            }
        };

        protocolManager.addPacketListener(adapter);
    }

    public void unregister() {
        if (adapter != null) {
            protocolManager.removePacketListener(adapter);
            adapter = null;
        }
    }

    private void runPacketCheck(CheckType type, Player player, PlayerData data) {
        Check check = pluginInstance.getCheckManager().getCheck(type);
        if (check == null || !check.isEnabled()) return;
        // Packet-level checks will be handled here later
        // For now, existing Bukkit checks still do the work
    }
}
