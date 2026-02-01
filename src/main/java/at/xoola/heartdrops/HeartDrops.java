package at.xoola.heartdrops;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.Sound;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;

public class HeartDrops extends JavaPlugin implements Listener {
    public static HeartDrops instance;
    public static NamespacedKey HEART_KEY;
    public Random rand = new Random();

    Sound pickUpSound;
    private float pickUpVolume;
    private float pickUpPitch;
    private int healAmount;
    private float doubleHealChance;

    private HashMap<EntityType, DropInfo> entityInfo;
    public DropTask dropTask;
    private ScheduledTask scheduledTask;
    private HeartDisplayManager heartDisplayManager;

    @Override
    public void onEnable() {
        instance = this;
        HEART_KEY = new NamespacedKey(this, "heart-drop");

        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);

        this.heartDisplayManager = new HeartDisplayManager();

        loadConfig();

        getServer().getCommandMap().register("heartdrops", new HeartDropsCommand(this));

        getLogger().info("HeartDrops enabled!");
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        Player killer = entity.getKiller();

        if (killer != null && this.entityInfo.containsKey(entity.getType())) {
            dropHearts(entity, killer);
        }
    }

    @Override
    public void onDisable() {
        if (this.scheduledTask != null && !this.scheduledTask.isCancelled()) {
            this.scheduledTask.cancel();
        }
        if (this.heartDisplayManager != null) {
            this.heartDisplayManager.clearAll();
        }
    }

    public void loadConfig() {
        if (this.scheduledTask != null && !this.scheduledTask.isCancelled()) {
            this.scheduledTask.cancel();
        }

        reloadConfig();

        String soundKeyString = getConfig().getString("pickupsound", "minecraft:entity.player.levelup").trim();
        Sound sound = null;

        try {
            // Convert input to enum name format
            String enumName = soundKeyString
                .replace("minecraft:", "")
                .replace(".", "_")
                .replace(":", "_")
                .toUpperCase(Locale.ROOT);
            
            sound = Sound.valueOf(enumName);
            getLogger().info("Loaded sound: " + soundKeyString + " (" + enumName + ")");
        } catch (IllegalArgumentException e) {
            getLogger().warning("Invalid sound: '" + soundKeyString + "'. Using default ENTITY_PLAYER_LEVELUP");
            sound = Sound.ENTITY_PLAYER_LEVELUP;
        }

        this.pickUpSound = sound;


        this.pickUpVolume = (float) getConfig().getDouble("pickupvolume", 1.0);
        this.pickUpPitch = (float) getConfig().getDouble("pickuppitch", 1.0);

        this.healAmount = getConfig().getInt("healAmount", 2);
        this.doubleHealChance = (float) getConfig().getDouble("doubleHealChance", 0.1);

        if (this.entityInfo == null) {
            this.entityInfo = new HashMap<>();
        } else {
            this.entityInfo.clear();
        }

        List<String> entityList = getConfig().getStringList("entities");
        //for (String value : entityList) {
        //    DropInfo dropInfo = new DropInfo(value);
        //    if (dropInfo.getEntityType() != null) {
        //        this.entityInfo.put(dropInfo.getEntityType(), dropInfo);
        //    }
        //}
        for (String value : entityList) {
            DropInfo dropInfo = new DropInfo(value);
            if (dropInfo.getEntityType() != null) {
                this.entityInfo.put(dropInfo.getEntityType(), dropInfo);
                getLogger().info("Loaded drop info for entity: " + dropInfo.getEntityType());
            } else {
                getLogger().warning("Failed to load DropInfo for config entry: " + value);
            }
        }


        if (this.dropTask == null) {
            this.dropTask = new DropTask();
        }
        this.dropTask.clearDrops();

        int particleTime = getConfig().getInt("particleTime", 20);
        long delay = particleTime;
        long period = particleTime;
        this.scheduledTask = getServer().getGlobalRegionScheduler().runAtFixedRate(this, task -> this.dropTask.run(), delay, period);

        getLogger().info("Config loaded! Pickup sound: " + (this.pickUpSound != null ? this.pickUpSound.toString() : "none"));
    }

    public void dropHearts(LivingEntity entity, Player player) {
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
        if (player.getHealth() >= maxHealth) return;

        this.entityInfo.get(entity.getType()).onMobDeath(entity);
    }

    public void healPlayer(Player player) {
        double maxHealth = Objects.requireNonNull(player.getAttribute(Attribute.MAX_HEALTH)).getValue();
        if (player.getHealth() < maxHealth) {
            int amountToHeal = this.healAmount;
            if (new Random().nextFloat() <= this.doubleHealChance) {
                amountToHeal *= 2;
            }
            player.setHealth(Math.min(maxHealth, player.getHealth() + amountToHeal));
            player.playSound(player.getLocation(), this.pickUpSound, this.pickUpVolume, this.pickUpPitch);
        }
    }

    public HeartDisplayManager getHeartDisplayManager() {
        return heartDisplayManager;
    }
}
