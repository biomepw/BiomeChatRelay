package pw.biome.biomechatrelay.util;

import discord4j.rest.entity.RestChannel;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import pw.biome.biomechat.obj.Corp;
import pw.biome.biomechatrelay.BiomeChatRelay;
import pw.biome.biomechatrelay.discord.DiscordThread;

import java.util.Optional;

/**
 * Utility class to send messages to either MC/Discord
 */
public final class ChatUtility {

    // Lazy loaded
    private static RestChannel serverChatChannel;
    private static RestChannel adminChannel;

    /**
     * Sends message to discord server-chat channel
     *
     * @param message to send
     */
    public static void sendToDiscord(String message) {
        DiscordThread discordThread = BiomeChatRelay.getInstance().getDiscordThread();

        // Lazy load server chat channel, and then cache
        if (serverChatChannel == null) {
            serverChatChannel = discordThread.getClient().getChannelById(discordThread.getServerChatSnowflake());
        }

        serverChatChannel.createMessage(message).subscribe();

        // Log all messages to console
        BiomeChatRelay.info(message);
    }

    /**
     * Sends message to discord admin channel
     *
     * @param message to send
     */
    public static void sendToAdminChannel(String message) {
        DiscordThread discordThread = BiomeChatRelay.getInstance().getDiscordThread();

        // Lazy load server chat channel, and then cache
        if (adminChannel == null) {
            adminChannel = discordThread.getClient().getChannelById(discordThread.getAdminChannelSnowflake());
        }

        adminChannel.createMessage(message).subscribe();

        // Log all messages to console
        BiomeChatRelay.info(message);
    }

    /**
     * Sends message to MC
     *
     * @param message to send
     */
    public static void sendToMinecraft(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));

        // Log all messages to console
        BiomeChatRelay.info(message);
    }

    /**
     * Returns ChatColor of the Corp (if found, otherwise green)
     *
     * @param corpName to look for
     * @return ChatColor of the Corp
     */
    public static ChatColor getColourFromCorpName(String corpName) {
        String sanitised = corpName.toLowerCase().replaceAll(" ", "_");

        Optional<Corp> corpOptional = Corp.getCorpFromName(sanitised);
        if (corpOptional.isPresent()) {
            return corpOptional.get().getPrefix();
        }

        return ChatColor.GREEN;
    }

    /**
     * Little helper method to check if a user is AFK
     *
     * @param player to check
     * @return whether or not the player is AFK
     */
    public static boolean isAFK(Player player) {
        if (player == null) return false;
        Team team = player.getScoreboard().getTeam("hc_afk");
        if (team == null) return false;

        return team.hasEntry(player.getName());
    }
}
