package pw.biome.biomechatrelay.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import pw.biome.biomechat.BiomeChat;
import pw.biome.biomechatrelay.BiomeChatRelay;
import pw.biome.biomechatrelay.util.ChatUtility;

import java.util.Arrays;

public final class DiscordChatHandler {

    /**
     * Method to handle discord MessageCreateEvent
     *
     * @param messageCreateEvent - Discord4j MessageCreateEvent
     */
    public static void handleChatEvent(MessageCreateEvent messageCreateEvent) {
        Snowflake serverChatSnowflake = BiomeChatRelay.getInstance().getDiscordThread().getServerChatSnowflake();
        messageCreateEvent.getMessage().getChannel().subscribe(messageChannel -> {
            if (messageChannel.getId().equals(serverChatSnowflake)) {
                String message = messageCreateEvent.getMessage().getContent();

                if (message.equalsIgnoreCase("list") || message.equalsIgnoreCase("playerlist")) {
                    messageChannel.createMessage(buildList()).subscribe();
                } else if (message.equalsIgnoreCase("tps")) {
                    messageChannel.createMessage("> **TPS: (1m, 5m, 15m) " + Arrays.toString(Bukkit.getTPS()) + "**");
                } else {
                    messageCreateEvent.getMember().ifPresent(member -> {
                        String displayName = member.getDisplayName();

                        // Don't display any bot relay
                        if (member.isBot()) return;

                        member.getHighestRole().subscribe(role -> {
                            ChatColor chatColor = ChatUtility.getColourFromRankName(role.getName());
                            String formattedMessage = ChatColor.GOLD + "»" + ChatColor.AQUA +
                                    " Discord: " + chatColor + displayName + ChatColor.WHITE + " » "
                                    + BiomeChat.colourise(message);

                            // Pass to minecraft
                            ChatUtility.sendToMinecraft(formattedMessage);
                        });
                    });
                }
            }
        });
    }

    /**
     * Private utility method to build player list
     *
     * @return player list in strong format
     */
    private static String buildList() {
        StringBuilder builder = new StringBuilder("» **List: ");

        for (Player player : Bukkit.getOnlinePlayers()) {
            if (ChatUtility.isAFK(player)) {
                builder.append("*");
                builder.append(player.getDisplayName());
                builder.append("*");
            } else {
                builder.append(player.getDisplayName());
            }
            builder.append(", ");
        }

        builder.substring(0, builder.length() - 2);
        builder.append("**");

        return builder.toString();
    }
}
