<p align="center">
   <img width="256px" height="256px" alt="Chunky Offline Plugin Logo" src="https://www.spigotmc.org/attachments/hearts-png.106172/" />
</p>

<p align="center">
   <a alt="Supports Minecraft 1.21.x" title="Go to Minecraft's Server download site" href="https://www.minecraft.net/en-us/download/server" > <img alt="Supported Minecraft Version" src="https://img.shields.io/badge/Minecraft-1.21.x-69986a" /></a>
    <img alt="GitHub Release (latest)" src="https://img.shields.io/github/v/release/X00LA/HeartDrops">
    <img alt="GitHub Downloads (all assets, all releases)" src="https://img.shields.io/github/downloads/X00LA/HeartDrops/total" />
    <img alt="GitHub Downloads (latest)" src="https://img.shields.io/github/downloads/X00LA/HeartDrops/latest/total">
    <a alt="SpigotMC" title="Go to SpigotMC's download site" href="https://hub.spigotmc.org/jenkins/" >
        <img alt="Supports SpigotMC" src="https://img.shields.io/badge/Supports-SpigotMC-gold?style=flat-square" /></a>
    <a alt="Supports PaperMC" title="Go to PaperMC's download site" href="https://papermc.io/downloads/paper" >
        <img alt="Supports PaperMC" src="https://img.shields.io/badge/Supports-PaperMC-blue" /></a>
    <a alt="Supports Folia" title="Go to Folia's download site" href="https://papermc.io/downloads/folia">
        <img alt="Supports Folia" src="https://img.shields.io/badge/Supports-Folia-green" /></a>
    <a alt="Supports PurPur" title="Go to Purpur's download site" href="https://purpurmc.org/download/purpur">
        <img alt="Supports Purpur" src="https://img.shields.io/badge/Supports-PurPur-a947ff" /></a>
</p>

<p align="center">
    <a alt="Ko-Fi Donation Link" title="Support me on Ko-Fi" href="https://ko-fi.com/Y8Y1RKLT1">
       <img alt="Ko-Fi Donation Banner" src="https://ko-fi.com/img/githubbutton_sm.svg" /></a>
</p>

## Credits

