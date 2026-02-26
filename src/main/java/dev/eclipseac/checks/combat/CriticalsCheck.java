package dev.eclipseac.checks.combat;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class CriticalsCheck extends Check {

    private static final int WINDOW = 15;
    private final Map<UUID, Deque<Boolean>> critHistory = new ConcurrentHashMap<>();

    public CriticalsCheck(EclipseAC plugin) {
        super(plugin, "Criticals", "Combat");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod() || data.isInLiquid()) return;

        boolean isFalling  = data.getLastDeltaY() < -0.1;
        boolean isAirborne = !player.isOnGround();

        UUID id = player.getUniqueId();
        Deque<Boolean> hist = critHistory.computeIfAbsent(id, k -> new ArrayDeque<>());

        if (player.isOnGround() && !isFalling && event.getDamage() > 1.0) {
            hist.addLast(false);
            if (hist.size() > WINDOW) hist.pollFirst();

            if (hist.size() >= WINDOW) {
                long groundedHits = hist.stream().filter(b -> !b).count();
                double groundRate = (double) groundedHits / hist.size();
                double maxRate    = getConfigDouble("max-grounded-critical-rate", 0.9);
                if (groundRate >= maxRate) {
                    flag(player, "grounded_crit_rate=" + String.format("%.0f%%", groundRate * 100) + " over " + WINDOW + " hits");
                    hist.clear();
                }
            }
        } else {
            hist.addLast(true);
            if (hist.size() > WINDOW) hist.pollFirst();
            reward(player);
        }
    }
}
