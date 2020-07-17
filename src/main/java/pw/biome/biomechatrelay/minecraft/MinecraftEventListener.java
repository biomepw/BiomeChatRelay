package pw.biome.biomechatrelay.minecraft;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import pw.biome.biomechatrelay.common.ChatUtility;

public class MinecraftEventListener implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String formattedMessage = player.getDisplayName() + " » " + event.getMessage();
        ChatUtility.sendToDiscord(formattedMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String displayName = player.getDisplayName();
        String formattedLeaveMessage = "» " + displayName + " has joined";
        ChatUtility.sendToDiscord(formattedLeaveMessage);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String displayName = player.getDisplayName();
        String formattedLeaveMessage = "» " + displayName + " has quit";
        ChatUtility.sendToDiscord(formattedLeaveMessage);
    }
}
