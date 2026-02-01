package at.xoola.heartdrops;

import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.jetbrains.annotations.NotNull;

public class HeartDropsCommand extends BukkitCommand {
    private final HeartDrops plugin;

    public HeartDropsCommand(HeartDrops plugin) {
        super("heartdrops");
        this.plugin = plugin;
        this.setDescription("HeartDrops commands");
        this.setPermission("heartdrops.admin");
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.loadConfig();
            sender.sendMessage("HeartDrops config reloaded!");
            return true;
        }
        sender.sendMessage("HeartDrops command usage: /heartdrops reload");
        return true;
    }
}
