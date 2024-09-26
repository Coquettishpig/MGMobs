package src.mgmobs;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.entity.Wolf;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import me.libraryaddict.disguise.DisguiseAPI;
import me.libraryaddict.disguise.disguisetypes.DisguiseType;
import me.libraryaddict.disguise.disguisetypes.MobDisguise;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

public class ItemClickListener implements Listener {

    private final MGMobs plugin;
    private final HashMap<UUID, Long> CDList = new HashMap<>();

    public ItemClickListener(MGMobs plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.hasItem()) {
            ItemStack item = event.getItem();
            if (item.getItemMeta() == null || item.getItemMeta().getLore() == null) {
                return;
            }

            for (String mobType : plugin.getConfig().getConfigurationSection("mobs").getKeys(false)) {
                String lore = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("mobs." + mobType + ".lore"));
                if (item.getItemMeta().getLore().contains(lore) && (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK)) {
                    event.setCancelled(true);
                    handleMobSpawn(player, mobType);
                    break;
                }
            }
        }
    }

    private void handleMobSpawn(Player player, String mobType) {
        UUID playerUUID = player.getUniqueId();
        long currentTime = System.currentTimeMillis();
        int cd = plugin.getConfig().getInt("mobs." + mobType + ".cd");

        if (CDList.containsKey(playerUUID)) {
            long lastUsedTime = CDList.get(playerUUID);
            if (lastUsedTime + (cd * 1000) > currentTime) {
                long remainingTime = (lastUsedTime + (cd * 1000) - currentTime) / 1000;
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("mobs." + mobType + ".nocdMessage")).replace("%cd%", String.valueOf(remainingTime)));
                return;
            }
        }

        CDList.put(playerUUID, currentTime);

        EntityType entityType = EntityType.valueOf(plugin.getConfig().getString("mobs." + mobType + ".entityType"));
        org.bukkit.entity.Entity entity = player.getWorld().spawnEntity(player.getLocation(), entityType);

        try {
            // 使用反射调用相应的方法
            Class<?> entityClass = entity.getClass();
            Method setCustomNameMethod = entityClass.getMethod("setCustomName", String.class);
            Method setCustomNameVisibleMethod = entityClass.getMethod("setCustomNameVisible", boolean.class);
            Method setMaxHealthMethod = entityClass.getMethod("setMaxHealth", double.class);
            Method setHealthMethod = entityClass.getMethod("setHealth", double.class);
            Method addPotionEffectMethod = entityClass.getMethod("addPotionEffect", PotionEffect.class);

            String customName = ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("mobs." + mobType + ".customName")).replace("%player%", player.getName());
            double health = plugin.getConfig().getDouble("mobs." + mobType + ".health");
            int speedLevel = plugin.getConfig().getInt("mobs." + mobType + ".speedLevel") - 1;

            setCustomNameMethod.invoke(entity, customName);
            setCustomNameVisibleMethod.invoke(entity, true);
            setMaxHealthMethod.invoke(entity, health);
            setHealthMethod.invoke(entity, health);
            addPotionEffectMethod.invoke(entity, new PotionEffect(PotionEffectType.SPEED, Integer.MAX_VALUE, speedLevel));

            // 免疫火焰
            addPotionEffectMethod.invoke(entity, new PotionEffect(PotionEffectType.FIRE_RESISTANCE, Integer.MAX_VALUE, 0));

            // 处理可驯服的生物
            if (isTameable(entity)) {
                Method setTamedMethod = entityClass.getMethod("setTamed", boolean.class);
                Method setOwnerMethod = entityClass.getMethod("setOwner", org.bukkit.entity.AnimalTamer.class);
                setTamedMethod.invoke(entity, true);
                setOwnerMethod.invoke(entity, player);
            }

            // 如果是马，不进行伪装，并附带钻石马铠和马鞍
            if (entityType == EntityType.HORSE) {
                Horse horse = (Horse) entity;
                horse.getInventory().setSaddle(new ItemStack(Material.SADDLE));
                horse.getInventory().setArmor(new ItemStack(Material.DIAMOND_HORSE_ARMOR));
            } else {
                // 伪装成其他生物
                DisguiseType disguiseType = DisguiseType.valueOf(entityType.name());
                MobDisguise disguise = new MobDisguise(disguiseType);
                DisguiseAPI.disguiseToAll(entity, disguise);
            }

            if (!plugin.getSpawnedEntities().containsKey(player.getName())) {
                plugin.getSpawnedEntities().put(player.getName(), new ArrayList<>());
            }
            plugin.getSpawnedEntities().get(player.getName()).add(entity);

            player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("mobs." + mobType + ".tamedMessage")));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isTameable(org.bukkit.entity.Entity entity) {
        return entity instanceof org.bukkit.entity.Tameable;
    }
}