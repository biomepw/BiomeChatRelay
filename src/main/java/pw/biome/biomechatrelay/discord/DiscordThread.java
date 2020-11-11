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

    public DiscordThread(String token, String serverChatId, String adminChatId) {
        this.serverChatSnowflake = Snowflake.of(serverChatId);
        this.adminChannelSnowflake = Snowflake.of(adminChatId);
        client = DiscordClient.create(token);
        discordGroupSyncHandler = new DiscordGroupSyncHandler(client);
        discordGroupSyncHandler.loadPermissionSet();
    }

    @Override
    public void run() {
        client.login().subscribe(gateway -> {
            gatewayDiscordClient = gateway;
            gateway.on(MessageCreateEvent.class).subscribe(DiscordChatHandler::handleChatEvent);
            gateway.on(DisconnectEvent.class).subscribe(event ->
                    BiomeChatRelay.info("DiscordManager: Disconnected due to " + event.getCause()));
        });
    }

    public void updatePresence(StatusUpdate statusUpdate) {
        if (gatewayDiscordClient != null) {
            gatewayDiscordClient.updatePresence(statusUpdate).subscribe();
        }
    }
}
