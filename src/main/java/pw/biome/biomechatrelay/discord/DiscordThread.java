package pw.biome.biomechatrelay.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.discordjson.json.gateway.StatusUpdate;
import lombok.Getter;
import lombok.Setter;
import pw.biome.biomechatrelay.BiomeChatRelay;

/**
 * Wrapper of Thread to house the D4J framework
 */
public class DiscordThread extends Thread {

    @Getter
    private final DiscordClient client;

    @Getter
    private GatewayDiscordClient gatewayDiscordClient;

    @Getter
    private final Snowflake serverChatSnowflake;

    @Getter
    private final Snowflake adminChannelSnowflake;

    @Getter
    @Setter
    private boolean debugMode;

    @Getter
    private final DiscordGroupSyncHandler discordGroupSyncHandler;

    /**
     * Constructor
     *
     * @param token        for discord
     * @param serverChatId of where to send server chat
     * @param adminChatId  of where to send admin chat
     */
    public DiscordThread(String token, String serverChatId, String adminChatId) {
        this.serverChatSnowflake = Snowflake.of(serverChatId);
        this.adminChannelSnowflake = Snowflake.of(adminChatId);
        client = DiscordClient.create(token);
        discordGroupSyncHandler = new DiscordGroupSyncHandler(client);
        discordGroupSyncHandler.loadPermissionSet();
    }

    /**
     * Run + register events
     */
    @Override
    public void run() {
        client.login().subscribe(gateway -> {
            gatewayDiscordClient = gateway;
            gateway.on(MessageCreateEvent.class).subscribe(DiscordChatHandler::handleChatEvent);
            gateway.on(DisconnectEvent.class).subscribe(event ->
                    BiomeChatRelay.info("DiscordManager: Disconnected due to " + event.getCause()));
        });
    }

    /**
     * Update the status of the Bot
     *
     * @param statusUpdate to update
     */
    public void updatePresence(StatusUpdate statusUpdate) {
        if (gatewayDiscordClient != null) {
            gatewayDiscordClient.updatePresence(statusUpdate).subscribe();
        }
    }
}
