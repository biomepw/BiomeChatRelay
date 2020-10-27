package pw.biome.biomechatrelay.minecraft;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Description;
import co.aikar.commands.annotation.Subcommand;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import pw.biome.biomechatrelay.discord.DiscordThread;

@CommandAlias("biomechatrelay|bcr")
@Description("Biome discord server relay commands")
public class CommandHandler extends BaseCommand {

    @Subcommand("stop")
    @CommandPermission("biomechatrelay.admin")
    public void onStop(CommandSender sender, DiscordThread discordThread) {
        if (discordThread.isAlive()) {
            discordThread.interrupt();
            sender.sendMessage(ChatColor.RED + "Stopped DiscordThread...");
        }
    }

    @Subcommand("start")
    @CommandPermission("biomechatrelay.admin")
    public void onStart(CommandSender sender, DiscordThread discordThread) {
        if (!discordThread.isAlive()) {
            discordThread.start();
            sender.sendMessage(ChatColor.GREEN + "Starting DiscordThread...");
        }
    }

    @Subcommand("debug")
    @CommandPermission("biomechatrelay.admin")
    public void onDebug(CommandSender sender, DiscordThread discordThread) {
        discordThread.setDebugMode(!discordThread.isDebugMode());
        sender.sendMessage(ChatColor.GREEN + "Debug mode: " + discordThread.isDebugMode());
    }
}