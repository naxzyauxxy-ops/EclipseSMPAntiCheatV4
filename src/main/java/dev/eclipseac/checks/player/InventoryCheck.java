package dev.eclipseac.checks.player;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;

public class InventoryCheck extends Check {

    public InventoryCheck(EclipseAC plugin) {
        super(plugin, "Inventory", "Player");
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player p)) return;
        plugin.getDataManager().getData(p).setInventoryOpen(true);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player p)) return;
        plugin.getDataManager().getData(p).setInventoryOpen(false);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player p)) return;
        plugin.getDataManager().getData(p).setLastInventoryAction(System.currentTimeMillis());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        PlayerData data = plugin.getDataManager().getData(player);
        long now = System.currentTimeMillis();

        if (data.isInventoryOpen() && (now - data.getLastInventoryAction()) < 150) {
            flag(player, "attacked while inventory open, click_age=" + (now - data.getLastInventoryAction()) + "ms");
        } else {
            reward(player);
        }
    }
}
