package at.xoola.heartdrops

import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand

class HeartDropsCommand(private val plugin: HeartDrops) : BukkitCommand("heartdrops") {
    
    init {
        description = "HeartDrops commands"
        permission = "heartdrops.command.reload"
    }

    override fun execute(sender: CommandSender, label: String, args: Array<String>): Boolean {
        if (args.isNotEmpty() && args[0].equals("reload", ignoreCase = true)) {
            plugin.loadConfig()
            sender.sendMessage("§aHeartDrops config reloaded!")
            return true
        }
        sender.sendMessage("§eHeartDrops command usage: /heartdrops reload")
        return true
    }
}
