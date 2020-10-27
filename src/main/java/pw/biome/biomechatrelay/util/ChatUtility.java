package pw.biome.biomechatrelay.util;

import discord4j.rest.entity.RestChannel;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Team;
import pw.biome.biomechat.obj.Corp;
import pw.biome.biomechatrelay.BiomeChatRelay;
import pw.biome.biomechatrelay.discord.DiscordThread;

public final class ChatUtility {

    private static RestChannel serverChatChannel;

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

    public static void sendToMinecraft(String message) {
        Bukkit.getOnlinePlayers().forEach(player -> player.sendMessage(message));

        // Log all messages to console
        BiomeChatRelay.info(message);
    }

    public static ChatColor getColourFromCorpName(String corpName) {
        String sanitised = corpName.toLowerCase().replaceAll(" ", "_");
        Corp.getCorpFromName(sanitised).ifPresent(Corp::getPrefix);
        return ChatColor.GREEN;
    }

    public static boolean isAFK(Player player) {
        if (player == null) return false;
        Team team = player.getScoreboard().getTeam("hc_afk");
        if (team == null) return false;

        return team.hasEntry(player.getName());
    }
}
