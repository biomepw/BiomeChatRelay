package pw.biome.biomechatrelay.discord;

import com.google.common.collect.ImmutableList;
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
        DiscordThread discordThread = BiomeChatRelay.getInstance().getDiscordThread();
        Snowflake serverChatSnowflake = discordThread.getServerChatSnowflake();
        boolean debug = discordThread.isDebugMode();

        messageCreateEvent.getMessage().getChannel().subscribe(messageChannel -> {
            if (debug) BiomeChatRelay.info("Debug: messagecreate event");
            if (messageChannel.getId().equals(serverChatSnowflake)) {
                if (debug) BiomeChatRelay.info("Debug: messagechannel equals serverchatsnowflake");
                String message = messageCreateEvent.getMessage().getContent();

                if (message.equalsIgnoreCase("list") || message.equalsIgnoreCase("playerlist")) {
                    messageChannel.createMessage(buildList()).subscribe();
                } else if (message.equalsIgnoreCase("tps")) {
                    messageChannel.createMessage("> **TPS: (1m, 5m, 15m) " + buildTpsString() + "**").subscribe();
                } else {
                    if (debug) BiomeChatRelay.info("Debug: else on message check");
                    messageCreateEvent.getMember().ifPresent(member -> {
                        if (debug) BiomeChatRelay.info("Debug: member is present");
                        String displayName = member.getDisplayName();

                        // Don't display any bot relay
                        if (member.isBot()) return;

                        member.getHighestRole().subscribe(role -> {
                            if (debug) BiomeChatRelay.info("Debug: get highest role!");
                            ChatColor chatColor = ChatUtility.getColourFromRankName(role.getName());
                            String formattedMessage = ChatColor.GOLD + "»" + ChatColor.AQUA +
                                    " Discord: " + chatColor + displayName + ChatColor.WHITE + " » "
                                    + BiomeChat.colourise(message);

                            // Pass to minecraft
                            ChatUtility.sendToMinecraft(formattedMessage);
                            if (debug) BiomeChatRelay.info("Debug: posted to mc");
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
        ImmutableList<Player> players = ImmutableList.copyOf(Bukkit.getOnlinePlayers());
        for (int i = 0; i < players.size(); i++) {
            Player value = players.get(i);
            if (ChatUtility.isAFK(value)) {
                builder.append("~~");
                builder.append(value.getDisplayName());
                builder.append("~~");
            } else {
                builder.append(value.getDisplayName());
            }
            if (i != players.size() - 1) builder.append(", ");
        }
        builder.append("**");
        return builder.toString();
    }

    private static String buildTpsString() {
        double[] tps = Bukkit.getTPS();
        int[] arr = new int[tps.length];
        for (int i = 0; i < tps.length; i++) {
            arr[i] = (int) tps[i];
        }
        return Arrays.toString(arr);
    }
}
