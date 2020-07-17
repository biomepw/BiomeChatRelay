package pw.biome.biomechatrelay.discord;

import lombok.Getter;

public class DiscordThread extends Thread {

    @Getter
    private DiscordManager discordManager;

    private final String token;
    private final String serverChatId;

    public DiscordThread(String token, String serverChatId) {
        this.token = token;
        this.serverChatId = serverChatId;
    }

    @Override
    public void run() {
        discordManager = new DiscordManager(token, serverChatId);
        discordManager.login();
    }
}
