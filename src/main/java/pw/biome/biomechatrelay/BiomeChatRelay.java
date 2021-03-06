package pw.biome.biomechatrelay;

import co.aikar.commands.PaperCommandManager;
import discord4j.core.object.presence.Activity;
import discord4j.core.object.presence.Status;
import discord4j.discordjson.json.gateway.StatusUpdate;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;
import pw.biome.biomechatrelay.discord.DiscordThread;
import pw.biome.biomechatrelay.minecraft.CommandHandler;
import pw.biome.biomechatrelay.minecraft.MinecraftEventListener;
import pw.biome.biomechatrelay.util.ChatUtility;

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

        PaperCommandManager manager = new PaperCommandManager(instance);
        manager.getCommandContexts().registerIssuerOnlyContext(DiscordThread.class, c -> instance.getDiscordThread());
        manager.registerCommand(new CommandHandler());
        getServer().getPluginManager().registerEvents(new MinecraftEventListener(), instance);

        initialiseDiscordThread();
        runPlayerCountTask();
    }

    @Override
    public void onDisable() {
        String stopMessage = "> :exclamation: Server has stopped :exclamation: ";
        ChatUtility.sendToDiscord(stopMessage);
        getServer().getScheduler().cancelTasks(instance);
        discordThread.interrupt();
    }

    public static void info(String msg) {
        LOGGER.info(msg);
    }

    private void initialiseDiscordThread() {
        discordThread = new DiscordThread(getConfig().getString("discord-token"),
                getConfig().getString("server-chat-channel-id"), getConfig().getString("admin-chat-channel-id"));

        discordThread.setName("BiomeChatRelay - Discord");
        discordThread.start();

        info("Running DiscordThread now!");
    }

    private void runPlayerCountTask() {
        getServer().getScheduler().runTaskTimerAsynchronously(this, () -> {
            String text = "Biome: " + getServer().getOnlinePlayers().size() + " players online!";
            discordThread.updatePresence(
                    StatusUpdate.builder()
                            .status(Status.ONLINE.getValue())
                            .afk(false)
                            .game(Activity.playing(text))
                            .build());
        }, 60 * 20, 60 * 20);
    }
}
