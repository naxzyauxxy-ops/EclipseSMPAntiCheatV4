package dev.eclipseac.listeners;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.*;
import org.bukkit.potion.PotionEffectType;

public class PlayerListener implements Listener {

    private final EclipseAC plugin;

    public PlayerListener(EclipseAC plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onJoin(PlayerJoinEvent event) {
        plugin.getDataManager().getData(event.getPlayer()).setRespawnTicks(60);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onQuit(PlayerQuitEvent event) {
        Player p = event.getPlayer();
        plugin.getDataManager().removeData(p.getUniqueId());
        plugin.getViolationManager().removePlayer(p.getUniqueId());
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onRespawn(PlayerRespawnEvent event) {
        PlayerData data = plugin.getDataManager().getData(event.getPlayer());
        data.setRespawnTicks(60);
        data.setTeleportTicks(20);
        data.setAirTicks(0);
        data.setFallDistance(0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onTeleport(PlayerTeleportEvent event) {
        if (event.isCancelled()) return;
        PlayerData data = plugin.getDataManager().getData(event.getPlayer());
        data.setTeleportTicks(20);
        data.setAirTicks(0);
        data.setGroundTicks(0);
        data.setFallDistance(0);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (event.isCancelled()) return;
        PlayerData data = plugin.getDataManager().getData(player);
        int ticks = switch (event.getCause()) {
            case ENTITY_ATTACK, ENTITY_SWEEP_ATTACK, PROJECTILE -> 10;
            case ENTITY_EXPLOSION, BLOCK_EXPLOSION              -> 15;
            default                                              ->  5;
        };
        data.setDamageTicks(Math.max(data.getDamageTicks(), ticks));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player     player = event.getPlayer();
        PlayerData data   = plugin.getDataManager().getData(player);

        data.decrementTeleportTicks();
        data.decrementVehicleExitTicks();
        data.decrementLiquidExitTicks();
        data.decrementClimbableExitTicks();
        data.decrementDamageTicks();
        data.decrementRespawnTicks();

        data.setInLiquid(isInLiquid(player));
        data.setOnClimbable(isOnClimbable(player));
        data.setInVehicle(player.isInsideVehicle());
        data.setHasSlowFalling(player.hasPotionEffect(PotionEffectType.SLOW_FALLING));

        boolean onGround  = player.isOnGround();
        boolean wasGround = data.isOnGround();
        data.setOnGround(onGround);

        if (!onGround) {
            data.incrementAirTicks();
            data.setGroundTicks(0);
        } else {
            data.setAirTicks(0);
            data.incrementGroundTicks();
        }

        double deltaY = event.getTo().getY() - event.getFrom().getY();
        data.setLastDeltaY(deltaY);

        boolean exempt = data.isInLiquid() || data.isOnClimbable() || data.hasSlowFalling();
        if (!onGround && deltaY < 0 && !exempt) {
            data.setFallDistance(data.getFallDistance() + (float) Math.abs(deltaY));
        }
        if (onGround && !wasGround && data.getFallDistance() > 0) {
            var nf = plugin.getNoFallCheck();
            if (nf != null) nf.checkLanding(player);
            data.setFallDistance(0);
        }
        if (exempt) data.setFallDistance(0);

        if (onGround) data.setLastGroundLocation(event.getTo().clone());
        data.setLastLocation(event.getTo().clone());
        data.setLastYaw(event.getTo().getYaw());
        data.setLastPitch(event.getTo().getPitch());
        data.setLastHorizontalSpeed(Math.sqrt(
            Math.pow(event.getTo().getX() - event.getFrom().getX(), 2) +
            Math.pow(event.getTo().getZ() - event.getFrom().getZ(), 2)
        ));
    }

    private boolean isInLiquid(Player player) {
        Material m = player.getLocation().getBlock().getType();
        return m == Material.WATER || m == Material.LAVA || m == Material.BUBBLE_COLUMN;
    }

    private boolean isOnClimbable(Player player) {
        return switch (player.getLocation().getBlock().getType()) {
            case LADDER, VINE, TWISTING_VINES, TWISTING_VINES_PLANT,
                 WEEPING_VINES, WEEPING_VINES_PLANT,
                 SCAFFOLDING, CAVE_VINES, CAVE_VINES_PLANT -> true;
            default -> false;
        };
    }
}
