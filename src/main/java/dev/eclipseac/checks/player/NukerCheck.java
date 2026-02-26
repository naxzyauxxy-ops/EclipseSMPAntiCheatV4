package dev.eclipseac.checks.player;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class NukerCheck extends Check {

    private final Map<UUID, Deque<Long>> breakTimes = new ConcurrentHashMap<>();

    public NukerCheck(EclipseAC plugin) {
        super(plugin, "Nuker", "Player");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBreak(BlockBreakEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();

        GameMode gm = player.getGameMode();
        if (gm == GameMode.CREATIVE || gm == GameMode.SPECTATOR) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod()) return;

        UUID id  = player.getUniqueId();
        long now = System.currentTimeMillis();

        Deque<Long> times = breakTimes.computeIfAbsent(id, k -> new ArrayDeque<>());
        times.addLast(now);
        times.removeIf(t -> now - t > 2000);

        int maxBps = getConfigInt("max-blocks-per-second", 7);

        var hasteEff = player.getPotionEffect(org.bukkit.potion.PotionEffectType.HASTE);
        if (hasteEff != null) maxBps += (hasteEff.getAmplifier() + 1) * 2;

        var tool = player.getInventory().getItemInMainHand();
        int effLevel = tool.getEnchantmentLevel(org.bukkit.enchantments.Enchantment.EFFICIENCY);
        if (effLevel > 0) maxBps += effLevel;

        double bps = times.size() / 2.0;
        if (bps > maxBps) {
            flag(player, "bps=" + String.format("%.1f", bps) + " max=" + maxBps);
            times.clear();
        }
    }
}
