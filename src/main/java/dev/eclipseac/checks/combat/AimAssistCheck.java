package dev.eclipseac.checks.combat;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AimAssistCheck extends Check {

    private static final int SAMPLE_SIZE = 20;

    private final Map<String, Deque<Double>> pitchErrors = new ConcurrentHashMap<>();
    private final Map<String, Deque<Double>> yawDeltas   = new ConcurrentHashMap<>();

    public AimAssistCheck(EclipseAC plugin) {
        super(plugin, "AimAssist", "Combat");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        float yaw = Math.abs(normalizeAngle(event.getTo().getYaw() - event.getFrom().getYaw()));
        String key = player.getUniqueId().toString();
        Deque<Double> yd = yawDeltas.computeIfAbsent(key, k -> new ArrayDeque<>());
        yd.addLast((double) yaw);
        if (yd.size() > SAMPLE_SIZE * 2) yd.pollFirst();
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod()) return;

        String key = player.getUniqueId().toString();

        double targetPitch = getPitchToTarget(player, target);
        double pitchError  = Math.abs(player.getLocation().getPitch() - targetPitch);

        Deque<Double> pd = pitchErrors.computeIfAbsent(key, k -> new ArrayDeque<>());
        pd.addLast(pitchError);
        if (pd.size() > SAMPLE_SIZE) pd.pollFirst();

        if (pd.size() >= SAMPLE_SIZE) {
            double stddev = stdDev(pd);
            double mean   = mean(pd);

            double stddevThreshold = getConfigDouble("pitch-stddev-threshold", 0.8);
            double meanThreshold   = getConfigDouble("pitch-mean-threshold", 2.0);

            if (stddev < stddevThreshold && mean < meanThreshold) {
                flag(player, "pitch_stddev=" + String.format("%.2f", stddev)
                    + " mean=" + String.format("%.2f", mean) + "° over " + SAMPLE_SIZE + " hits");
                pd.clear();
                return;
            }
        }

        Deque<Double> yd = yawDeltas.get(key);
        if (yd != null && yd.size() >= SAMPLE_SIZE) {
            double baselineStddev = stdDev(yd);
            if (baselineStddev < 0.5) {
                double attackYawDelta = Math.abs(normalizeAngle(
                    player.getLocation().getYaw() - data.getLastYaw()));
                double spikeMin   = getConfigDouble("yaw-snap-min-deg", 30.0);
                double spikeMulti = getConfigDouble("yaw-spike-multiplier", 5.0);
                if (attackYawDelta > spikeMin && attackYawDelta > mean(yd) * spikeMulti) {
                    flag(player, "yaw_spike=" + String.format("%.1f", attackYawDelta)
                        + "° baseline_stddev=" + String.format("%.2f", baselineStddev));
                }
            }
        }
    }

    private double getPitchToTarget(Player player, LivingEntity target) {
        var eye   = player.getEyeLocation();
        var tPos  = target.getBoundingBox().getCenter();
        double dx = tPos.getX() - eye.getX();
        double dy = tPos.getY() - eye.getY();
        double dz = tPos.getZ() - eye.getZ();
        return -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
    }

    private float normalizeAngle(float angle) {
        angle = angle % 360;
        if (angle > 180)  angle -= 360;
        if (angle < -180) angle += 360;
        return Math.abs(angle);
    }

    private double mean(Deque<Double> v) {
        return v.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    private double stdDev(Deque<Double> v) {
        double m = mean(v);
        return Math.sqrt(v.stream().mapToDouble(x -> Math.pow(x - m, 2)).average().orElse(0));
    }
}
