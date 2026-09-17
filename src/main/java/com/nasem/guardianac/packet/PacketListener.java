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

    private final GuardianAC plugin;
    private final ProtocolManager protocolManager;
    private PacketAdapter adapter;

    public PacketListener(GuardianAC plugin) {
        this.plugin = plugin;
        this.protocolManager = plugin.getProtocolManager();
    }

    public void register() {
        adapter = new PacketAdapter(plugin, ListenerPriority.HIGH,
                PacketType.Play.Client.POSITION,
                PacketType.Play.Client.POSITION_LOOK,
                PacketType.Play.Client.LOOK,
                PacketType.Play.Client.FLYING,
                PacketType.Play.Client.ARM_ANIMATION) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null) return;

                PlayerData data = plugin.getPlayerDataManager().get(player);
                if (data == null) return;

                PacketType type = event.getPacketType();

                if (type == PacketType.Play.Client.POSITION
                        || type == PacketType.Play.Client.POSITION_LOOK) {
                    // Player sent position packet
                    runPacketCheck(CheckType.SPEED, player, data, event);
                    runPacketCheck(CheckType.FLIGHT, player, data, event);
                } else if (type == PacketType.Play.Client.ARM_ANIMATION) {
                    // Player swung arm (attack)
                    data.incrementClicks();
                    runPacketCheck(CheckType.AUTOCLICKER, player, data, event);
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

    private void runPacketCheck(CheckType type, Player player, PlayerData data, PacketEvent event) {
        Check check = plugin.getCheckManager().getCheck(type);
        if (check == null || !check.isEnabled()) return;
        // Packet-level checks will be handled in their own classes later
        // For now, Bukkit checks still work
    }
}
