package src.mgmobs;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

import java.util.*;

public class EntityFollowListener implements Listener {

    private final MGMobs plugin;

    public EntityFollowListener(MGMobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Entity entity = event.getEntity();
        for (Map.Entry<String, List<Entity>> entry : plugin.getSpawnedEntities().entrySet()) {
            List<Entity> entities = entry.getValue();
            if (entities.contains(entity)) {
                entities.remove(entity);
                break;
            }
        }
    }

    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof org.bukkit.entity.Wolf) {
            event.setDamage(plugin.getConfig().getDouble("mobs.wolf.damage"));
        } else if (event.getDamager() instanceof org.bukkit.entity.Zombie) {
            event.setDamage(plugin.getConfig().getDouble("mobs.zombie.damage"));
        }

        // 处理僵尸攻击事件
        if (event.getEntity() instanceof Player && event.getDamager() instanceof org.bukkit.entity.Zombie) {
            Player player = (Player) event.getEntity();
            org.bukkit.entity.Zombie zombie = (org.bukkit.entity.Zombie) event.getDamager();

            for (Map.Entry<String, List<Entity>> entry : plugin.getSpawnedEntities().entrySet()) {
                List<Object> entities = Collections.singletonList(entry.getValue());
                if (entities.contains(zombie)) {
                    UUID ownerUUID = (UUID) entities.get(entities.indexOf(zombie) + 1);
                    if (player.getUniqueId().equals(ownerUUID)) {
                        event.setCancelled(true); // 取消僵尸对召唤者的攻击
                        return;
                    }
                }
            }
        }
    }
}