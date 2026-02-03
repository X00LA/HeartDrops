package at.xoola.heartdrops

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.reflect.FieldAccessException
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.comphenix.protocol.wrappers.WrappedDataValue
import com.comphenix.protocol.wrappers.WrappedDataWatcher
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * Manages heart display using ProtocolLib packets.
 * Creates fake armor stands with custom names showing heart symbols.
 */
class HeartDisplayManager {
    private val protocolManager: ProtocolManager?
    private val activeHearts = ConcurrentHashMap<Int, HeartEntity>()
    val isProtocolLibAvailable: Boolean

    init {
        var tempManager: ProtocolManager? = null
        var tempAvailable = false
        
        try {
            tempManager = ProtocolLibrary.getProtocolManager()
            tempAvailable = true
            HeartDrops.instance?.logger?.info("\u001B[32mProtocolLib found! Using packet-based heart display.\u001B[0m")
        } catch (e: Exception) {
            HeartDrops.instance?.logger?.warning("\u001B[31mProtocolLib not found! Falling back to particle-only display.\u001B[0m")
        }
        
        protocolManager = tempManager
        isProtocolLibAvailable = tempAvailable
    }

    /**
     * Spawns a heart display at the given location.
     * @return The entity ID of the spawned heart, or -1 if failed
     */
    fun spawnHeart(location: Location, viewers: Collection<Player>): Int {
        if (!isProtocolLibAvailable || viewers.isEmpty()) {
            return -1
        }

        val entityId = ENTITY_ID_COUNTER.getAndDecrement()
        val entityUUID = UUID.randomUUID()
        
        val heart = HeartEntity(entityId, entityUUID, location.clone(), viewers.toHashSet())
        activeHearts[entityId] = heart

        // Send spawn packet to all viewers
        for (viewer in viewers) {
            sendSpawnPacket(viewer, heart)
            sendMetadataPacket(viewer, heart)
        }

        return entityId
    }

    /**
     * Removes a heart display.
     */
    fun removeHeart(entityId: Int) {
        val heart = activeHearts.remove(entityId) ?: return
        for (viewer in heart.viewers) {
            if (viewer.isOnline) {
                sendDestroyPacket(viewer, entityId)
            }
        }
    }

    /**
     * Clears all active hearts.
     */
    fun clearAll() {
        val entityIds = activeHearts.keys.toList()
        for (entityId in entityIds) {
            removeHeart(entityId)
        }
        activeHearts.clear()
    }

    /**
     * Gets the location of a heart entity.
     */
    fun getHeartLocation(entityId: Int): Location? {
        return activeHearts[entityId]?.location?.clone()
    }

    /**
     * Gets all active heart entity IDs.
     */
    fun getActiveHeartIds(): Set<Int> {
        return activeHearts.keys.toHashSet()
    }

    private fun sendSpawnPacket(player: Player, heart: HeartEntity) {
        try {
            val spawnPacket = protocolManager?.createPacket(PacketType.Play.Server.SPAWN_ENTITY) ?: return
            
            spawnPacket.integers.write(0, heart.entityId)
            spawnPacket.uuiDs.write(0, heart.uuid)
            spawnPacket.entityTypeModifier.write(0, EntityType.ARMOR_STAND)
            
            spawnPacket.doubles
                .write(0, heart.location.x)
                .write(1, heart.location.y)
                .write(2, heart.location.z)
            
            // Pitch and Yaw as bytes (0-255 range)
            spawnPacket.bytes
                .write(0, 0.toByte())  // Pitch
                .write(1, 0.toByte())  // Yaw
            
            // Head yaw
            spawnPacket.bytes.write(2, 0.toByte())
            
            // Data/Object data
            spawnPacket.integers.write(1, 0)

            protocolManager.sendServerPacket(player, spawnPacket)
        } catch (e: FieldAccessException) {
            HeartDrops.instance?.logger?.warning { "\u001B[31mFailed to send spawn packet: ${e.message}\u001B[0m" }
        } catch (e: IllegalArgumentException) {
            HeartDrops.instance?.logger?.warning { "\u001B[31mFailed to send spawn packet: ${e.message}\u001B[0m" }
        }
    }

    private fun sendMetadataPacket(player: Player, heart: HeartEntity) {
        try {
            val metadataPacket = protocolManager?.createPacket(PacketType.Play.Server.ENTITY_METADATA) ?: return
            metadataPacket.integers.write(0, heart.entityId)

            val metadata = mutableListOf<WrappedDataValue>()
            
            // Entity flags (index 0): invisible + no collision
            metadata.add(WrappedDataValue(0, WrappedDataWatcher.Registry.get(Byte::class.java), 0x20.toByte()))
            
            // Custom name (index 2)
            val heartSymbol = "§c❤"
            metadata.add(WrappedDataValue(2, WrappedDataWatcher.Registry.getChatComponentSerializer(true), 
                Optional.of(WrappedChatComponent.fromText(heartSymbol).handle)))
            
            // Custom name visible (index 3)
            metadata.add(WrappedDataValue(3, WrappedDataWatcher.Registry.get(Boolean::class.java), true))
            
            // Armor stand flags (index 15): small (0x01) + marker (0x10) + no base plate (0x08)
            metadata.add(WrappedDataValue(15, WrappedDataWatcher.Registry.get(Byte::class.java), (0x01 or 0x08 or 0x10).toByte()))

            metadataPacket.dataValueCollectionModifier.write(0, metadata)

            protocolManager.sendServerPacket(player, metadataPacket)
        } catch (e: FieldAccessException) {
            HeartDrops.instance?.logger?.warning { "\u001B[31mFailed to send metadata packet: ${e.message}\u001B[0m" }
        } catch (e: IllegalArgumentException) {
            HeartDrops.instance?.logger?.warning { "\u001B[31mFailed to send metadata packet: ${e.message}\u001B[0m" }
        }
    }

    private fun sendDestroyPacket(player: Player, entityId: Int) {
        try {
            val destroyPacket = protocolManager?.createPacket(PacketType.Play.Server.ENTITY_DESTROY) ?: return
            destroyPacket.intLists.write(0, listOf(entityId))
            
            protocolManager.sendServerPacket(player, destroyPacket)
        } catch (e: FieldAccessException) {
            HeartDrops.instance?.logger?.warning { "\u001B[31mFailed to send destroy packet: ${e.message}\u001B[0m" }
        } catch (e: IllegalArgumentException) {
            HeartDrops.instance?.logger?.warning { "\u001B[31mFailed to send destroy packet: ${e.message}\u001B[0m" }
        }
    }

    private data class HeartEntity(
        val entityId: Int,
        val uuid: UUID,
        val location: Location,
        val viewers: HashSet<Player>
    )

    companion object {
        private val ENTITY_ID_COUNTER = AtomicInteger(Int.MAX_VALUE - 100000)
    }
}
