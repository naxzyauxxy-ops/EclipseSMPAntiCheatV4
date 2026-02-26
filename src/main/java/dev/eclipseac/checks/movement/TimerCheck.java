package dev.eclipseac.checks.movement;

import dev.eclipseac.EclipseAC;
import dev.eclipseac.checks.Check;
import dev.eclipseac.managers.PlayerData;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerMoveEvent;

public class TimerCheck extends Check {

    public TimerCheck(EclipseAC plugin) {
        super(plugin, "Timer", "Movement");
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!isEnabled()) return;
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE ||
            player.getGameMode() == GameMode.SPECTATOR) return;

        PlayerData data = plugin.getDataManager().getData(player);
        if (data.isInGracePeriod()) return;

        long now        = System.currentTimeMillis();
        int  windowSecs = getConfigInt("average-window-seconds", 3);
        long windowMs   = windowSecs * 1000L;

        data.getMoveTimes().addLast(now);
        data.getMoveTimes().removeIf(t -> now - t > windowMs);

        double avgPps = (double) data.getMoveTimes().size() / windowSecs;
        int    maxPps = getConfigInt("max-packets-per-second", 25);

        long twoSecCount = data.getMoveTimes().stream().filter(t -> now - t <= 2000).count();
        int  burstPps    = getConfigInt("max-burst-pps", 32);

        if (avgPps > maxPps) {
            flag(player, String.format("avg_pps=%.1f max=%d window=%ds", avgPps, maxPps, windowSecs));
        } else if (twoSecCount > burstPps * 2) {
            flag(player, String.format("burst_pps=%d max=%d", twoSecCount / 2, burstPps));
        } else {
            reward(player);
        }

        data.setLastMoveTime(now);
    }
}
