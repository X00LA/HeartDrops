package at.xoola.heartdrops;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;

/**
 * Manages heart display using ProtocolLib packets.
 * Creates fake armor stands with custom names showing heart symbols.
 */
public class HeartDisplayManager {
    private static final AtomicInteger ENTITY_ID_COUNTER = new AtomicInteger(Integer.MAX_VALUE - 100000);
    private final ProtocolManager protocolManager;
    private final Map<Integer, HeartEntity> activeHearts;
    private final boolean protocolLibAvailable;

    public HeartDisplayManager() {
        this.activeHearts = new ConcurrentHashMap<>();
        
        ProtocolManager tempManager = null;
        boolean tempAvailable = false;
        
        try {
            tempManager = ProtocolLibrary.getProtocolManager();
            tempAvailable = true;
            HeartDrops.instance.getLogger().info("ProtocolLib found! Using packet-based heart display.");
        } catch (Exception e) {
            HeartDrops.instance.getLogger().warning("ProtocolLib not found! Falling back to particle-only display.");
        }
        
        this.protocolManager = tempManager;
        this.protocolLibAvailable = tempAvailable;
    }

    public boolean isProtocolLibAvailable() {
        return protocolLibAvailable;
    }

    /**
     * Spawns a heart display at the given location.
     * @return The entity ID of the spawned heart, or -1 if failed
     */
    public int spawnHeart(Location location, Collection<Player> viewers) {
        if (!protocolLibAvailable || location == null || viewers == null || viewers.isEmpty()) {
            return -1;
        }

        int entityId = ENTITY_ID_COUNTER.getAndDecrement();
        UUID entityUUID = UUID.randomUUID();
        
        HeartEntity heart = new HeartEntity(entityId, entityUUID, location.clone(), new HashSet<>(viewers));
        activeHearts.put(entityId, heart);

        // Send spawn packet to all viewers
        for (Player viewer : viewers) {
            sendSpawnPacket(viewer, heart);
            sendMetadataPacket(viewer, heart);
        }

        return entityId;
    }

    /**
     * Removes a heart display.
     */
    public void removeHeart(int entityId) {
        HeartEntity heart = activeHearts.remove(entityId);
        if (heart != null) {
            for (Player viewer : heart.viewers) {
                if (viewer.isOnline()) {
                    sendDestroyPacket(viewer, entityId);
                }
            }
        }
    }

    /**
     * Clears all active hearts.
     */
    public void clearAll() {
        for (int entityId : new ArrayList<>(activeHearts.keySet())) {
            removeHeart(entityId);
        }
        activeHearts.clear();
    }

    /**
     * Gets the location of a heart entity.
     */
    public Location getHeartLocation(int entityId) {
        HeartEntity heart = activeHearts.get(entityId);
        return heart != null ? heart.location.clone() : null;
    }

    /**
     * Gets all active heart entity IDs.
     */
    public Set<Integer> getActiveHeartIds() {
        return new HashSet<>(activeHearts.keySet());
    }

    private void sendSpawnPacket(Player player, HeartEntity heart) {
        try {
            PacketContainer spawnPacket = protocolManager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
            
            spawnPacket.getIntegers().write(0, heart.entityId);
            spawnPacket.getUUIDs().write(0, heart.uuid);
            spawnPacket.getEntityTypeModifier().write(0, EntityType.ARMOR_STAND);
            
            spawnPacket.getDoubles()
                .write(0, heart.location.getX())
                .write(1, heart.location.getY())
                .write(2, heart.location.getZ());
            
            // Pitch and Yaw as bytes (0-255 range)
            spawnPacket.getBytes()
                .write(0, (byte) 0)  // Pitch
                .write(1, (byte) 0); // Yaw
            
            // Head yaw
            spawnPacket.getBytes().write(2, (byte) 0);
            
            // Data/Object data
            spawnPacket.getIntegers().write(1, 0);

            protocolManager.sendServerPacket(player, spawnPacket);
        } catch (Exception e) {
            HeartDrops.instance.getLogger().warning("Failed to send spawn packet: " + e.getMessage());
        }
    }

    private void sendMetadataPacket(Player player, HeartEntity heart) {
        try {
            PacketContainer metadataPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_METADATA);
            metadataPacket.getIntegers().write(0, heart.entityId);

            List<WrappedDataValue> metadata = new ArrayList<>();
            
            // Entity flags (index 0): invisible + no collision
            metadata.add(new WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte.class), (byte) 0x20));
            
            // Custom name (index 2)
            String heartSymbol = "§c❤";
            metadata.add(new WrappedDataValue(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), 
                Optional.of(WrappedChatComponent.fromText(heartSymbol).getHandle())));
            
            // Custom name visible (index 3)
            metadata.add(new WrappedDataValue(3, WrappedDataWatcher.Registry.get(Boolean.class), true));
            
            // Armor stand flags (index 15): small (0x01) + marker (0x10) + no base plate (0x08)
            metadata.add(new WrappedDataValue(15, WrappedDataWatcher.Registry.get(Byte.class), (byte) (0x01 | 0x08 | 0x10)));

            metadataPacket.getDataValueCollectionModifier().write(0, metadata);

            protocolManager.sendServerPacket(player, metadataPacket);
        } catch (Exception e) {
            HeartDrops.instance.getLogger().warning("Failed to send metadata packet: " + e.getMessage());
        }
    }

    private void sendDestroyPacket(Player player, int entityId) {
        try {
            PacketContainer destroyPacket = protocolManager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
            destroyPacket.getIntLists().write(0, Collections.singletonList(entityId));
            
            protocolManager.sendServerPacket(player, destroyPacket);
        } catch (Exception e) {
            HeartDrops.instance.getLogger().warning("Failed to send destroy packet: " + e.getMessage());
        }
    }

    private static class HeartEntity {
        final int entityId;
        final UUID uuid;
        final Location location;
        final Set<Player> viewers;

        HeartEntity(int entityId, UUID uuid, Location location, Set<Player> viewers) {
            this.entityId = entityId;
            this.uuid = uuid;
            this.location = location;
            this.viewers = viewers;
        }
    }
}
