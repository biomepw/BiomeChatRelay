package pw.biome.biomechatrelay.minecraft;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import pw.biome.biomechatrelay.BiomeChatRelay;
import pw.biome.biomechatrelay.discord.DiscordThread;

public class CommandHandler implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        DiscordThread discordThread = BiomeChatRelay.getInstance().getDiscordThread();
        if (sender.hasPermission("biomechatrelay.admin")) {
            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("stop")) {
                    if (discordThread.isAlive()) {
                        discordThread.interrupt();
                        sender.sendMessage(ChatColor.RED + "Stopped DiscordThread...");
                    }
                } else if (args[0].equalsIgnoreCase("start")) {
                    if (!discordThread.isAlive()) {
                        discordThread.start();
                        sender.sendMessage(ChatColor.GREEN + "Starting DiscordThread...");
                    }
                }
            }
        }
        return true;
    }
}