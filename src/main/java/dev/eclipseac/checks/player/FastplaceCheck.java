package dev.eclipseac.checks.player;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockPlaceEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class FastplaceCheck extends Check {

    private final Map<String, Integer> strikeMap = new ConcurrentHashMap<>();

    public FastplaceCheck(EclipseAC plugin) {
        super(plugin, "Fastplace", "Player");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        PlayerData data = plugin.getDataManager().getData(player);

        long now      = System.currentTimeMillis();
        long last     = data.getLastPlaceTime();
        long minDelay = getConfigInt("min-delay-ms", 60);

        int  ping    = player.getPing();
        long allowed = Math.max(10, minDelay - Math.min(ping / 4, 30));

        if (last > 0 && (now - last) < allowed) {
            int strikes = strikeMap.merge(player.getUniqueId().toString(), 1, Integer::sum);
            if (strikes >= 2) {
                flag(player, "delay=" + (now - last) + "ms min=" + allowed + "ms strikes=" + strikes);
                strikeMap.put(player.getUniqueId().toString(), 0);
            }
        } else {
            strikeMap.put(player.getUniqueId().toString(), 0);
            reward(player);
        }

        data.setLastPlaceTime(now);
    }
}
