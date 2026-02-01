package at.xoola.heartdrops;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

class DropTask implements Runnable {
    private final Map<Integer, Location> drops = new ConcurrentHashMap<>();

    public void addDrop(int entityId, Location loc) {
        if (loc != null && loc.getWorld() != null) {
            drops.put(entityId, loc.clone());
        }
    }

    public void clearDrops() {
        HeartDisplayManager manager = HeartDrops.instance.getHeartDisplayManager();
        if (manager != null) {
            manager.clearAll();
        }
        drops.clear();
    }

    @Override
    public void run() {
        HeartDisplayManager manager = HeartDrops.instance.getHeartDisplayManager();
        Iterator<Map.Entry<Integer, Location>> it = drops.entrySet().iterator();
        
        while (it.hasNext()) {
            Map.Entry<Integer, Location> entry = it.next();
            int entityId = entry.getKey();
            Location loc = entry.getValue();
            
            if (loc.getWorld() == null) {
                if (manager != null) {
                    manager.removeHeart(entityId);
                }
                it.remove();
                continue;
            }

            // Spawn particles around the heart
            loc.getWorld().spawnParticle(Particle.HEART, loc, 3, 0.4, 0.4, 0.4, 0.0);

            // Check for nearby players
            for (Player p : loc.getWorld().getPlayers()) {
                if (!p.hasPermission("heartdrops.pickup")) continue;
                
                if (p.getLocation().distanceSquared(loc) < 2.25) {
                    HeartDrops.instance.healPlayer(p);
                    
                    if (manager != null) {
                        manager.removeHeart(entityId);
                    }
                    it.remove();
                    break;
                }
            }
        }
    }
}
