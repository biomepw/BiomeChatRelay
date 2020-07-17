package pw.biome.biomechatrelay.common;

import discord4j.rest.entity.RestChannel;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import pw.biome.biomechat.obj.Rank;
import pw.biome.biomechatrelay.BiomeChatRelay;
import pw.biome.biomechatrelay.discord.DiscordManager;

public final class ChatUtility {

    private static RestChannel serverChatChannel;

    public static void sendToDiscord(String message) {
        DiscordManager discordManager = BiomeChatRelay.getInstance().getDiscordManager();

        // Lazy load server chat channel, and then cache
        if (serverChatChannel == null) {
            serverChatChannel = discordManager.getClient().getChannelById(discordManager.getServerChatSnowflake());
        }

        serverChatChannel.createMessage(message).subscribe();

        // Log all messages to console
        BiomeChatRelay.info(message);
    }

    public static void sendToMinecraft(String message) {
        Bukkit.broadcastMessage(message);

        // Log all messages to console
        BiomeChatRelay.info(message);
    }

    public static ChatColor getColourFromRankName(String rankName) {
        Rank rank = Rank.getRankFromName(rankName);

        if (rank != null) {
            return rank.getPrefix();
        }

        // default as player colour
        return ChatColor.GREEN;
    }
}
