package src.mgmobs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Iterator;
import java.util.List;

public class PlayerEventListener implements Listener {

    private final MGMobs plugin;

    public PlayerEventListener(MGMobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        handlePlayerLeave(event.getPlayer());
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        handlePlayerLeave(event.getEntity());
    }

    @EventHandler
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        handlePlayerLeave(event.getPlayer());
    }

    private void handlePlayerLeave(Player player) {
        if (plugin.getSpawnedEntities().containsKey(player.getName())) {
            List<Entity> entities = plugin.getSpawnedEntities().get(player.getName());
            for (Entity entity : entities) {
                entity.remove();
            }
            plugin.getSpawnedEntities().remove(player.getName());
        }
    }
}