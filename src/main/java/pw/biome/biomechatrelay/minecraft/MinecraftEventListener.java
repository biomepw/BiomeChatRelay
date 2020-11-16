package pw.biome.biomechatrelay.minecraft;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.ServerLoadEvent;
import pw.biome.biomechat.obj.Corp;
import pw.biome.biomechat.obj.MetadataManager;
import pw.biome.biomechatrelay.BiomeChatRelay;
import pw.biome.biomechatrelay.util.ChatUtility;

public class MinecraftEventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerChat(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) return;
        Player player = event.getPlayer();
        String displayName = MetadataManager.getNicknameMap().getOrDefault(player.getUniqueId(), player.getName());
        String formattedMessage = displayName + " » " + event.getMessage();
        ChatUtility.sendToDiscord(formattedMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String displayName = MetadataManager.getNicknameMap().getOrDefault(player.getUniqueId(), player.getName());
        String formattedLeaveMessage = "> **» " + displayName + " has joined**";
        ChatUtility.sendToDiscord(formattedLeaveMessage);

        // Handle user's group changes
        BiomeChatRelay plugin = BiomeChatRelay.getInstance();
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () ->
                plugin.getDiscordThread().getDiscordGroupSyncHandler().handleUser(player.getDisplayName(), Corp.getCorpForUser(player.getUniqueId())));
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String displayName = MetadataManager.getNicknameMap().getOrDefault(player.getUniqueId(), player.getName());
        String formattedLeaveMessage = "> **» " + displayName + " has quit**";
        ChatUtility.sendToDiscord(formattedLeaveMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String displayName = MetadataManager.getNicknameMap().getOrDefault(player.getUniqueId(), player.getName());
        String deathMessage = event.getDeathMessage().replaceAll(player.getName(), displayName);
        String formattedDeathMessage = "> **» " + deathMessage + "**";
        ChatUtility.sendToDiscord(formattedDeathMessage);
    }

    @EventHandler
    public void serverLoad(ServerLoadEvent event) {
        String startMessage = "> :white_check_mark:  Server has started :white_check_mark:";
        ChatUtility.sendToDiscord(startMessage);
    }
}
