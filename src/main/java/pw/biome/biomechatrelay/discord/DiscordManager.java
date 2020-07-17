package pw.biome.biomechatrelay.discord;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.event.domain.lifecycle.DisconnectEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Getter;
import pw.biome.biomechatrelay.BiomeChatRelay;

public class DiscordManager {

    @Getter
    private final DiscordClient client;

    @Getter
    private final Snowflake serverChatSnowflake;

    protected DiscordManager(String token, String serverChatId) {
        client = DiscordClient.create(token);
        serverChatSnowflake = Snowflake.of(serverChatId);
    }

    protected void login() {
        client.login().subscribe(gateway -> {
            gateway.on(MessageCreateEvent.class).subscribe(DiscordChatHandler::handleChatEvent);
            gateway.on(DisconnectEvent.class).subscribe(event ->
                    BiomeChatRelay.info("DiscordManager: Disconnected due to " + event.getCause()));
        });
    }
}
