package dev.eclipseac.checks.combat;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ReachCheck extends Check {

    private static final int REQUIRED_HITS = 3;
    private final Map<String, Deque<Double>> reachBuffers = new ConcurrentHashMap<>();

    public ReachCheck(EclipseAC plugin) {
        super(plugin, "Reach", "Combat");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        int    ping       = player.getPing();
        double pingBuffer = Math.min(ping / 1000.0 * 3.0, 0.6);
        double maxReach   = getConfigDouble("max-reach", 3.05) + pingBuffer;

        BoundingBox box = target.getBoundingBox().expand(getConfigDouble("hitbox-buffer", 0.1));
        double distance = distanceToBox(player.getEyeLocation().toVector(), box);

        String key = player.getUniqueId().toString();
        Deque<Double> buf = reachBuffers.computeIfAbsent(key, k -> new ArrayDeque<>());

        if (distance > maxReach) {
            buf.addLast(distance);
            if (buf.size() > REQUIRED_HITS) buf.pollFirst();

            if (buf.size() >= REQUIRED_HITS && buf.stream().allMatch(d -> d > maxReach)) {
                double avg = buf.stream().mapToDouble(Double::doubleValue).average().orElse(0);
                flag(player, "dist=" + String.format("%.2f", avg) + " max=" + String.format("%.2f", maxReach) + " ping=" + ping + "ms");
                buf.clear();
            }
        } else {
            buf.clear();
            reward(player);
        }
    }

    private double distanceToBox(Vector p, BoundingBox box) {
        double dx = Math.max(box.getMinX() - p.getX(), Math.max(0, p.getX() - box.getMaxX()));
        double dy = Math.max(box.getMinY() - p.getY(), Math.max(0, p.getY() - box.getMaxY()));
        double dz = Math.max(box.getMinZ() - p.getZ(), Math.max(0, p.getZ() - box.getMaxZ()));
        return Math.sqrt(dx * dx + dy * dy + dz * dz);
    }
}
