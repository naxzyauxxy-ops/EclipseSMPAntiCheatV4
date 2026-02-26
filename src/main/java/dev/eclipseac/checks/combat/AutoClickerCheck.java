package dev.eclipseac.checks.combat;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Deque;

public class AutoClickerCheck extends Check {

    private static final int MIN_SAMPLES = 15;

    public AutoClickerCheck(EclipseAC plugin) {
        super(plugin, "AutoClicker", "Combat");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        PlayerData data   = plugin.getDataManager().getData(player);
        int maxCps = getConfigInt("max-cps", 20);
        int cps    = data.getCurrentCps();

        if (cps > maxCps) {
            flag(player, "cps=" + cps + " max=" + maxCps);
            return;
        }

        if (getConfigBool("consistency-check", true) && cps > 6) {
            Deque<Long> times = data.getAttackTimes();
            if (times.size() >= MIN_SAMPLES) {
                long[] arr    = times.stream().mapToLong(Long::longValue).toArray();
                double stddev = calculateStdDev(arr);
                double mean   = calculateMean(arr);
                double threshold = getConfigDouble("min-stddev-ms", 3.5);

                if (stddev < threshold) {
                    flag(player, "cps=" + cps + " stddev=" + String.format("%.2f", stddev) + "ms mean=" + String.format("%.0f", mean) + "ms");
                    return;
                }

                double modThreshold = getConfigDouble("moderate-stddev-ms", 8.0);
                if (cps >= 16 && stddev < modThreshold) {
                    flag(player, "cps=" + cps + " stddev=" + String.format("%.2f", stddev) + "ms");
                    return;
                }
            }
        }

        reward(player);
    }

    private double calculateStdDev(long[] times) {
        if (times.length < 2) return 999.0;
        long[] intervals = new long[times.length - 1];
        for (int i = 1; i < times.length; i++) intervals[i - 1] = times[i] - times[i - 1];
        double mean = 0;
        for (long v : intervals) mean += v;
        mean /= intervals.length;
        double variance = 0;
        for (long v : intervals) variance += Math.pow(v - mean, 2);
        variance /= intervals.length;
        return Math.sqrt(variance);
    }

    private double calculateMean(long[] times) {
        if (times.length < 2) return 0;
        long sum = 0;
        for (int i = 1; i < times.length; i++) sum += times[i] - times[i - 1];
        return (double) sum / (times.length - 1);
    }
}
