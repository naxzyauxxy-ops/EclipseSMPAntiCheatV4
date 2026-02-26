package dev.eclipseac.checks.combat;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AntiVelocityCheck extends Check {

    private final Map<UUID, long[]> pendingKnockback = new ConcurrentHashMap<>();

    public AntiVelocityCheck(EclipseAC plugin) {
        super(plugin, "AntiVelocity", "Combat");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onHit(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player)) return;

        PlayerData data = plugin.getDataManager().getData(victim);
        if (data.isInGracePeriod() || data.isInLiquid()) return;

        pendingKnockback.put(victim.getUniqueId(), new long[]{System.currentTimeMillis()});
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        UUID   id     = player.getUniqueId();

        long[] pending = pendingKnockback.get(id);
        if (pending == null) return;

        long age = System.currentTimeMillis() - pending[0];

        if (age > 200) { pendingKnockback.remove(id); return; }
        if (age < 50)  return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod()) { pendingKnockback.remove(id); return; }

        double dx = Math.abs(event.getTo().getX() - event.getFrom().getX());
        double dz = Math.abs(event.getTo().getZ() - event.getFrom().getZ());
        double horizontal = Math.sqrt(dx * dx + dz * dz);

        double minExpected = getConfigDouble("min-knockback-horizontal", 0.28);
        if (horizontal < minExpected && player.isOnGround()) {
            flag(player, "velocity=" + String.format("%.4f", horizontal)
                + " expected>=" + minExpected + " age=" + age + "ms");
        }

        pendingKnockback.remove(id);
    }
}
