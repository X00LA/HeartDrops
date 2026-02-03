package at.xoola.heartdrops

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import java.util.function.Consumer
import java.util.stream.Collectors

class DropInfo(configList: String) {
    var entityType: EntityType? = null
        private set
    var dropChance: Float = 0f
        private set
    var minDropAmount: Int = 0
        private set
    var maxDropAmount: Int = 0
        private set
    private val heartLifetime: Int = 30 // seconds

    init {
        val data = configList.split(":")
        if (data.size == 4) {
            try {
                entityType = EntityType.valueOf(data[0].trim().uppercase())
                dropChance = data[1].trim().toFloat()
                minDropAmount = data[2].trim().toInt()
                maxDropAmount = data[3].trim().toInt()
            } catch (e: IllegalArgumentException) {
                HeartDrops.instance?.logger?.severe { "\u001B[31mFailed to parse drop info from config: $configList\u001B[0m" }
                entityType = null
            }
        } else {
            HeartDrops.instance?.logger?.severe { "\u001B[31mInvalid format in config list: $configList\u001B[0m" }
            HeartDrops.instance?.logger?.severe("\u001B[33mExpected format: <EntityType> : <DropChance> : <MinAmount> : <MaxAmount>\u001B[0m")
            entityType = null
        }
    }

    fun onMobDeath(entity: LivingEntity) {
        val killer = entity.killer
        
        if (entityType == null) return

        if (killer != null) {
            val currentHealth = killer.health
            val maxHealth = killer.getAttribute(Attribute.MAX_HEALTH)?.value ?: return
            if (currentHealth >= maxHealth) {
                return
            }
        }

        var dropAmount = minDropAmount
        for (i in minDropAmount until maxDropAmount) {
            if (HeartDrops.instance?.rand?.nextFloat()!! <= dropChance) {
                dropAmount++
            }
        }

        if (dropAmount <= 0) return

        val deathLoc = entity.location
        val displayManager = HeartDrops.instance?.heartDisplayManager
        
        // Get nearby players who can see the hearts
        val nearbyPlayers = deathLoc.world?.getNearbyPlayers(deathLoc, 64.0)
            ?.stream()
            ?.filter { it.hasPermission("heartdrops.pickup") }
            ?.collect(Collectors.toList()) ?: emptyList()

        // Spawn hearts
        repeat(dropAmount) {
            val heartLoc = deathLoc.clone().add(
                (HeartDrops.instance?.rand?.nextDouble()!! - 0.5) * 0.8,
                0.5 + HeartDrops.instance?.rand?.nextDouble()!! * 0.3,
                (HeartDrops.instance?.rand?.nextDouble()!! - 0.5) * 0.8
            )

            // Find ground level - move heart down until solid block or ground is found
            val groundLoc = findGroundLocation(heartLoc)
            
            // Try to use ProtocolLib display first
            var entityId = -1
            if (displayManager?.isProtocolLibAvailable == true && nearbyPlayers.isNotEmpty()) {
                entityId = displayManager.spawnHeart(groundLoc, nearbyPlayers)
            }

            // Add to drop task for particle effects and pickup detection
            HeartDrops.instance?.dropTask?.addDrop(entityId, groundLoc)

            // Schedule automatic removal after lifetime
            val finalEntityId = entityId
            val scheduleLoc = groundLoc.clone()
            HeartDrops.instance?.server?.regionScheduler?.runDelayed(
                HeartDrops.instance!!,
                scheduleLoc,
                Consumer<ScheduledTask> { _ ->
                    if (displayManager != null && finalEntityId != -1) {
                        displayManager.removeHeart(finalEntityId)
                    }
                },
                20L * heartLifetime
            )
        }

        // Spawn initial particle burst
        deathLoc.world?.spawnParticle(
            Particle.HEART,
            deathLoc.add(0.0, 1.0, 0.0),
            dropAmount * 2,
            0.5, 0.5, 0.5,
            0.1
        )
    }

    /**
     * Finds the ground location below the given location.
     * Moves down until a solid block is found or world min height is reached.
     */
    private fun findGroundLocation(startLoc: Location): Location {
        val groundLoc = startLoc.clone()
        val minHeight = startLoc.world?.minHeight ?: return groundLoc
        
        // Move down until we find a solid block or reach minimum height
        while (groundLoc.y > minHeight) {
            val checkLoc = groundLoc.clone().subtract(0.0, 1.0, 0.0)
            
            // Check if there's a solid block below
            if (checkLoc.block.type.isSolid) {
                // Position heart slightly above the solid block
                groundLoc.y = checkLoc.y + 1.2
                return groundLoc
            }
            
            // Move down
            groundLoc.subtract(0.0, 1.0, 0.0)
            
            // Safety check - if we've gone too far down, stop
            if (startLoc.y - groundLoc.y > 100) {
                groundLoc.y = startLoc.y
                return groundLoc
            }
        }
        
        // If we reached minimum height, place it there
        groundLoc.y = minHeight + 1.0
        return groundLoc
    }
}
