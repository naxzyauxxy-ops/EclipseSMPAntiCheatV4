package dev.eclipseac.checks.combat;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class KillAuraCheck extends Check {

    private static final int ANGLE_WINDOW     = 7;
    private static final int ROTATION_SAMPLES = 12;

    private final Map<String, Deque<Double>> angleBuffers    = new ConcurrentHashMap<>();
    private final Map<String, Deque<Double>> yawDeltaBuffers = new ConcurrentHashMap<>();

    public KillAuraCheck(EclipseAC plugin) {
        super(plugin, "KillAura", "Combat");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        PlayerData data = plugin.getDataManager().getData(player);
        data.recordAttack();

        String key = player.getUniqueId().toString();

        int maxHps = getConfigInt("max-hits-per-second", 20);
        int hps    = data.getCurrentCps();
        if (hps > maxHps) {
            flag(player, "hps=" + hps + " max=" + maxHps);
            return;
        }

        double angle    = getAngleBetween(player, (Entity) event.getEntity());
        double maxAngle = getConfigDouble("max-attack-angle", 80.0);

        Deque<Double> angleBuf = angleBuffers.computeIfAbsent(key, k -> new ArrayDeque<>());
        angleBuf.addLast(angle);
        if (angleBuf.size() > ANGLE_WINDOW) angleBuf.pollFirst();

        if (angleBuf.size() >= ANGLE_WINDOW) {
            long wideHits = angleBuf.stream().filter(a -> a > maxAngle).count();
            if (wideHits >= ANGLE_WINDOW) {
                flag(player, "angle=" + String.format("%.1f", angle) + "° max=" + maxAngle + "° streak=" + ANGLE_WINDOW);
                angleBuf.clear();
                return;
            }
        }

        if (getConfigBool("snap-check", true)) {
            float prevYaw  = data.getLastYaw();
            float curYaw   = player.getLocation().getYaw();
            float yawDelta = normalizeAngle(curYaw - prevYaw);

            Deque<Double> yawDeltas = yawDeltaBuffers.computeIfAbsent(key, k -> new ArrayDeque<>());
            yawDeltas.addLast((double) yawDelta);
            if (yawDeltas.size() > ROTATION_SAMPLES) yawDeltas.pollFirst();

            double snapThreshold = getConfigDouble("snap-threshold-deg", 90.0);
            if (yawDeltas.size() >= ROTATION_SAMPLES && yawDelta > snapThreshold) {
                long stillTicks = 0;
                int i = 0;
                for (Double d : yawDeltas) {
                    if (i < ROTATION_SAMPLES - 1 && d < 1.0) stillTicks++;
                    i++;
                }
                if (stillTicks >= ROTATION_SAMPLES - 2) {
                    flag(player, "yaw-snap=" + String.format("%.1f", yawDelta) + "° stillTicks=" + stillTicks);
                    yawDeltas.clear();
                    return;
                }
            }
        }

        reward(player);
    }

    private double getAngleBetween(Player player, Entity target) {
        var dir      = player.getEyeLocation().getDirection();
        var toTarget = ((LivingEntity) target).getBoundingBox().getCenter()
                             .subtract(player.getEyeLocation().toVector())
                             .normalize();
        double dot = Math.max(-1.0, Math.min(1.0, dir.dot(toTarget)));
        return Math.toDegrees(Math.acos(dot));
    }

    private float normalizeAngle(float angle) {
        angle = Math.abs(angle % 360);
        if (angle > 180) angle = 360 - angle;
        return angle;
    }
}
