package dev.eclipseac.checks.combat;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class SprintResetCheck extends Check {

    private final Map<UUID, Long>    lastHitTime      = new ConcurrentHashMap<>();
    private final Map<UUID, Boolean> wasSprintingOnHit = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> resetStreaks      = new ConcurrentHashMap<>();

    public SprintResetCheck(EclipseAC plugin) {
        super(plugin, "SprintReset", "Combat");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (!isEnabled()) return;
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity)) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod()) return;

        UUID id      = player.getUniqueId();
        long now     = System.currentTimeMillis();
        Long lastHit = lastHitTime.get(id);
        boolean wasSprinting = wasSprintingOnHit.getOrDefault(id, true);

        if (lastHit != null) {
            long gap = now - lastHit;
            if (gap < 200 && !wasSprinting && player.isSprinting()) {
                int streak    = resetStreaks.merge(id, 1, Integer::sum);
                int threshold = getConfigInt("reset-streak-threshold", 5);
                if (streak >= threshold) {
                    flag(player, "sprint_resets=" + streak + " gap=" + gap + "ms");
                    resetStreaks.put(id, 0);
                }
            } else if (gap > 500) {
                resetStreaks.put(id, 0);
            }
        }

        lastHitTime.put(id, now);
        wasSprintingOnHit.put(id, player.isSprinting());
    }
}