With the kind permission of [Shadow3097](https://www.spigotmc.org/resources/authors/shadow3097.71715/) I'm continuing the development of HeartDrops. Big thank you for this! 🥰

Here's the link to the old resource: [HeartDrops-1.4](https://www.spigotmc.org/resources/heartdrops.9706/)


# Heart Drops

Killing mobs can now heal you!
Define all kinds of mobs to drop heart containers on death.
You can easily configure what mobs will drop containers and which doesn't.
You can also define how much a container will heal you, what drop chance it has and what chance you have to get a double healing.
Use custom sounds for the pickup sound and more...

## Dependencies

- Optional - [ProtocolLib](https://www.spigotmc.org/resources/protocollib.1997/)
If you don't use ProtcolLib the used Effect is slightly different.

## Wiki

Detailed informations on how to configure the plugin, find the right sound and more, visit the [wiki](https://github.com/X00LA/HeartDrops/wiki).

## Features

- Mobs drop hearts with a chance you can define in the config.
- The dropped hearts heal a specific amount of health that you can define in the config.
- The dropped hearts have a chance to heal double the health that you can define in the config.
- You can choose a custom pickup sound that you can define in the config.
- You can define the delay that the hearts spawn in the config.
- You can change the volume and pitch of the pickup sound in the config to make it unique.
- Works with all entities in minecraft. Not tested with custom entities but should be compatible. Simply put the custom mobs in the list like the others and you are good to go.
- You can define in the config the dropchance, minimum drop amount and maximum drop amount of the dropped hearts for each mob separately.
- Plugin was build for maximum compatibility in mind. Supports Bukkit, Spigot, Paper, Folia and Purpur out of the box.

## The Config

```
# The material of the item(it will not be visible on the client)
# You can change this, but this has no effect to the game or to the player.
# For usable materials look here: https://github.com/X00LA/HeartDrops/wiki/Config
material: "REDSTONE"
# The chance for a double heal.
# You can increase or decrease the chance for your players to "win" a double heal.
doubleHealChance: 0.2
# The delay between the spawn of a heart particle.
# You can decrease the delay so the heart spawn faster or increase it to let the players wait for their energy.
particleTime: 15
# The sound of an item pickup.
# You can use different sound.
# For a list of available sounds look here: https://github.com/X00LA/HeartDrops/wiki/Config
pickupsound: "minecraft:block_amethyst_block_chime"
# The volume of the pickup sound.
# Increase or decrease the volume as your want.
pickupvolume: 0.5
# The pitch of the pickup sound.
# Increase or decrease the pitch to make the sound a bit... courious.
pickuppitch: 0
# The amount to heal the player.
# 1 means that 1 heart heals half a player heart. So 2 heals a full player heart.
healAmount: 1
###########################################################################################################
# Here you can play with the variables to increase or decrease the amount of given hearts.                #
# Should be compatible with modded mobs.                                                                  #
# Pattern: entitytype : dropchance : minimum drop amount : maximumdropamount                              #
# Example:    ZOMBIE  :     0.2    :         0           :         1                                      #
###########################################################################################################
entities:
  - "BAT : 0.2 : 0 : 1"
  - "BLAZE : 0.2 : 0 : 1"
  - "BOGGED : 0.2 : 0 : 1"
  - "BREEZE : 0.2 : 0 : 1"
  - "CAMEL_HUSK : 0.2 : 0 : 1"
  - "CAVE_SPIDER : 0.2 : 0 : 1"
  - "CREAKING : 0.2 : 0 : 1"
  - "CREEPER : 0.2 : 0 : 1"
  - "DROWNED : 0.2 : 0 : 1"
  - "ELDER_GUARDIAN : 0.2 : 0 : 1"
  - "ENDER_DRAGON : 0.5 : 2 : 5"
  - "ENDERMAN : 0.2 : 0 : 1"
  - "ENDERMITE : 0.2 : 0 : 1"
  - "EVOKER : 0.2 : 0 : 1"
  - "GHAST : 0.2 : 0 : 1"
  - "GIANT : 0.2 : 1 : 3"
  - "GUARDIAN : 0.2 : 0 : 1"
  - "HOGLIN : 0.2 : 0 : 1"
  - "HUSK : 0.2 : 0 : 1"
  - "ILLUSIONER : 0.2 : 0 : 1"
  - "MAGMA_CUBE : 0.2 : 0 : 1"
  - "PARCHED : 0.2 : 0 : 1"
  - "PHANTOM : 0.2 : 0 : 1"
  - "PIGLIN : 0.2 : 0 : 1"
  - "PIGLIN_BRUTE : 0.2 : 0 : 1"
  - "PILLAGER : 0.2 : 0 : 1"
  - "RAVAGER : 0.2 : 0 : 1"
  - "SHULKER : 0.2 : 0 : 1"
  - "SILVERFISH : 0.2 : 0 : 1"
  - "SKELETON : 0.2 : 0 : 1"
  - "SKELETON_HORSE : 0.2 : 0 : 1"
  - "SLIME : 0.2 : 0 : 1"
  - "SNOW_GOLEM : 0.2 : 0 : 1"
  - "SPIDER : 0.2 : 0 : 1"
  - "STRAY : 0.2 : 0 : 1"
  - "VEX : 0.2 : 0 : 1"
  - "VINDICATOR : 0.2 : 0 : 1"
  - "WARDEN : 0.5 : 2 : 5"
  - "WITCH : 0.2 : 0 : 1"
  - "WITHER : 0.2 : 0 : 1"
  - "WITHER_SKELETON : 0.2 : 0 : 1"
  - "ZOGLIN : 0.2 : 0 : 1"
  - "ZOMBIE : 0.2 : 0 : 1"
  - "ZOMBIE_HORSE : 0.2 : 0 : 1"
  - "ZOMBIE_NAUTILUS : 0.2 : 0 : 1"
  - "ZOMBIE_VILLAGER : 0.2 : 0 : 1"
  - "ZOMBIFIED_PIGLIN : 0.2 : 0 : 1"
  #########################################################################
  # Passive Mobs                                                          #   
  # Uncomment the following entries if you want hearts when killing them. #
  #########################################################################
  #  - "ALLAY : 0.2 : 0 : 1"
  #  - "ARMADILLO : 0.2 : 0 : 1"
  #  - "AXOLOTL : 0.2 : 0 : 1"
  #  - "BEE : 0.2 : 0 : 1"
  #  - "CAMEL : 0.2 : 0 : 1"
  #  - "CAT : 0.2 : 0 : 1"
  #  - "CHICKEN : 0.2 : 0 : 1"
  #  - "COD : 0.2 : 0 : 1"
  #  - "COW : 0.2 : 0 : 1"
  #  - "DOLPHIN : 0.2 : 0 : 1"
  #  - "DONKEY : 0.2 : 0 : 1"
  #  - "FOX : 0.2 : 0 : 1"
  #  - "FROG : 0.2 : 0 : 1"
  #  - "GLOW_SQUID : 0.2 : 0 : 1"
  #  - "GOAT : 0.2 : 0 : 1"
  #  - "HAPPY_GHAST : 0.2 : 0 : 1"
  #  - "HORSE : 0.2 : 0 : 1"
  #  - "IRON_GOLEM : 0.2 : 0 : 1"
  #  - "LLAMA : 0.2 : 0 : 1"
  #  - "MOOSHROOM : 0.2 : 0 : 1"
  #  - "MULE : 0.2 : 0 : 1"
  #  - "NAUTILUS : 0.2 : 0 : 1"
  #  - "OCELOT : 0.2 : 0 : 1"
  #  - "PANDA : 0.2 : 0 : 1"
  #  - "PARROT : 0.2 : 0 : 1"
  #  - "PIG : 0.2 : 0 : 1"
  #  - "PLAYER : 0.2 : 0 : 1"
  #  - "POLAR_BEAR : 0.2 : 0 : 1"
  #  - "PUFFERFISH : 0.2 : 0 : 1"
  #  - "RABBIT : 0.2 : 0 : 1"
  #  - "SALMON : 0.2 : 0 : 1"
  #  - "SHEEP : 0.2 : 0 : 1"
  #  - "SNIFFER : 0.2 : 0 : 1"
  #  - "SQUID : 0.2 : 0 : 1"
  #  - "STRIDER : 0.2 : 0 : 1"
  #  - "TADPOLE : 0.2 : 0 : 1"
  #  - "TRADER_LLAMA : 0.2 : 0 : 1"
  #  - "TROPICAL_FISH : 0.2 : 0 : 1"
  #  - "TURTLE : 0.2 : 0 : 1"
  #  - "VILLAGER : 0.2 : 0 : 1"
  #  - "WANDERING_TRADER : 0.2 : 0 : 1"
  #  - "WOLF : 0.2 : 0 : 1"
```

## Commands & Permissions

```
| Permission | Command | Description | Default |
| ------ | ------ | ------ | ------ |
| heartdrops.admin | --- | Admin permission for heartdrops. | OP |
| heartdrops.command.help | /heartsdrops help | Players with this permission can use the help command for heartdrops. | OP |
| heartdrops.pickup | --- | Players with this permission are allowed to pickup hearts. | Default |
```
