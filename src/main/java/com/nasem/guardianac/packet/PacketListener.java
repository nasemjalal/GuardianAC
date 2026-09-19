package com.nasem.guardianac.packet;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.event.PacketListenerAbstract;
import com.github.retrooper.packetevents.event.PacketListenerPriority;
import com.github.retrooper.packetevents.event.PacketReceiveEvent;
import com.github.retrooper.packetevents.protocol.packettype.PacketType;
import com.github.retrooper.packetevents.protocol.packettype.PacketTypeCommon;
import com.github.retrooper.packetevents.protocol.player.DiggingAction;
import com.github.retrooper.packetevents.wrapper.play.client.*;
import com.nasem.guardianac.GuardianAC;
import com.nasem.guardianac.check.Check;
import com.nasem.guardianac.check.CheckType;
import com.nasem.guardianac.check.impl.BlockReachCheck;
import com.nasem.guardianac.check.impl.FastBreakCheck;
import com.nasem.guardianac.check.impl.KillAuraCheck;
import com.nasem.guardianac.check.impl.NukerCheck;
import com.nasem.guardianac.data.PlayerData;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class PacketListener {

    private final GuardianAC pluginInstance;

    public PacketListener(GuardianAC plugin) {
        this.pluginInstance = plugin;
    }

    public void register() {
        PacketEvents.getAPI().getEventManager().registerListener(
                new PacketListenerAbstract(PacketListenerPriority.HIGHEST) {
            @Override
            public void onPacketReceive(PacketReceiveEvent event) {
                Player player = event.getPlayer();
                if (player == null) return;

                PlayerData data = pluginInstance.getPlayerDataManager().get(player);
                if (data == null) return;

                PacketTypeCommon type = event.getPacketType();

                if (type == PacketType.Play.Client.PLAYER_ROTATION
                        || type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION) {
                    try {
                        WrapperPlayClientPlayerFlying flyingPacket =
                                new WrapperPlayClientPlayerFlying(event);
                        float yaw = flyingPacket.getLocation().getYaw();
                        float pitch = flyingPacket.getLocation().getPitch();
                        data.setPreviousYaw(data.getCurrentYaw());
                        data.setPreviousPitch(data.getCurrentPitch());
                        data.setCurrentYaw(yaw);
                        data.setCurrentPitch(pitch);
                        data.setLastLookTime(System.currentTimeMillis());
                    } catch (Exception ignored) {}
                }

                if (type == PacketType.Play.Client.ANIMATION) {
                    data.incrementClicks();
                    data.setLastSwingTime(System.currentTimeMillis());
                    handleSwing(player, data);
                }

                if (type == PacketType.Play.Client.INTERACT_ENTITY) {
                    data.setLastEntityAttackTime(System.currentTimeMillis());
                    handleUseEntity(event, player, data);
                }

                if (type == PacketType.Play.Client.PLAYER_DIGGING) {
                    handleBlockDig(event, player, data);
                }

                if (type == PacketType.Play.Client.PLAYER_POSITION
                        || type == PacketType.Play.Client.PLAYER_POSITION_AND_ROTATION
                        || type == PacketType.Play.Client.PLAYER_ROTATION
                        || type == PacketType.Play.Client.PLAYER_FLYING) {
                    data.incrementPacketCount();
                }
            }
        });
    }

    private void handleBlockDig(PacketReceiveEvent event, Player player, PlayerData data) {
        try {
            WrapperPlayClientPlayerDigging packet = new WrapperPlayClientPlayerDigging(event);
            if (packet.getAction() != DiggingAction.START_DIGGING) return;

            Location blockLoc = new Location(player.getWorld(),
                    packet.getBlockPosition().getX(),
                    packet.getBlockPosition().getY(),
                    packet.getBlockPosition().getZ());

            long now = System.currentTimeMillis();

            // ⭐ Nuker packet-level
            Check nukerCheck = pluginInstance.getCheckManager().getCheck(CheckType.NUKER);
            if (nukerCheck instanceof NukerCheck && nukerCheck.isEnabled()) {
                ((NukerCheck) nukerCheck).handlePacketDig(player, data, blockLoc);
            }

            // FastBreak
            long minDelay = pluginInstance.getConfig().getLong("checks.fastbreak.min-delay", 80);
            long lastBreak = data.getLastBlockBreakTime();

            if (lastBreak > 0 && (now - lastBreak) < minDelay) {
                Check check = pluginInstance.getCheckManager().getCheck(CheckType.FASTBREAK);
                if (check instanceof FastBreakCheck && check.isEnabled()) {
                    ((FastBreakCheck) check).handlePacket(player, data, now - lastBreak);
                }
            }

            // BlockReach
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
        } catch (Exception e) {
            // ⭐ debug — إذا فيه خطأ، يظهر في الكونسول
            pluginInstance.getLogger().warning("BlockDig error: " + e.getMessage());
        }
    }

    private void handleSwing(Player player, PlayerData data) {
        Check killAura = pluginInstance.getCheckManager().getCheck(CheckType.KILLAURA);
        if (killAura instanceof KillAuraCheck && killAura.isEnabled()) {
            ((KillAuraCheck) killAura).handleSwing(player, data);
        }
    }

    private void handleUseEntity(PacketReceiveEvent event, Player player, PlayerData data) {
        try {
            WrapperPlayClientInteractEntity packet = new WrapperPlayClientInteractEntity(event);
            if (packet.getAction() != WrapperPlayClientInteractEntity.InteractAction.ATTACK) return;

            int entityId = packet.getEntityId();
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
        PacketEvents.getAPI().getEventManager().unregisterAllListeners();
    }
}
