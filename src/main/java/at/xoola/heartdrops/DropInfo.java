package at.xoola.heartdrops;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

public class DropInfo {
    private EntityType entityType;
    private float dropChance;
    private int minDropAmount;
    private int maxDropAmount;
    private int heartLifetime = 30; // seconds

    public DropInfo(String configList) {
        String[] data = configList.split(":");
        if (data.length == 4) {
            try {
                this.entityType = EntityType.valueOf(data[0].trim().toUpperCase());
                this.dropChance = Float.parseFloat(data[1].trim());
                this.minDropAmount = Integer.parseInt(data[2].trim());
                this.maxDropAmount = Integer.parseInt(data[3].trim());
            } catch (IllegalArgumentException e) {
                HeartDrops.instance.getLogger().severe("Failed to parse drop info from config: " + configList);
                this.entityType = null;
            }
        } else {
            HeartDrops.instance.getLogger().severe("Invalid format in config list: " + configList);
            HeartDrops.instance.getLogger().severe("Expected format: <EntityType> : <DropChance> : <MinAmount> : <MaxAmount>");
            this.entityType = null;
        }
    }

    public void onMobDeath(LivingEntity entity) {
        Player killer = entity.getKiller();
        
        if (this.entityType == null) return;

        if (killer != null) {
            double currentHealth = killer.getHealth();
            double maxHealth = Objects.requireNonNull(killer.getAttribute(Attribute.MAX_HEALTH)).getValue();
            if (currentHealth >= maxHealth) {
                return;
            }
        }

        int dropAmount = getMinDropAmount();
        for (int i = getMinDropAmount(); i < getMaxDropAmount(); i++) {
            if (HeartDrops.instance.rand.nextFloat() <= getDropChance()) {
                dropAmount++;
            }
        }

        if (dropAmount <= 0) return;

        Location deathLoc = entity.getLocation();
        HeartDisplayManager displayManager = HeartDrops.instance.getHeartDisplayManager();
        
        // Get nearby players who can see the hearts
        Collection<Player> nearbyPlayers = deathLoc.getWorld().getNearbyPlayers(deathLoc, 64)
            .stream()
            .filter(p -> p.hasPermission("heartdrops.pickup"))
            .collect(Collectors.toList());

        // Spawn hearts
        for (int i = 0; i < dropAmount; i++) {
            Location heartLoc = deathLoc.clone().add(
                (HeartDrops.instance.rand.nextDouble() - 0.5) * 0.8,
                0.5 + HeartDrops.instance.rand.nextDouble() * 0.3,
                (HeartDrops.instance.rand.nextDouble() - 0.5) * 0.8
            );

            // Find ground level - move heart down until solid block or ground is found
            Location groundLoc = findGroundLocation(heartLoc);
            
            // Try to use ProtocolLib display first
            int entityId = -1;
            if (displayManager != null && displayManager.isProtocolLibAvailable() && !nearbyPlayers.isEmpty()) {
                entityId = displayManager.spawnHeart(groundLoc, nearbyPlayers);
            }

            // Add to drop task for particle effects and pickup detection
            if (HeartDrops.instance.dropTask != null) {
                HeartDrops.instance.dropTask.addDrop(entityId, groundLoc);
            }

            // Schedule automatic removal after lifetime
            final int finalEntityId = entityId;
            final Location scheduleLoc = groundLoc.clone();
            HeartDrops.instance.getServer().getRegionScheduler().runDelayed(
                HeartDrops.instance,
                scheduleLoc,
                task -> {
                    if (displayManager != null && finalEntityId != -1) {
                        displayManager.removeHeart(finalEntityId);
                    }
                },
                20L * heartLifetime
            );
        }

        // Spawn initial particle burst
        deathLoc.getWorld().spawnParticle(
                Particle.HEART,
                deathLoc.add(0, 1, 0),
                dropAmount * 2,
                0.5, 0.5, 0.5,
                0.1
        );
    }

    /**
     * Finds the ground location below the given location.
     * Moves down until a solid block is found or world min height is reached.
     */
    private Location findGroundLocation(Location startLoc) {
        Location groundLoc = startLoc.clone();
        int minHeight = startLoc.getWorld().getMinHeight();
        
        // Move down until we find a solid block or reach minimum height
        while (groundLoc.getY() > minHeight) {
            Location checkLoc = groundLoc.clone().subtract(0, 1, 0);
            
            // Check if there's a solid block below
            if (checkLoc.getBlock().getType().isSolid()) {
                // Position heart slightly above the solid block
                groundLoc.setY(checkLoc.getY() + 1.2);
                return groundLoc;
            }
            
            // Move down
            groundLoc.subtract(0, 1, 0);
            
            // Safety check - if we've gone too far down, stop
            if (startLoc.getY() - groundLoc.getY() > 100) {
                groundLoc.setY(startLoc.getY());
                return groundLoc;
            }
        }
        
        // If we reached minimum height, place it there
        groundLoc.setY(minHeight + 1);
        return groundLoc;
    }

    public EntityType getEntityType() {
        return this.entityType;
    }

    public float getDropChance() {
        return this.dropChance;
    }

    public int getMinDropAmount() {
        return this.minDropAmount;
    }

    public int getMaxDropAmount() {
        return this.maxDropAmount;
    }
}
