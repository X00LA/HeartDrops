package at.xoola.heartdrops

import org.bukkit.Location
import org.bukkit.Particle
import java.util.concurrent.ConcurrentHashMap

internal class DropTask : Runnable {
    private val drops = ConcurrentHashMap<Int, Location>()

    fun addDrop(entityId: Int, loc: Location?) {
        if (loc != null && loc.world != null) {
            drops[entityId] = loc.clone()
        }
    }

    fun clearDrops() {
        val manager = HeartDrops.instance?.heartDisplayManager
        manager?.clearAll()
        drops.clear()
    }

    override fun run() {
        val manager = HeartDrops.instance?.heartDisplayManager
        val iterator = drops.entries.iterator()
        
        while (iterator.hasNext()) {
            val (entityId, loc) = iterator.next()
            
            if (loc.world == null) {
                manager?.removeHeart(entityId)
                iterator.remove()
                continue
            }

            // Spawn particles around the heart
            loc.world?.spawnParticle(Particle.HEART, loc, 3, 0.4, 0.4, 0.4, 0.0)

            // Check for nearby players
            for (player in loc.world?.players ?: emptyList()) {
                if (!player.hasPermission("heartdrops.pickup")) continue
                
                if (player.location.distanceSquared(loc) < 2.25) {
                    HeartDrops.instance?.healPlayer(player)
                    manager?.removeHeart(entityId)
                    iterator.remove()
                    break
                }
            }
        }
    }
}
