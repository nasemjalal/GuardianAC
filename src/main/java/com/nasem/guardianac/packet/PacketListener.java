package com.nasem.guardianac.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.ListenerPriority;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.check.impl.BlockReachCheck;
import com.nasem.guardianac.check.impl.FastBreakCheck;
import com.nasem.guardianac.check.impl.FastPlaceCheck;
import com.nasem.guardianac.check.impl.KillAuraCheck;
import com.nasem.guardianac.check.impl.NukerCheck;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Location;
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
                PacketType.Play.Client.USE_ENTITY,
                PacketType.Play.Client.BLOCK_DIG,
                PacketType.Play.Client.BLOCK_PLACE) {

            @Override
            public void onPacketReceiving(PacketEvent event) {
                Player player = event.getPlayer();
                if (player == null) return;

                PlayerData data = pluginInstance.getPlayerDataManager().get(player);
                if (data == null) return;

                PacketType type = event.getPacketType();

                if (type == PacketType.Play.Client.POSITION_LOOK
                        || type == PacketType.Play.Client.LOOK) {
                    try {
                        PacketContainer packet = event.getPacket();
                        float yaw = packet.getFloat().read(0);
                        float pitch = packet.getFloat().read(1);

                        data.setPreviousYaw(data.getCurrentYaw());
                        data.setPreviousPitch(data.getCurrentPitch());
                        data.setCurrentYaw(yaw);
                        data.setCurrentPitch(pitch);
                        data.setLastLookTime(System.currentTimeMillis());
                    } catch (Exception ignored) {}
                }

                if (type == PacketType.Play.Client.ARM_ANIMATION) {
                    data.incrementClicks();
                    handleSwing(player, data);
                } else if (type == PacketType.Play.Client.USE_ENTITY) {
                    handleUseEntity(event, player, data);
                } else if (type == PacketType.Play.Client.BLOCK_DIG) {
                    handleBlockDig(event, player, data);
                } else if (type == PacketType.Play.Client.BLOCK_PLACE) {
                    handleBlockPlace(event, player, data);
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
     * ⭐⭐ BLOCK_DIG — FastBreak + Nuker + BlockReach
     */
    private void handleBlockDig(PacketEvent event, Player player, PlayerData data) {
        try {
            PacketContainer packet = event.getPacket();
            EnumWrappers.PlayerDigType digType = packet.getPlayerDigTypes().read(0);

            // فقط START_DESTROY_BLOCK
            if (digType != EnumWrappers.PlayerDigType.START_DESTROY_BLOCK) return;

            BlockPosition pos = packet.getBlockPositionModifier().read(0);
            Location blockLoc = new Location(player.getWorld(),
                    pos.getX(), pos.getY(), pos.getZ());

            long now = System.currentTimeMillis();

            // ⭐ FastBreak
            long minDelay = pluginInstance.getConfig().getLong("checks.fastbreak.min-delay", 100);
            long lastBreak = data.getLastBlockBreakTime();

            if (lastBreak > 0 && (now - lastBreak) < minDelay) {
                Check check = pluginInstance.getCheckManager().getCheck(CheckType.FASTBREAK);
                if (check instanceof FastBreakCheck && check.isEnabled()) {
                    ((FastBreakCheck) check).handlePacket(player, data, now - lastBreak);
                }
            }

            // ⭐ Nuker — كسر بلوكات متباعدة
            Location lastBreakLoc = data.getLastBlockBreakLocation();
            if (lastBreakLoc != null && lastBreak > 0 && (now - lastBreak) < 200) {
                double dist = blockLoc.distance(lastBreakLoc);
                if (dist > 2.0) {
                    Check check = pluginInstance.getCheckManager().getCheck(CheckType.NUKER);
                    if (check instanceof NukerCheck && check.isEnabled()) {
                        ((NukerCheck) check).handlePacket(player, data, dist, now - lastBreak);
                    }
                }
            }

            // ⭐ BlockReach — بلوك بعيد
            double maxReach = pluginInstance.getConfig().getDouble("checks.blockreach.max-reach", 4.5);
            double reach = player.getEyeLocation().distance(blockLoc.clone().add(0.5, 0.5, 0.5));
            if (reach > maxReach) {
                Check check = pluginInstance.getCheckManager().getCheck(CheckType.BLOCKREACH);
                if (check instanceof BlockReachCheck && check.isEnabled()) {
                    ((BlockReachCheck) check).handlePacketBreak(player, data, reach);
                }
            }

            data.setLastBlockBreakTime(now);
            data.setLastBlockBreakLocation(blockLoc);
        } catch (Exception ignored) {}
    }

    /**
     * ⭐⭐ BLOCK_PLACE — FastPlace + BlockReach
     */
    private void handleBlockPlace(PacketEvent event, Player player, PlayerData data) {
        try {
            PacketContainer packet = event.getPacket();
            BlockPosition pos = packet.getBlockPositionModifier().read(0);
            Location blockLoc = new Location(player.getWorld(),
                    pos.getX(), pos.getY(), pos.getZ());

            long now = System.currentTimeMillis();

            // ⭐ FastPlace
            long minDelay = pluginInstance.getConfig().getLong("checks.fastplace.min-delay", 80);
            long lastPlace = data.getLastBlockPlaceTime();

            if (lastPlace > 0 && (now - lastPlace) < minDelay) {
                Check check = pluginInstance.getCheckManager().getCheck(CheckType.FASTPLACE);
                if (check instanceof FastPlaceCheck && check.isEnabled()) {
                    ((FastPlaceCheck) check).handlePacket(player, data, now - lastPlace);
                }
            }

            // ⭐ BlockReach (place)
            double maxReach = pluginInstance.getConfig().getDouble("checks.blockreach.max-reach", 4.5);
            double reach = player.getEyeLocation().distance(blockLoc.clone().add(0.5, 0.5, 0.5));
            if (reach > maxReach) {
                Check check = pluginInstance.getCheckManager().getCheck(CheckType.BLOCKREACH);
                if (check instanceof BlockReachCheck && check.isEnabled()) {
                    ((BlockReachCheck) check).handlePacketPlace(player, data, reach);
                }
            }

            data.setLastBlockPlaceTime(now);
            data.setLastBlockPlaceLocation(blockLoc);
        } catch (Exception ignored) {}
    }

    private void handleSwing(Player player, PlayerData data) {
        Check killAura = pluginInstance.getCheckManager().getCheck(CheckType.KILLAURA);
        if (killAura instanceof KillAuraCheck && killAura.isEnabled()) {
            ((KillAuraCheck) killAura).handleSwing(player, data);
        }
    }

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

    public void unregister() {
        if (adapter != null) {
            protocolManager.removePacketListener(adapter);
            adapter = null;
        }
    }
}
