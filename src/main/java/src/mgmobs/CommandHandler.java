package src.mgmobs;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class CommandHandler implements CommandExecutor {

    private final MGMobs plugin;

    public CommandHandler(MGMobs plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("mgmobs")) {
            if (args.length == 0) {
                sendHelpMessage(sender);
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (sender.hasPermission("mgmobs.reload")) {
                    plugin.reloadConfig();
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("reloadMessage")));
                } else {
                    sender.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("noPermMessage")));
                }
            } else {
                sendHelpMessage(sender);
            }
        }
        return true;
    }

    private void sendHelpMessage(CommandSender sender) {
        sender.sendMessage("§9§n                                                                                          ");
        sender.sendMessage("§l");
        sender.sendMessage("§2/mgmobs help §f- §aTo Get command help");
        sender.sendMessage("§2/mgmobs reload §f- §aReloads the plugin");
        sender.sendMessage("§c§lPlugin By: §f- §a§lCoquettishpigs");
        sender.sendMessage("§9§n                                                                                          ");
    }
}