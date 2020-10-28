package pw.biome.biomechatrelay.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Getter;
import lombok.Setter;
import pw.biome.biomechatrelay.BiomeChatRelay;

public class DiscordThread extends Thread {

    @Getter
    private final DiscordClient client;

    @Getter
    private final Snowflake serverChatSnowflake;

    @Getter
    @Setter
    private boolean debugMode;

    @Getter
    private final DiscordGroupSyncHandler discordGroupSyncHandler;

    public DiscordThread(String token, String serverChatId) {
        this.serverChatSnowflake = Snowflake.of(serverChatId);
        client = DiscordClient.create(token);
        discordGroupSyncHandler = new DiscordGroupSyncHandler(client);
        discordGroupSyncHandler.loadPermissionSet();
    }

    @Override
    public void run() {
        client.login().subscribe(gateway -> {
            gateway.on(MessageCreateEvent.class).subscribe(DiscordChatHandler::handleChatEvent);
            gateway.on(DisconnectEvent.class).subscribe(event ->
                    BiomeChatRelay.info("DiscordManager: Disconnected due to " + event.getCause()));
        });
    }
}
