package at.xoola.heartdrops

import io.papermc.paper.threadedregions.scheduler.ScheduledTask
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class HeartDrops : JavaPlugin(), Listener {
    val rand = Random()
    
    var pickUpSound: Sound? = null
        private set
    private var pickUpVolume: Float = 0f
    private var pickUpPitch: Float = 0f
    private var healAmount: Int = 0
    private var doubleHealChance: Float = 0f

    private val entityInfo = HashMap<EntityType, DropInfo>()
    internal var dropTask: DropTask? = null
        private set
    private var scheduledTask: ScheduledTask? = null
    var heartDisplayManager: HeartDisplayManager? = null
        private set

    override fun onEnable() {
        instance = this
        HEART_KEY = NamespacedKey(this, "heart-drop")

        displayLogo()

        saveDefaultConfig()
        server.pluginManager.registerEvents(this, this)

        heartDisplayManager = HeartDisplayManager()

        loadConfig()

        server.commandMap.register("heartdrops", HeartDropsCommand(this))

        logger.info("\u001B[32mHeartDrops enabled!\u001B[0m")
    }

	private fun displayLogo() {
        try {
            val logoStream = getResource("ascii-logo.ans")
            if (logoStream != null) {
                logoStream.bufferedReader().useLines { lines ->
                    lines.forEach { println(it) }
                }
            }
            logger.info("\u001B[32mHeartDrops v${pluginMeta.version} by X00LA.\u001B[0m")
        } catch (e: Exception) {
            logger.warning("\u001B[31mCould not load ASCII logo: ${e.message}\u001B[0m")
        }
    }

    private fun updateConfigWithDefaults() {
        val defaultConfig = config.defaults ?: return
        var configChanged = false

        for (key in defaultConfig.getKeys(false)) {
            if (!config.contains(key)) {
                config.set(key, defaultConfig.get(key))
                configChanged = true
                logger.info { "\u001B[33mAdded missing config key: $key\u001B[0m" }
            }
        }

        val currentEntities = config.getStringList("entities").toMutableSet()
        val defaultEntities = defaultConfig.getStringList("entities")
        
        for (defaultEntity in defaultEntities) {
            val entityType = defaultEntity.split(":").firstOrNull()
            if (entityType != null && currentEntities.none { it.startsWith("$entityType:") }) {
                currentEntities.add(defaultEntity)
                configChanged = true
                logger.info { "\u001B[33mAdded missing entity: $entityType\u001B[0m" }
            }
        }

        if (configChanged) {
            config.set("entities", currentEntities.sorted())
            saveConfig()
            logger.info { "\u001B[32mConfig updated with missing entries.\u001B[0m" }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onEntityDeath(event: EntityDeathEvent) {
        val entity = event.entity
        val killer = entity.killer

        if (killer != null && entityInfo.containsKey(entity.type)) {
            dropHearts(entity, killer)
        }
    }

    override fun onDisable() {
        scheduledTask?.let {
            if (!it.isCancelled) {
                it.cancel()
            }
        }
        heartDisplayManager?.clearAll()
    }

    fun loadConfig() {
        scheduledTask?.let {
            if (!it.isCancelled) {
                it.cancel()
            }
        }

        reloadConfig()
        updateConfigWithDefaults()

        var soundKeyString = config.getString("pickupsound", "minecraft:entity.player.levelup")
        soundKeyString = soundKeyString?.trim() ?: "minecraft:entity.player.levelup"

        val sound: Sound = try {
            val soundKey = NamespacedKey.fromString(soundKeyString)
            if (soundKey != null) {
                var registrySound = Registry.SOUND_EVENT.get(soundKey)
                
                if (registrySound == null) {
                    val enumName = soundKeyString.substringAfter("minecraft:", soundKeyString)
                        .uppercase()
                        .replace('.', '_')
                    
                    registrySound = try {
                        Sound::class.java.getField(enumName).get(null) as? Sound
                    } catch (e: Exception) {
                        null
                    }
                }
                
                registrySound ?: Sound.ENTITY_PLAYER_LEVELUP
            } else {
                Sound.ENTITY_PLAYER_LEVELUP
            }
        } catch (e: Exception) {
            logger.warning { "\u001B[31mError loading sound '$soundKeyString': ${e.message}\u001B[0m" }
            Sound.ENTITY_PLAYER_LEVELUP
        }

        pickUpSound = sound

        pickUpVolume = config.getDouble("pickupvolume", 1.0).toFloat()
        pickUpPitch = config.getDouble("pickuppitch", 1.0).toFloat()

        healAmount = config.getInt("healAmount", 2)
        doubleHealChance = config.getDouble("doubleHealChance", 0.1).toFloat()

        entityInfo.clear()

        val entityList = config.getStringList("entities")
        for (value in entityList) {
            val dropInfo = DropInfo(value)
            if (dropInfo.entityType != null) {
                entityInfo[dropInfo.entityType!!] = dropInfo
                logger.info { "\u001B[32mLoaded drop info for entity: ${dropInfo.entityType}\u001B[0m" }
            } else {
                logger.warning { "\u001B[31mFailed to load DropInfo for config entry: $value\u001B[0m" }
            }
        }

        if (dropTask == null) {
            dropTask = DropTask()
        }
        dropTask?.clearDrops()

        val particleTime = config.getInt("particleTime", 20)
        val delay = particleTime.toLong()
        val period = particleTime.toLong()
        scheduledTask = server.globalRegionScheduler.runAtFixedRate(this, { dropTask?.run() }, delay, period)

        logger.info { "\u001B[32mConfig loaded! Pickup sound: ${pickUpSound?.toString() ?: "none"}\u001B[0m" }
    }

    fun dropHearts(entity: LivingEntity, player: Player) {
        val maxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: return
        if (player.health >= maxHealth) return

        entityInfo[entity.type]?.onMobDeath(entity)
    }

    fun healPlayer(player: Player) {
        val maxHealth = player.getAttribute(Attribute.MAX_HEALTH)?.value ?: return
        if (player.health < maxHealth) {
            var amountToHeal = healAmount
            if (Random().nextFloat() <= doubleHealChance) {
                amountToHeal *= 2
            }
            player.health = minOf(maxHealth, player.health + amountToHeal)
            pickUpSound?.let { player.playSound(player.location, it, pickUpVolume, pickUpPitch) }
        }
    }

    companion object {
        @JvmStatic
        var instance: HeartDrops? = null
            private set
        
        @JvmStatic
        lateinit var HEART_KEY: NamespacedKey
            private set
    }
}
