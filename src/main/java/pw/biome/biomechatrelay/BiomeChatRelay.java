package pw.biome.biomechatrelay;

import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import pw.biome.biomechatrelay.discord.DiscordThread;
import pw.biome.biomechatrelay.minecraft.MinecraftEventListener;

import java.util.logging.Logger;

public final class BiomeChatRelay extends JavaPlugin {

    @Getter
    private static BiomeChatRelay instance;

    private static Logger LOGGER;

    @Getter
    private DiscordThread discordThread;

    @Override
    public void onEnable() {
        instance = this;
        LOGGER = getLogger();
        saveDefaultConfig();

        getServer().getPluginManager().registerEvents(new MinecraftEventListener(), instance);

        initialiseDiscordThread();
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    private void initialiseDiscordThread() {
        discordThread = new DiscordThread(getConfig().getString("discord-token"),
                getConfig().getString("server-chat-channel-id"));

        discordThread.setName("BiomeChatRelay - DiscordThread");

        discordThread.start();

        info("Running DiscordThread now!");
    }
}
